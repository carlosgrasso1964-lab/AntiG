package com.carlos.indextracker.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class IndexTrackerDbHelper(context: Context) :
    SQLiteOpenHelper(context, "indextracker.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE tb_indice (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                fonte TEXT NOT NULL,
                codigo TEXT NOT NULL DEFAULT '',
                api_url TEXT NOT NULL DEFAULT '',
                json_path TEXT NOT NULL DEFAULT '',
                sufixo TEXT NOT NULL DEFAULT '',
                cor TEXT NOT NULL DEFAULT '#1f4f8a',
                casas_decimais INTEGER NOT NULL DEFAULT 2,
                valor_padrao REAL NOT NULL DEFAULT 0,
                ativo INTEGER NOT NULL DEFAULT 1,
                ordem INTEGER NOT NULL DEFAULT 0,
                criado_em INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE tb_cotacao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                indice_id INTEGER NOT NULL,
                data TEXT NOT NULL,
                valor REAL NOT NULL,
                criado_em INTEGER NOT NULL,
                UNIQUE(indice_id, data),
                FOREIGN KEY(indice_id) REFERENCES tb_indice(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX idx_cotacao_indice_data ON tb_cotacao(indice_id, data)")

        // Apenas o dólar vem cadastrado: os demais índices são criados pelo usuário.
        insertIndice(db, Indice.dolar())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Estrutura antiga (lista simples, sem histórico de cards) não é compatível.
        db.execSQL("DROP TABLE IF EXISTS tb_cotacao")
        db.execSQL("DROP TABLE IF EXISTS tb_indice")
        onCreate(db)
    }

    // ---------- Índices ----------

    fun insertIndice(i: Indice): Long = insertIndice(writableDatabase, i)

    private fun insertIndice(db: SQLiteDatabase, i: Indice): Long {
        val cv = valoresIndice(i).apply { put("criado_em", i.criadoEm) }
        return db.insert("tb_indice", null, cv)
    }

    private fun valoresIndice(i: Indice): ContentValues = ContentValues().apply {
        put("nome", i.nome)
        put("fonte", i.fonte)
        put("codigo", i.codigo)
        put("api_url", i.apiUrl)
        put("json_path", i.jsonPath)
        put("sufixo", i.sufixo)
        put("cor", i.cor)
        put("casas_decimais", i.casasDecimais)
        put("valor_padrao", i.valorPadrao)
        put("ativo", if (i.ativo) 1 else 0)
        put("ordem", i.ordem)
    }

    fun updateIndice(i: Indice) {
        writableDatabase.update(
            "tb_indice", valoresIndice(i), "id = ?", arrayOf(i.id.toString())
        )
    }

    fun deleteIndice(id: Long) {
        writableDatabase.delete("tb_cotacao", "indice_id = ?", arrayOf(id.toString()))
        writableDatabase.delete("tb_indice", "id = ?", arrayOf(id.toString()))
    }

    fun listIndices(onlyActive: Boolean = false): List<Indice> {
        val where = if (onlyActive) "WHERE ativo = 1" else ""
        return readableDatabase.rawQuery(
            "SELECT * FROM tb_indice $where ORDER BY ordem, id", null
        ).use { c ->
            (0 until c.count).map {
                c.moveToPosition(it)
                Indice(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    nome = c.getString(c.getColumnIndexOrThrow("nome")),
                    fonte = c.getString(c.getColumnIndexOrThrow("fonte")),
                    codigo = c.getString(c.getColumnIndexOrThrow("codigo")),
                    apiUrl = c.getString(c.getColumnIndexOrThrow("api_url")),
                    jsonPath = c.getString(c.getColumnIndexOrThrow("json_path")),
                    sufixo = c.getString(c.getColumnIndexOrThrow("sufixo")),
                    cor = c.getString(c.getColumnIndexOrThrow("cor")),
                    casasDecimais = c.getInt(c.getColumnIndexOrThrow("casas_decimais")),
                    valorPadrao = c.getDouble(c.getColumnIndexOrThrow("valor_padrao")),
                    ativo = c.getInt(c.getColumnIndexOrThrow("ativo")) == 1,
                    ordem = c.getInt(c.getColumnIndexOrThrow("ordem")),
                    criadoEm = c.getLong(c.getColumnIndexOrThrow("criado_em"))
                )
            }
        }
    }

    fun getIndice(id: Long): Indice? = listIndices().firstOrNull { it.id == id }

    fun proximaOrdem(): Int = listIndices().maxOfOrNull { it.ordem }?.plus(1) ?: 0

    // ---------- Cotações ----------

    fun upsertCotacao(indiceId: Long, data: String, valor: Double) {
        val cv = ContentValues().apply {
            put("indice_id", indiceId)
            put("data", data)
            put("valor", valor)
            put("criado_em", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(
            "tb_cotacao", null, cv, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /** Histórico do índice em ordem cronológica, limitado aos últimos [limite] registros. */
    fun getCotacoes(indiceId: Long, limite: Int = 90): List<Cotacao> {
        val lista = readableDatabase.rawQuery(
            "SELECT * FROM tb_cotacao WHERE indice_id = ? ORDER BY data DESC LIMIT ?",
            arrayOf(indiceId.toString(), limite.toString())
        ).use { c ->
            (0 until c.count).map {
                c.moveToPosition(it)
                Cotacao(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    indiceId = c.getLong(c.getColumnIndexOrThrow("indice_id")),
                    data = c.getString(c.getColumnIndexOrThrow("data")),
                    valor = c.getDouble(c.getColumnIndexOrThrow("valor")),
                    criadoEm = c.getLong(c.getColumnIndexOrThrow("criado_em"))
                )
            }
        }
        return lista.reversed()
    }

    fun getUltimoValor(indiceId: Long): Double? {
        return readableDatabase.rawQuery(
            "SELECT valor FROM tb_cotacao WHERE indice_id = ? ORDER BY data DESC LIMIT 1",
            arrayOf(indiceId.toString())
        ).use { c -> if (c.moveToFirst()) c.getDouble(0) else null }
    }

    /** Todo o histórico, usado na exportação do backup. */
    fun getTodasCotacoes(): List<Cotacao> {
        return readableDatabase.rawQuery(
            "SELECT * FROM tb_cotacao ORDER BY indice_id, data", null
        ).use { c ->
            (0 until c.count).map {
                c.moveToPosition(it)
                Cotacao(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    indiceId = c.getLong(c.getColumnIndexOrThrow("indice_id")),
                    data = c.getString(c.getColumnIndexOrThrow("data")),
                    valor = c.getDouble(c.getColumnIndexOrThrow("valor")),
                    criadoEm = c.getLong(c.getColumnIndexOrThrow("criado_em"))
                )
            }
        }
    }

    /** Substitui todos os dados pelos do backup, preservando os ids dos índices. */
    fun substituirTudo(indices: List<Indice>, cotacoes: List<Cotacao>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("tb_cotacao", null, null)
            db.delete("tb_indice", null, null)

            indices.forEach { indice ->
                val cv = valoresIndice(indice).apply {
                    put("id", indice.id)
                    put("criado_em", indice.criadoEm)
                }
                db.insertOrThrow("tb_indice", null, cv)
            }

            cotacoes.forEach { cotacao ->
                val cv = ContentValues().apply {
                    put("indice_id", cotacao.indiceId)
                    put("data", cotacao.data)
                    put("valor", cotacao.valor)
                    put("criado_em", cotacao.criadoEm)
                }
                db.insertWithOnConflict(
                    "tb_cotacao", null, cv, SQLiteDatabase.CONFLICT_REPLACE
                )
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun ultimaData(): String? {
        return readableDatabase.rawQuery(
            "SELECT MAX(data) FROM tb_cotacao", null
        ).use { c -> if (c.moveToFirst() && !c.isNull(0)) c.getString(0) else null }
    }

    fun reiniciarHistorico() {
        writableDatabase.delete("tb_cotacao", null, null)
    }
}
