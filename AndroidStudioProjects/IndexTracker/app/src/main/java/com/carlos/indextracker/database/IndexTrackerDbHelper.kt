package com.carlos.indextracker.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class IndexTrackerDbHelper(context: Context) : SQLiteOpenHelper(context, "indextracker.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE tb_indice (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                ativo TEXT NOT NULL,
                fonte TEXT NOT NULL,
                sufixo TEXT NOT NULL,
                cor TEXT NOT NULL,
                ativo_ativo INTEGER NOT NULL DEFAULT 1,
                criado_em INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE tb_cotacao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                indice_id INTEGER NOT NULL,
                data TEXT NOT NULL,
                valor REAL NOT NULL,
                criado_em INTEGER NOT NULL,
                UNIQUE(indice_id, data),
                FOREIGN KEY(indice_id) REFERENCES tb_indice(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX idx_cotacao_indice_data ON tb_cotacao(indice_id, data)")

        // Seed inicial com os índices do HTML
        seed(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tb_cotacao")
        db.execSQL("DROP TABLE IF EXISTS tb_indice")
        onCreate(db)
    }

    private fun seed(db: SQLiteDatabase) {
        val iniciais = listOf(
            Indice(nome = "Dólar", ativo = "USD", fonte = "exchangerate", sufixo = " R$", cor = "#1f4f8a"),
            Indice(nome = "Euro", ativo = "EUR", fonte = "exchangerate", sufixo = " R$", cor = "#2b7a4b"),
            Indice(nome = "Libra", ativo = "GBP", fonte = "exchangerate", sufixo = " R$", cor = "#b13e3e"),
            Indice(nome = "Yuan", ativo = "CNY", fonte = "exchangerate", sufixo = " R$", cor = "#c97f1a"),
            Indice(nome = "Iene", ativo = "JPY", fonte = "exchangerate", sufixo = " R$", cor = "#8b5a9e"),
            Indice(nome = "Bitcoin", ativo = "bitcoin", fonte = "coingecko", sufixo = " R$", cor = "#f7931a"),
            Indice(nome = "Ouro", ativo = "XAU", fonte = "gold", sufixo = " R$", cor = "#d4a017"),
            Indice(nome = "IBOVESPA", ativo = "^BVSP", fonte = "yahoo", sufixo = " pts", cor = "#2a6f3a")
        )
        iniciais.forEach { insertIndice(it) }
    }

    fun insertIndice(i: Indice): Long {
        val cv = ContentValues().apply {
            put("nome", i.nome)
            put("ativo", i.ativo)
            put("fonte", i.fonte)
            put("sufixo", i.sufixo)
            put("cor", i.cor)
            put("ativo_ativo", if (i.ativoAtivo) 1 else 0)
            put("criado_em", i.criadoEm)
        }
        return writableDatabase.insert("tb_indice", null, cv)
    }

    fun updateIndice(i: Indice) {
        val cv = ContentValues().apply {
            put("nome", i.nome)
            put("ativo", i.ativo)
            put("fonte", i.fonte)
            put("sufixo", i.sufixo)
            put("cor", i.cor)
            put("ativo_ativo", if (i.ativoAtivo) 1 else 0)
        }
        writableDatabase.update("tb_indice", cv, "id = ?", arrayOf(i.id.toString()))
    }

    fun deleteIndice(id: Long) {
        writableDatabase.delete("tb_cotacao", "indice_id = ?", arrayOf(id.toString()))
        writableDatabase.delete("tb_indice", "id = ?", arrayOf(id.toString()))
    }

    fun listIndices(onlyActive: Boolean = false): List<Indice> {
        val sql = if (onlyActive) "SELECT * FROM tb_indice WHERE ativo_ativo = 1 ORDER BY nome"
                  else "SELECT * FROM tb_indice ORDER BY nome"
        return readableDatabase.rawQuery(sql, null).use { c ->
            (0 until c.count).map {
                c.moveToPosition(it)
                Indice(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    nome = c.getString(c.getColumnIndexOrThrow("nome")),
                    ativo = c.getString(c.getColumnIndexOrThrow("ativo")),
                    fonte = c.getString(c.getColumnIndexOrThrow("fonte")),
                    sufixo = c.getString(c.getColumnIndexOrThrow("sufixo")),
                    cor = c.getString(c.getColumnIndexOrThrow("cor")),
                    ativoAtivo = c.getInt(c.getColumnIndexOrThrow("ativo_ativo")) == 1,
                    criadoEm = c.getLong(c.getColumnIndexOrThrow("criado_em"))
                )
            }
        }
    }

    fun getIndice(id: Long): Indice? {
        return readableDatabase.rawQuery("SELECT * FROM tb_indice WHERE id = ?", arrayOf(id.toString())).use { c ->
            if (c.moveToFirst()) {
                Indice(
                    id = c.getLong(0),
                    nome = c.getString(c.getColumnIndexOrThrow("nome")),
                    ativo = c.getString(c.getColumnIndexOrThrow("ativo")),
                    fonte = c.getString(c.getColumnIndexOrThrow("fonte")),
                    sufixo = c.getString(c.getColumnIndexOrThrow("sufixo")),
                    cor = c.getString(c.getColumnIndexOrThrow("cor")),
                    ativoAtivo = c.getInt(c.getColumnIndexOrThrow("ativo_ativo")) == 1,
                    criadoEm = c.getLong(c.getColumnIndexOrThrow("criado_em"))
                )
            } else null
        }
    }

    fun upsertCotacao(indiceId: Long, data: String, valor: Double) {
        val cv = ContentValues().apply {
            put("indice_id", indiceId)
            put("data", data)
            put("valor", valor)
            put("criado_em", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("tb_cotacao", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getCotacoes(indiceId: Long): List<Cotacao> {
        return readableDatabase.rawQuery(
            "SELECT * FROM tb_cotacao WHERE indice_id = ? ORDER BY data",
            arrayOf(indiceId.toString())
        ).use { c ->
            (0 until c.count).map {
                c.moveToPosition(it)
                Cotacao(
                    id = c.getLong(0),
                    indiceId = c.getLong(c.getColumnIndexOrThrow("indice_id")),
                    data = c.getString(c.getColumnIndexOrThrow("data")),
                    valor = c.getDouble(c.getColumnIndexOrThrow("valor")),
                    criadoEm = c.getLong(c.getColumnIndexOrThrow("criado_em"))
                )
            }
        }
    }

    fun arquivarAntigos(anos: Int = 1): Int {
        val limite = java.time.LocalDate.now().minusYears(anos.toLong()).toString()
        return writableDatabase.delete("tb_cotacao", "data < ?", arrayOf(limite))
    }
}
