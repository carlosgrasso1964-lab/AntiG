package com.carlos.finas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.getLongOrNull
import com.carlos.finas.models.Clifor
import com.carlos.finas.models.Lancamento
import com.carlos.finas.models.PlanoContas
import com.carlos.finas.models.Recurso
import com.carlos.finas.models.Usuario
import org.mindrot.jbcrypt.BCrypt
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "FinASDB"
        private const val DATABASE_VERSION = 13 // Incrementado para criar tb_plano_diretor
        private var cachedRecursos: List<Recurso>? = null

        // Tabela usu
        private const val TABLE_USUARIO = "usu"
        private const val COL_ID_USUARIO = "id"
        private const val COL_USUARIO = "usuario"
        private const val COL_SENHA = "senha"

        // Tabela tbrecursos
        const val TABLE_RECURSOS = "tbrecursos"
        private const val COL_CODIGO = "codigo"
        private const val COL_NOMEBCO = "nomebco"
        private const val COL_AGENCIA = "agencia"
        private const val COL_FLUXO = "fluxo"
        private const val COL_LIMITE = "limite"
        private const val COL_ABERTURA = "abertura"
        private const val COL_ENCERRAMENTO = "encerramento"
        private const val COL_STATUS = "status"
        private const val COL_FK_GPPRINC = "fk_gpprinc"

        // Tabela tbmovimento
        const val TABLE_LANCAMENTOS = "tbmovimento"
        const val COL_ID_MOV = "idMov"
        private const val COL_RECURSO = "recurso"
        private const val COL_VRECURSO = "vrecurso"
        private const val COL_CLIFOR = "clifor"
        private const val COL_VCLIFOR = "vCliFor"
        private const val COL_DTLANCTO = "dtlancto"
        private const val COL_DT_EMI = "dtEmi"
        private const val COL_DT_VCTO = "dtVcto"
        private const val COL_DOCUMENTO = "documento"
        private const val COL_CLASSIF = "classif"
        private const val COL_DESCR = "Descr"
        private const val COL_VALOR = "Valor"
        const val COL_DT_APR = "dtApr"
        const val COL_STATUS_MOV = "statusMov"
        private const val COL_PREV = "Prev"

        // Tabela tbclifor
        const val TABLE_CLIFOR = "tbclifor"
        private const val COL_COD_CLIFOR = "codCliFor"
        private const val COL_TIPO = "Tipo"
        private const val COL_NOME_CLIFOR = "nomeCliFor"
        private const val COL_APELIDO_CLIFOR = "apelidoCliFor"
        private const val COL_EMAIL = "email"
        private const val COL_CELULAR = "celular"
        private const val COL_TELEFONE = "telefone"
        private const val COL_CEP = "cep"
        private const val COL_ENDERECO = "endereco"
        private const val COL_NUMERO = "numero"
        private const val COL_COMPLEMENTO = "complemento"
        private const val COL_BAIRRO = "bairro"
        private const val COL_CIDADE = "cidade"
        private const val COL_ESTADO = "estado"
        private const val COL_RG = "rg"
        private const val COL_CPF = "cpf"
        private const val COL_CONTATO_CLIFOR = "contatoCliFor"
        private const val COL_OBS = "obs"
        private const val COL_FK_CLIFOR_GP = "fkCliForGp"

        // Tabela gpprincipal
        private const val TABLE_GPPRINCIPAL = "gpprincipal"
        private const val COL_COD_GERAL = "cod_Geral"
        private const val COL_NOME_P = "nome_P"
        private const val COL_NOME_S = "nome_S"
        private const val COL_NOME_C = "nome_C"

        // Tabela testbirth
        private const val TABLE_TESTBIRTH = "testbirth"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_BDATE = "Bdate"

        // Tabela tb_plano_diretor
        const val TABLE_PLANO_DIRETOR = "tb_plano_diretor"
        private const val COL_PD_ID = "id"
        private const val COL_PD_ANO_REFERENCIA = "ano_referencia"
        private const val COL_PD_MES = "mes"
        private const val COL_PD_CLASSIFICACAO = "classificacao"
        private const val COL_PD_CONTA = "conta"
        private const val COL_PD_VALOR_PLANEJADO = "valor_planejado"
        private const val COL_PD_INFLACAO_PREMISSA = "inflacao_premissa"
        private const val COL_PD_DATA_SNAPSHOT = "data_snapshot"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DatabaseHelper", "Iniciando criação do banco de dados")
        try {
            // === CRIAR TABELAS ===
            val createUsuarioTable = """
            CREATE TABLE $TABLE_USUARIO (
                $COL_ID_USUARIO INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USUARIO TEXT NOT NULL,
                $COL_SENHA TEXT NOT NULL
            )
        """.trimIndent()

            val createRecursosTable = """
            CREATE TABLE $TABLE_RECURSOS (
                $COL_CODIGO VARCHAR(4) PRIMARY KEY,
                $COL_NOMEBCO VARCHAR(45),
                $COL_AGENCIA VARCHAR(10),
                $COL_FLUXO CHAR(1),
                $COL_LIMITE DOUBLE,
                $COL_ABERTURA TEXT NOT NULL,
                $COL_ENCERRAMENTO TEXT NOT NULL,
                $COL_STATUS CHAR(1),
                $COL_FK_GPPRINC VARCHAR(40)
            )
        """.trimIndent()

            val createLancamentosTable = """
            CREATE TABLE $TABLE_LANCAMENTOS (
                $COL_ID_MOV INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_RECURSO VARCHAR(4),
                $COL_VRECURSO VARCHAR(15),
                $COL_CLIFOR VARCHAR(4),
                $COL_VCLIFOR VARCHAR(15),
                $COL_DTLANCTO TEXT NOT NULL,
                $COL_DT_EMI TEXT NOT NULL,
                $COL_DT_VCTO TEXT NOT NULL,
                $COL_DOCUMENTO VARCHAR(20),
                $COL_CLASSIF VARCHAR(40),
                $COL_DESCR VARCHAR(150),
                $COL_VALOR REAL NOT NULL,
                $COL_DT_APR TEXT,
                $COL_STATUS_MOV CHAR(2),
                $COL_PREV CHAR(1)
            )
        """.trimIndent()

            val createCliforTable = """
            CREATE TABLE $TABLE_CLIFOR (
                $COL_COD_CLIFOR VARCHAR(4) PRIMARY KEY,
                $COL_TIPO VARCHAR(3),
                $COL_NOME_CLIFOR VARCHAR(100),
                $COL_APELIDO_CLIFOR VARCHAR(50),
                $COL_EMAIL VARCHAR(200),
                $COL_CELULAR VARCHAR(30),
                $COL_TELEFONE VARCHAR(30),
                $COL_CEP VARCHAR(100),
                $COL_ENDERECO VARCHAR(255),
                $COL_NUMERO INTEGER NOT NULL DEFAULT 0,
                $COL_COMPLEMENTO VARCHAR(200),
                $COL_BAIRRO VARCHAR(100),
                $COL_CIDADE VARCHAR(100),
                $COL_ESTADO VARCHAR(2),
                $COL_RG VARCHAR(30),
                $COL_CPF VARCHAR(20),
                $COL_CONTATO_CLIFOR VARCHAR(40),
                $COL_OBS VARCHAR(200),
                $COL_FK_CLIFOR_GP VARCHAR(10)
            )
        """.trimIndent()

            val createGpPrincipalTable = """
            CREATE TABLE $TABLE_GPPRINCIPAL (
                $COL_COD_GERAL VARCHAR(40) PRIMARY KEY,
                $COL_NOME_P VARCHAR(45),
                $COL_NOME_S VARCHAR(45),
                $COL_NOME_C VARCHAR(100)
            )
        """.trimIndent()

            val createTestbirthTable = """
            CREATE TABLE $TABLE_TESTBIRTH (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME VARCHAR(40),
                $COL_BDATE VARCHAR(100)
            )
        """.trimIndent()

            val createPlanoDiretorTable = """
            CREATE TABLE $TABLE_PLANO_DIRETOR (
                $COL_PD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PD_ANO_REFERENCIA INTEGER,
                $COL_PD_MES INTEGER,
                $COL_PD_CLASSIFICACAO TEXT,
                $COL_PD_CONTA TEXT,
                $COL_PD_VALOR_PLANEJADO REAL,
                $COL_PD_INFLACAO_PREMISSA REAL,
                $COL_PD_DATA_SNAPSHOT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

            db.execSQL(createUsuarioTable)
            db.execSQL(createRecursosTable)
            db.execSQL(createLancamentosTable)
            db.execSQL(createCliforTable)
            db.execSQL(createGpPrincipalTable)
            db.execSQL(createTestbirthTable)
            db.execSQL(createPlanoDiretorTable)

            // === ÍNDICES ===
            db.execSQL("CREATE INDEX idx_tbrecursos_nomebco ON $TABLE_RECURSOS($COL_NOMEBCO)")
            db.execSQL("CREATE INDEX idx_tbmovimento_recurso ON $TABLE_LANCAMENTOS($COL_RECURSO)")
            db.execSQL("CREATE INDEX idx_tbmovimento_dtlancto ON $TABLE_LANCAMENTOS($COL_DTLANCTO)")
            db.execSQL("CREATE INDEX idx_tbmovimento_dtVcto ON $TABLE_LANCAMENTOS($COL_DT_VCTO)")
            db.execSQL("CREATE INDEX idx_tbmovimento_vCliFor ON $TABLE_LANCAMENTOS($COL_VCLIFOR)")
            db.execSQL("CREATE INDEX idx_tbmovimento_classif ON $TABLE_LANCAMENTOS($COL_CLASSIF)")

            // === USUÁRIO PADRÃO ===
            val hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt())
            db.execSQL(
                "INSERT INTO $TABLE_USUARIO ($COL_USUARIO, $COL_SENHA) VALUES (?, ?)",
                arrayOf("admin", hashedPassword)
            )

            // === DADOS DE TESTE ===
            db.execSQL(
                """
            INSERT INTO $TABLE_GPPRINCIPAL (cod_Geral, nome_P, nome_S, nome_C) VALUES
            ('1.001.001', 'ATIVO', 'ATIVO CIRCULANTE', 'Bancos'),
            ('1.001.003', 'ATIVO', 'ATIVO CIRCULANTE', 'Contas_à_Receber'),
            ('1.001.004', 'ATIVO', 'ATIVO CIRCULANTE', 'Caixa'),
            ('1.001.006', 'ATIVO', 'ATIVO NÃO CIRCULANTE', 'Imobilizado'),
            ('2.001.002', 'PASSIVO', 'PASSIVO CIRCULANTE', 'Contas_à_Pagar'),
            ('2.001.005', 'PASSIVO', 'PASSIVO CIRCULANTE', 'Cartões_de_Crédito'),
            ('2.001.007', 'PASSIVO', 'PASSIVO CIRCULANTE', 'Fornecedores')
        """.trimIndent()
            )

            db.execSQL(
                """
            INSERT INTO $TABLE_RECURSOS (codigo, nomebco, agencia, fluxo, limite, abertura, encerramento, status, fk_gpprinc) VALUES
            ('0022', 'Banco A', '0152', 'S', 0, '2000-01-01', '2099-12-31', 'A', '1.001.001'),
            ('0079', 'Banco B', '0000', 'S', 8000, '2000-01-01', '2099-12-31', 'A', '1.001.003')
        """.trimIndent()
            )

            db.execSQL(
                """
            INSERT INTO $TABLE_CLIFOR ($COL_COD_CLIFOR, $COL_TIPO, $COL_NOME_CLIFOR, $COL_FK_CLIFOR_GP) VALUES
            ('C001', 'CLI', 'Cliente A', '0022'),
            ('F001', 'FOR', 'Fornecedor X', '0079')
        """.trimIndent()
            )

            db.execSQL(
                """
            INSERT INTO $TABLE_LANCAMENTOS (idMov, recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento, classif, Descr, Valor, dtApr, statusMov, Prev) VALUES
            (1, '0022', '1.001.001', '001', 'Cliente A', '2025-08-01', '2025-08-01', '2025-08-15', 'DOC001', '3.001.001', 'Recebimento', 1000.0, '2025-08-01', 'A', 'N'),
            (2, '0079', '2.001.002', '002', 'Cliente B', '2025-08-02', '2025-08-02', '2025-08-16', 'DOC002', '4.001.002', 'Pagamento', -500.0, '2025-08-02', 'A', 'N'),
            (3, '0022', '1.001.003', '003', 'Cliente C', '2025-08-03', '2025-08-03', '2025-08-17', 'DOC003', '3.001.003', 'Bancos', 2000.0, '2025-08-03', 'A', 'N'),
            (4, '0079', '1.001.004', '004', 'Cliente D', '2025-08-04', '2025-08-04', '2025-08-18', 'DOC004', '3.001.004', 'Contas_à_Receber', 1500.0, '2025-08-04', 'A', 'N'),
            (5, '0022', '2.001.005', '005', 'Cliente E', '2025-08-05', '2025-08-05', '2025-08-19', 'DOC005', '4.001.005', 'Contas_à_Pagar', -800.0, '2025-08-05', 'A', 'N'),
            (6, '0079', '1.001.006', '006', 'Cliente F', '2025-08-06', '2025-08-06', '2025-08-20', 'DOC006', '3.001.006', 'Imobilizado', 3000.0, '2025-08-06', 'A', 'N'),
            (7, '0022', '2.001.007', '007', 'Cliente G', '2025-08-07', '2025-08-07', '2025-08-21', 'DOC007', '4.001.007', 'Cartões_de_Crédito', -600.0, '2025-08-07', 'A', 'N')
        """.trimIndent()
            )

            db.execSQL(
                """
            INSERT INTO $TABLE_TESTBIRTH ($COL_NAME, $COL_BDATE) VALUES
            ('João Silva', '15/05/1990'),
            ('Maria Oliveira', '22/12/1985')
        """.trimIndent()
            )

            // === RECRIAR TODAS AS VIEWS (CENTRALIZADO) ===
            recreateAllViews(db)

            Log.d("DatabaseHelper", "Banco criado com sucesso")
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro na criação do banco: ${e.message}")
            throw e
        }
        db.execSQL("PRAGMA user_version = $DATABASE_VERSION")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("DatabaseHelper", "Atualizando banco de $oldVersion para $newVersion")
        try {
            // Dropar views e tabelas
            db.execSQL("DROP VIEW IF EXISTS vsaldos")
            db.execSQL("DROP VIEW IF EXISTS view_movimento")
            db.execSQL("DROP VIEW IF EXISTS view_mov")
            db.execSQL("DROP VIEW IF EXISTS viewmovrd")

            db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIO")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_RECURSOS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_LANCAMENTOS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CLIFOR")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_GPPRINCIPAL")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TESTBIRTH")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PLANO_DIRETOR")

            onCreate(db)
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro na atualização do banco: ${e.message}")
            throw e
        }
        db.execSQL("PRAGMA user_version = $DATABASE_VERSION")
    }

    fun ensureViewsExist(): Boolean {
        val db = writableDatabase
        try {
            val tables = listOf(TABLE_LANCAMENTOS, TABLE_GPPRINCIPAL, TABLE_RECURSOS)
            for (table in tables) {
                db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    arrayOf(table)
                ).use { cursor ->
                    if (!cursor.moveToFirst()) {
                        Log.e("DatabaseHelper", "Tabela $table não encontrada")
                        return false
                    }
                }
            }

            recreateAllViews(db)
            Log.d("DatabaseHelper", "Views recriadas com sucesso")
            return true
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro ao recriar views: ${e.message}", e)
            return false
        } finally {
            db.close()
        }
    }

    // === FUNÇÃO CENTRAL: RECRIAR TODAS AS VIEWS ===
    private fun recreateAllViews(db: SQLiteDatabase) {
        // 1. view_mov (igual ao SQLite) - CORRIGIDO
        db.execSQL("DROP VIEW IF EXISTS view_mov")
        Log.d("DatabaseHelper", "Recriando view_mov")
        db.execSQL(
            """
        CREATE VIEW view_mov AS
        SELECT 
            tbmovimento.idMov AS idMov,
            tbmovimento.recurso AS recurso,
            tbmovimento.vrecurso AS vrecurso,
            tbmovimento.clifor AS clifor,
            tbmovimento.vCliFor AS vCliFor,
            tbmovimento.dtlancto AS dtlancto,
            tbmovimento.dtEmi AS dtEmi,
            tbmovimento.dtVcto AS dtVcto,
            tbmovimento.documento AS documento,
            tbmovimento.classif AS classif,
            tbmovimento.Descr AS Descr,
            tbmovimento.Valor AS Valor,
            tbmovimento.dtApr AS dtApr,
            tbmovimento.statusMov AS statusMov,
            tbmovimento.Prev AS Prev,
            gpprincipal.nome_P AS nome_P,
            gpprincipal.nome_S AS nome_S,
            gpprincipal.nome_C AS nome_C,
            tbrecursos.nomebco AS nomebcoR,
            tbrecursos.codigo AS codrecR
        FROM tbmovimento
        JOIN gpprincipal ON tbmovimento.vrecurso = gpprincipal.cod_Geral
        JOIN tbrecursos ON tbmovimento.recurso = tbrecursos.codigo
    """.trimIndent()
        )

        // 2. view_movimento
        db.execSQL("DROP VIEW IF EXISTS view_movimento")
        Log.d("DatabaseHelper", "Recriando view_movimento")
        db.execSQL(
            """
        CREATE VIEW view_movimento AS
        SELECT 
            m.idMov AS idMov,
            m.recurso AS recurso,
            m.vrecurso AS vrecurso,
            m.clifor AS clifor,
            m.vCliFor AS vCliFor,
            m.dtlancto AS dtlancto,
            m.dtEmi AS dtEmi,
            m.dtVcto AS dtVcto,
            m.documento AS documento,
            m.classif AS classif,
            m.Descr AS Descr,
            m.Valor AS Valor,
            m.dtApr AS dtApr,
            m.statusMov AS statusMov,
            m.Prev AS Prev,
            g.nome_P AS nome_P,
            g.nome_S AS nome_S,
            g.nome_C AS nome_C,
            r.nomebco AS nomebco
        FROM tbmovimento AS m
        LEFT JOIN gpprincipal AS g ON m.classif = g.cod_Geral
        LEFT JOIN tbrecursos AS r ON m.recurso = r.codigo
    """.trimIndent()
        )

        // 3. vsaldos
        db.execSQL("DROP VIEW IF EXISTS vsaldos")
        Log.d("DatabaseHelper", "Recriando vsaldos")
        db.execSQL(
            """
        CREATE VIEW vsaldos AS
        SELECT
            principal,
            subprincipal,
            conta,
            Fonte,
            vrecurso,
            recurso,
            ROUND(SUM(saldos), 2) AS saldos
        FROM (
            SELECT
                nome_P AS principal,
                nome_S AS subprincipal,
                CASE
                    WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN 'Contas à Receber'
                    WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN 'Contas à Receber'
                    ELSE nome_C
                END AS conta,
                CASE
                    WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' THEN 'CONTAS À RECEBER'
                    WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN 'CONTAS À RECEBER'
                    ELSE nomebcoR
                END AS Fonte,
                CASE
                    WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN '1.002.001'
                    WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN '1.002.001'
                    ELSE vrecurso
                END AS vrecurso,
                recurso,
                Valor AS saldos
            FROM view_mov
            WHERE
                IFNULL(Prev, '') <> 'F'
                AND (nome_C <> 'Contas à Pagar' OR (nome_C = 'Contas à Pagar' AND (dtApr IS NULL OR dtApr = '')))
                AND NOT (nomebcoR = 'CONTAS À PAGAR' AND nome_C NOT IN ('Contas à Pagar', 'Contas à Receber'))
        ) AS sub
        WHERE
            NOT (conta = 'Bancos' AND Fonte = 'CONTAS À RECEBER')
            AND NOT (conta IN ('Bancos', 'Caixa', 'Cartões de Crédito', 'Empr.Obtidos Curto Prazo') AND Fonte = 'CONTAS À PAGAR')
        GROUP BY
            principal, subprincipal, conta, Fonte, vrecurso, recurso
        ORDER BY
            CAST(SUBSTRING(vrecurso, 1, 1) AS INTEGER), vrecurso
    """.trimIndent()
        )

        // 4. viewmovrd
        db.execSQL("DROP VIEW IF EXISTS viewmovrd")
        Log.d("DatabaseHelper", "Recriando viewmovrd")
        db.execSQL(
            """
        CREATE VIEW viewmovrd AS
        SELECT 
            tbmovimento.idMov AS idMov,
            tbmovimento.recurso AS recurso,
            tbmovimento.vrecurso AS vrecurso,
            tbmovimento.clifor AS clifor,
            tbmovimento.vCliFor AS vCliFor,
            tbmovimento.dtlancto AS dtlancto,
            tbmovimento.dtEmi AS dtEmi,
            tbmovimento.dtVcto AS dtVcto,
            tbmovimento.documento AS documento,
            tbmovimento.classif AS classif,
            tbmovimento.Descr AS Descr,
            tbmovimento.Valor AS Valor,
            tbmovimento.dtApr AS dtApr,
            tbmovimento.statusMov AS statusMov,
            tbmovimento.Prev AS Prev,
            gpprincipal.nome_P AS nome_P,
            gpprincipal.nome_S AS nome_S,
            gpprincipal.nome_C AS nome_C
        FROM tbmovimento
        JOIN gpprincipal ON tbmovimento.classif = gpprincipal.cod_Geral
        WHERE 
            tbmovimento.recurso IN ('0022', '0079', '0080')
            AND tbmovimento.classif < '9.000.000'
            AND tbmovimento.classif > '3.000.000'
    """.trimIndent()
        )
    }

    // Função auxiliar para mapear Lancamento
    private fun mapLancamento(cursor: android.database.Cursor): Lancamento {
        return Lancamento(
            idMov = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_MOV)),
            recurso = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECURSO)) ?: "",
            vrecurso = cursor.getString(cursor.getColumnIndexOrThrow(COL_VRECURSO)) ?: "",
            clifor = cursor.getString(cursor.getColumnIndexOrThrow(COL_CLIFOR)) ?: "",
            vCliFor = cursor.getString(cursor.getColumnIndexOrThrow(COL_VCLIFOR)) ?: "",
            dtLancto = cursor.getString(cursor.getColumnIndexOrThrow(COL_DTLANCTO)) ?: "",
            dtEmi = cursor.getString(cursor.getColumnIndexOrThrow(COL_DT_EMI)) ?: "",
            dtVcto = cursor.getString(cursor.getColumnIndexOrThrow(COL_DT_VCTO)) ?: "",
            documento = cursor.getString(cursor.getColumnIndexOrThrow(COL_DOCUMENTO)),
            classif = cursor.getString(cursor.getColumnIndexOrThrow(COL_CLASSIF)) ?: "",
            Descr = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCR)) ?: "",
            Valor = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_VALOR)),
            dtApr = cursor.getString(cursor.getColumnIndexOrThrow(COL_DT_APR)),
            statusMov = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS_MOV)) ?: "",
            Prev = cursor.getString(cursor.getColumnIndexOrThrow(COL_PREV)) ?: "V"
        )
    }

    // Métodos para usu
    fun checkUser(usuario: String, senha: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USUARIO,
            arrayOf(COL_SENHA),
            "$COL_USUARIO = ?",
            arrayOf(usuario),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val storedPassword = it.getString(it.getColumnIndexOrThrow(COL_SENHA))
                return BCrypt.checkpw(senha, storedPassword)
            }
        }
        return false
    }

    fun getUsuario(usuario: String): Usuario? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USUARIO,
            null,
            "$COL_USUARIO = ?",
            arrayOf(usuario),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return Usuario(
                    id = it.getLongOrNull(it.getColumnIndexOrThrow(COL_ID_USUARIO)),
                    usuario = it.getString(it.getColumnIndexOrThrow(COL_USUARIO)) ?: "",
                    senha = it.getString(it.getColumnIndexOrThrow(COL_SENHA)) ?: ""
                )
            }
        }
        return null
    }

    fun getAllUsuarios(): List<Usuario> {
        val usuarios = mutableListOf<Usuario>()
        val db = readableDatabase
        val cursor = db.query(TABLE_USUARIO, null, null, null, null, null, "$COL_USUARIO ASC")
        cursor.use {
            while (it.moveToNext()) {
                val usuario = Usuario(
                    id = it.getLongOrNull(it.getColumnIndexOrThrow(COL_ID_USUARIO)),
                    usuario = it.getString(it.getColumnIndexOrThrow(COL_USUARIO)) ?: "",
                    senha = it.getString(it.getColumnIndexOrThrow(COL_SENHA)) ?: ""
                )
                usuarios.add(usuario)
            }
        }
        return usuarios
    }

    fun insertUsuario(usuario: String, senha: String): Long {
        val db = writableDatabase
        val hashedPassword = BCrypt.hashpw(senha, BCrypt.gensalt())
        val values = ContentValues().apply {
            put(COL_USUARIO, usuario)
            put(COL_SENHA, hashedPassword)
        }
        return db.insert(TABLE_USUARIO, null, values)
    }

    fun updateUsuario(id: Long, usuario: String, senha: String): Int {
        val db = writableDatabase
        val hashedPassword = BCrypt.hashpw(senha, BCrypt.gensalt())
        val values = ContentValues().apply {
            put(COL_USUARIO, usuario)
            put(COL_SENHA, hashedPassword)
        }
        return db.update(TABLE_USUARIO, values, "$COL_ID_USUARIO = ?", arrayOf(id.toString()))
    }

    fun deleteUsuario(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_USUARIO, "$COL_ID_USUARIO = ?", arrayOf(id.toString()))
    }

    // Métodos para tbrecursos
    fun insertRecurso(recurso: Recurso): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CODIGO, recurso.codigo)
            put(COL_NOMEBCO, recurso.nomebco)
            put(COL_AGENCIA, recurso.agencia)
            put(COL_FLUXO, recurso.fluxo)
            put(COL_LIMITE, recurso.limite)
            put(COL_ABERTURA, recurso.abertura)
            put(COL_ENCERRAMENTO, recurso.encerramento)
            put(COL_STATUS, recurso.status)
            put(COL_FK_GPPRINC, recurso.fk_gpprinc)
        }
        val id = db.insert(TABLE_RECURSOS, null, values)
        cachedRecursos = null // Invalida o cache
        return id
    }

    fun updateRecurso(codigo: String, recurso: Recurso): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NOMEBCO, recurso.nomebco)
            put(COL_AGENCIA, recurso.agencia)
            put(COL_FLUXO, recurso.fluxo)
            put(COL_LIMITE, recurso.limite)
            put(COL_ABERTURA, recurso.abertura)
            put(COL_ENCERRAMENTO, recurso.encerramento)
            put(COL_STATUS, recurso.status)
            put(COL_FK_GPPRINC, recurso.fk_gpprinc)
        }
        val rows = db.update(TABLE_RECURSOS, values, "$COL_CODIGO = ?", arrayOf(codigo))
        cachedRecursos = null // Invalida o cache
        return rows
    }

    fun deleteRecurso(codigo: String): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_RECURSOS, "$COL_CODIGO = ?", arrayOf(codigo))
        cachedRecursos = null // Invalida o cache
        return rows
    }

    fun getRecursos(): List<Recurso> {
        cachedRecursos?.let { return it }
        val recursos = mutableListOf<Recurso>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT codigo, nomebco, fk_gpprinc, agencia, fluxo, limite, abertura, encerramento, status " +
                    "FROM tbrecursos ORDER BY nomebco",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                recursos.add(
                    Recurso(
                        codigo = it.getString(it.getColumnIndexOrThrow("codigo")) ?: "",
                        nomebco = it.getString(it.getColumnIndexOrThrow("nomebco")) ?: "",
                        fk_gpprinc = it.getString(it.getColumnIndexOrThrow("fk_gpprinc")) ?: "",
                        agencia = it.getString(it.getColumnIndexOrThrow("agencia")),
                        fluxo = it.getString(it.getColumnIndexOrThrow("fluxo")),
                        limite = if (it.isNull(it.getColumnIndexOrThrow("limite"))) null else it.getDouble(
                            it.getColumnIndexOrThrow("limite")
                        ),
                        abertura = it.getString(it.getColumnIndexOrThrow("abertura")) ?: "",
                        encerramento = it.getString(it.getColumnIndexOrThrow("encerramento"))
                            ?: "",
                        status = it.getString(it.getColumnIndexOrThrow("status")) ?: ""
                    )
                )
            }
        }
        cachedRecursos = recursos
        return recursos
    }

    fun getAllRecursos(): List<Recurso> {
        return getRecursos() // Reutiliza getRecursos com cache
    }

    fun getRecursosAtivos(): List<Recurso> {
        val recursos = mutableListOf<Recurso>()
        val db = readableDatabase
        val query = """
        SELECT * FROM $TABLE_RECURSOS
        WHERE $COL_STATUS = 'A' AND ($COL_ENCERRAMENTO IS NULL OR $COL_ENCERRAMENTO = '' OR $COL_ENCERRAMENTO > date('now'))
        ORDER BY $COL_NOMEBCO ASC
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                recursos.add(
                    Recurso(
                        codigo = it.getString(it.getColumnIndexOrThrow(COL_CODIGO)) ?: "",
                        nomebco = it.getString(it.getColumnIndexOrThrow(COL_NOMEBCO)) ?: "",
                        agencia = it.getString(it.getColumnIndexOrThrow(COL_AGENCIA)),
                        fluxo = it.getString(it.getColumnIndexOrThrow(COL_FLUXO)),
                        limite = if (it.isNull(it.getColumnIndexOrThrow(COL_LIMITE))) null else it.getDouble(
                            it.getColumnIndexOrThrow(COL_LIMITE)
                        ),
                        abertura = it.getString(it.getColumnIndexOrThrow(COL_ABERTURA)) ?: "",
                        encerramento = it.getString(it.getColumnIndexOrThrow(COL_ENCERRAMENTO))
                            ?: "",
                        status = it.getString(it.getColumnIndexOrThrow(COL_STATUS)) ?: "",
                        fk_gpprinc = it.getString(it.getColumnIndexOrThrow(COL_FK_GPPRINC))
                            ?: ""
                    )
                )
            }
        }
        return recursos
    }

    fun isRecursoCartao(recursoCodigo: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_RECURSOS,
            arrayOf(COL_FK_GPPRINC),
            "$COL_CODIGO = ?",
            arrayOf(recursoCodigo),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(COL_FK_GPPRINC)) == "2.001.003"
            }
        }
        return false
    }

    // Métodos para Baixar Cartões
    fun atualizarBaixaCartao(
        recurso: String,
        dataInicio: String,
        dataFim: String,
        dataBaixa: String
    ): Int {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put(COL_DT_APR, dataBaixa)
                put(COL_STATUS_MOV, "PG")
            }
            val rowsUpdated = db.update(
                TABLE_LANCAMENTOS,
                values,
                "$COL_RECURSO = ? AND $COL_DT_VCTO BETWEEN ? AND ? AND $COL_DT_APR IS NULL AND $COL_STATUS_MOV = ''",
                arrayOf(recurso, dataInicio, dataFim)
            )
            db.setTransactionSuccessful()
            return rowsUpdated
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro ao atualizar baixa de cartão: ${e.message}")
            return 0
        } finally {
            db.endTransaction()
        }
    }

    // Métodos para tbmovimento
    fun insertLancamento(lancamento: Lancamento): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_RECURSO, lancamento.recurso)
            put(COL_VRECURSO, lancamento.vrecurso)
            put(COL_CLIFOR, lancamento.clifor)
            put(COL_VCLIFOR, lancamento.vCliFor)
            put(COL_DTLANCTO, lancamento.dtLancto)
            put(COL_DT_EMI, lancamento.dtEmi)
            put(COL_DT_VCTO, lancamento.dtVcto)
            put(COL_DOCUMENTO, lancamento.documento)
            put(COL_CLASSIF, lancamento.classif)
            put(COL_DESCR, lancamento.Descr)
            put(COL_VALOR, lancamento.Valor)
            put(COL_DT_APR, lancamento.dtApr)
            put(COL_STATUS_MOV, lancamento.statusMov)
            put(COL_PREV, lancamento.Prev)
        }
        return try {
            val result = db.insertOrThrow(TABLE_LANCAMENTOS, null, values)
            if (result == -1L) {
                Log.e("INSERT_ERROR", "Falha ao inserir lançamento: $lancamento")
            }
            result
        } catch (e: SQLiteException) {
            Log.e("INSERT_ERROR", "Erro ao inserir lançamento: ${e.message}, Dados: $lancamento")
            -1L
        }
    }

    fun updateLancamento(idMov: Int, lancamento: Lancamento): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_RECURSO, lancamento.recurso)
            put(COL_VRECURSO, lancamento.vrecurso)
            put(COL_CLIFOR, lancamento.clifor)
            put(COL_VCLIFOR, lancamento.vCliFor)
            put(COL_DTLANCTO, lancamento.dtLancto)
            put(COL_DT_EMI, lancamento.dtEmi)
            put(COL_DT_VCTO, lancamento.dtVcto)
            put(COL_DOCUMENTO, lancamento.documento)
            put(COL_CLASSIF, lancamento.classif)
            put(COL_DESCR, lancamento.Descr)
            put(COL_VALOR, lancamento.Valor)
            put(COL_DT_APR, lancamento.dtApr)
            put(COL_STATUS_MOV, lancamento.statusMov)
            put(COL_PREV, lancamento.Prev)
        }
        return db.update(
            TABLE_LANCAMENTOS,
            values,
            "$COL_ID_MOV = ?",
            arrayOf(idMov.toString())
        )
    }

    fun deleteLancamento(idMov: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_LANCAMENTOS, "$COL_ID_MOV = ?", arrayOf(idMov.toString()))
    }

    fun pagarLancamento(
        idMov: Int,
        dataPagamento: String,
        valorPago: Double,
        recursoUtilizado: String,
        fkGpPrinc: String,
        desconto: Double = 0.0,
        juros: Double = 0.0
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Verificar o lançamento original
            val lancamento = getLancamentoById(idMov) ?: return false
            if (lancamento.dtApr != null || lancamento.statusMov.isNotEmpty()) {
                return false
            }

            // Criar registro de entrada no Contas a Pagar (baixa)
            val baixaDescr = "Baixa de: ${lancamento.Descr} -PG- $idMov"
            val entradaContentValues = ContentValues().apply {
                put(COL_RECURSO, lancamento.recurso)
                put(COL_VRECURSO, lancamento.vrecurso)
                put(COL_CLIFOR, lancamento.clifor)
                put(COL_VCLIFOR, lancamento.vCliFor)
                put(COL_DTLANCTO, dataPagamento)
                put(COL_DT_EMI, dataPagamento)
                put(COL_DT_VCTO, lancamento.dtVcto)
                put(COL_DOCUMENTO, lancamento.documento)
                put(COL_CLASSIF, "9.001.003")
                put(COL_DESCR, baixaDescr)
                put(COL_VALOR, valorPago + desconto - juros)
                put(COL_DT_APR, dataPagamento)
                put(COL_STATUS_MOV, "PG")
                put(COL_PREV, lancamento.Prev)
            }
            val entradaId = db.insertOrThrow(TABLE_LANCAMENTOS, null, entradaContentValues)
            if (entradaId == -1L) return false

            // Criar registro de saída no recurso de pagamento
            val pagamentoDescr = "Pagto. de: ${lancamento.Descr} -PG- $idMov"
            val saidaContentValues = ContentValues().apply {
                put(COL_RECURSO, recursoUtilizado)
                put(COL_VRECURSO, fkGpPrinc)
                put(COL_CLIFOR, lancamento.clifor)
                put(COL_VCLIFOR, lancamento.vCliFor)
                put(COL_DTLANCTO, dataPagamento)
                put(COL_DT_EMI, dataPagamento)
                put(COL_DT_VCTO, lancamento.dtVcto)
                put(COL_DOCUMENTO, lancamento.documento)
                put(COL_CLASSIF, "9.001.003")
                put(COL_DESCR, pagamentoDescr)
                put(COL_VALOR, -valorPago)
                put(COL_DT_APR, dataPagamento)
                put(COL_STATUS_MOV, "PG")
                put(COL_PREV, lancamento.Prev)
            }
            val saidaId = db.insertOrThrow(TABLE_LANCAMENTOS, null, saidaContentValues)
            if (saidaId == -1L) return false

            // Atualizar o lançamento original
            val updateContentValues = ContentValues().apply {
                put(COL_DT_APR, dataPagamento)
                put(COL_STATUS_MOV, "PG")
            }
            val rowsUpdated = db.update(
                TABLE_LANCAMENTOS,
                updateContentValues,
                "$COL_ID_MOV = ?",
                arrayOf(idMov.toString())
            )
            if (rowsUpdated != 1) return false

            db.setTransactionSuccessful()
            return true
        } catch (e: SQLiteException) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun receberLancamento(
        idMov: Int,
        dataRecebimento: String,
        valorRecebido: Double,
        recursoUtilizado: String,
        fkGpPrinc: String,
        desconto: Double = 0.0,
        juros: Double = 0.0
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Verificar o lançamento original
            val lancamento = getLancamentoById(idMov) ?: return false
            //if (lancamento.dtApr != null || lancamento.Prev != "V") return false
            if (lancamento.dtApr != null || lancamento.statusMov.isNotEmpty()) {
                return false
            }
            // Criar registro de saída no Contas a Receber (baixa)
            val baixaDescr = "Baixa de: ${lancamento.Descr} -RC- $idMov"
            val saidaContentValues = ContentValues().apply {
                put(COL_RECURSO, lancamento.recurso)
                put(COL_VRECURSO, lancamento.vrecurso)
                put(COL_CLIFOR, lancamento.clifor)
                put(COL_VCLIFOR, lancamento.vCliFor)
                put(COL_DTLANCTO, dataRecebimento)
                put(COL_DT_EMI, dataRecebimento)
                put(COL_DT_VCTO, lancamento.dtVcto)
                put(COL_DOCUMENTO, lancamento.documento)
                put(COL_CLASSIF, "9.001.003")
                put(COL_DESCR, baixaDescr)
                put(COL_VALOR, -(valorRecebido + desconto - juros))
                put(COL_DT_APR, dataRecebimento)
                put(COL_STATUS_MOV, "RC")
                put(COL_PREV, lancamento.Prev)
            }
            val saidaId = db.insertOrThrow(TABLE_LANCAMENTOS, null, saidaContentValues)
            if (saidaId == -1L) return false

            // Criar registro de entrada no recurso de recebimento
            val recebimentoDescr = "Recto.de ${lancamento.Descr} -RC- $idMov"
            val entradaContentValues = ContentValues().apply {
                put(COL_RECURSO, recursoUtilizado)
                put(COL_VRECURSO, fkGpPrinc)
                put(COL_CLIFOR, lancamento.clifor)
                put(COL_VCLIFOR, lancamento.vCliFor)
                put(COL_DTLANCTO, dataRecebimento)
                put(COL_DT_EMI, dataRecebimento)
                put(COL_DT_VCTO, lancamento.dtVcto)
                put(COL_DOCUMENTO, lancamento.documento)
                put(COL_CLASSIF, "9.001.003")
                put(COL_DESCR, recebimentoDescr)
                put(COL_VALOR, valorRecebido)
                put(COL_DT_APR, dataRecebimento)
                put(COL_STATUS_MOV, "RC")
                put(COL_PREV, lancamento.Prev)
            }
            val entradaId = db.insertOrThrow(TABLE_LANCAMENTOS, null, entradaContentValues)
            if (entradaId == -1L) return false

            // Atualizar o lançamento original
            val updateContentValues = ContentValues().apply {
                put(COL_DT_APR, dataRecebimento)
                put(COL_STATUS_MOV, "RC")
            }
            val rowsUpdated = db.update(
                TABLE_LANCAMENTOS,
                updateContentValues,
                "$COL_ID_MOV = ?",
                arrayOf(idMov.toString())
            )
            if (rowsUpdated != 1) return false

            db.setTransactionSuccessful()
            return true
        } catch (e: SQLiteException) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun salvarCompraCartao(
        recurso: String,
        vrecurso: String,
        clifor: String,
        vCliFor: String,
        dtlancto: String,
        dtEmi: String,
        dtVcto: String,
        documento: String,
        classif: String,
        descr: String,
        valor: Double,
        cartaoRecurso: String,
        cartaoVrecurso: String
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Lançamento inicial no Contas a Pagar
            val lancamentoValues = ContentValues().apply {
                put(COL_RECURSO, recurso)
                // put(COL_VRECURSO, vrecurso)
                put(COL_VRECURSO, "2.001.003")
                put(COL_CLIFOR, clifor)
                put(COL_VCLIFOR, vCliFor)
                put(COL_DTLANCTO, dtlancto)
                put(COL_DT_EMI, dtEmi)
                put(COL_DT_VCTO, dtVcto)
                put(COL_DOCUMENTO, documento)
                put(COL_CLASSIF, classif)
                put(COL_DESCR, descr)
                put(COL_VALOR, -valor)
                put(COL_DT_APR, dtVcto)
                put(COL_STATUS_MOV, "PG")
                put(COL_PREV, "V")
            }
            val idMov = db.insertOrThrow(TABLE_LANCAMENTOS, null, lancamentoValues).toInt()
            if (idMov.toLong() == -1L) return false

            // Baixa do Contas a Pagar
            val baixaDescr = "Baixa de: $descr -PG- $idMov"
            val baixaValues = ContentValues().apply {
                put(COL_RECURSO, recurso)
                // put(COL_VRECURSO, vrecurso)
                put(COL_VRECURSO, "2.001.003")
                put(COL_CLIFOR, clifor)
                put(COL_VCLIFOR, vCliFor)
                put(COL_DTLANCTO, dtlancto)
                put(COL_DT_EMI, dtEmi)
                put(COL_DT_VCTO, dtVcto)
                put(COL_DOCUMENTO, documento)
                put(COL_CLASSIF, "9.001.003")
                put(COL_DESCR, baixaDescr)
                put(COL_VALOR, valor)
                put(COL_DT_APR, dtVcto)
                put(COL_STATUS_MOV, "PG")
                put(COL_PREV, "V")
            }
            val baixaId = db.insertOrThrow(TABLE_LANCAMENTOS, null, baixaValues)
            if (baixaId == -1L) return false

            // Lançamento pendente no cartão
            val cartaoDescr = "Pagto. de: $descr -PG- $idMov"
            val cartaoValues = ContentValues().apply {
                put(COL_RECURSO, cartaoRecurso)
                put(COL_VRECURSO, cartaoVrecurso)
                put(COL_CLIFOR, clifor)
                put(COL_VCLIFOR, vCliFor)
                put(COL_DTLANCTO, dtlancto)
                put(COL_DT_EMI, dtEmi)
                put(COL_DT_VCTO, dtVcto)
                put(COL_DOCUMENTO, documento)
                put(COL_CLASSIF, "9.001.003")
                put(COL_DESCR, cartaoDescr)
                put(COL_VALOR, -valor)
                putNull(COL_DT_APR)
                put(COL_STATUS_MOV, "")
                put(COL_PREV, "V")
            }
            val cartaoId = db.insertOrThrow(TABLE_LANCAMENTOS, null, cartaoValues)
            if (cartaoId == -1L) return false

            db.setTransactionSuccessful()
            return true
        } catch (e: SQLiteException) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun receberLancamentoParcial(
        idMov: Int,
        dataRecebimento: String,
        valorRecebido: Double,
        valorRestante: Double,
        recursoUtilizado: String,
        fkGpPrinc: String,
        juros: Double
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Verificar o lançamento original
            val lancamento = getLancamentoById(idMov) ?: return false
            if (lancamento.dtApr != null || lancamento.Prev != "V") return false

            // Criar novo lançamento com o valor restante
            val novoLancamentoValues = ContentValues().apply {
                put(COL_RECURSO, lancamento.recurso)
                put(COL_VRECURSO, lancamento.vrecurso)
                put(COL_CLIFOR, lancamento.clifor)
                put(COL_VCLIFOR, lancamento.vCliFor)
                put(COL_DTLANCTO, lancamento.dtLancto)
                put(COL_DT_EMI, lancamento.dtEmi)
                put(COL_DT_VCTO, lancamento.dtVcto)
                put(COL_DOCUMENTO, lancamento.documento)
                put(COL_CLASSIF, lancamento.classif)
                put(COL_DESCR, lancamento.Descr)
                put(COL_VALOR, valorRestante)
                putNull(COL_DT_APR)
                put(COL_STATUS_MOV, "")
                put(COL_PREV, "V")
            }
            val novoId = db.insertOrThrow(TABLE_LANCAMENTOS, null, novoLancamentoValues)
            if (novoId == -1L) return false

            // Registrar a baixa parcial
            val baixaDescr = "Baixa de: ${lancamento.Descr} -RC- $idMov"
            val baixaValues = ContentValues().apply {
                put(COL_RECURSO, lancamento.recurso)
                put(COL_VRECURSO, lancamento.vrecurso)
                put(COL_CLIFOR, lancamento.clifor)
                put(COL_VCLIFOR, lancamento.vCliFor)
                put(COL_DTLANCTO, dataRecebimento)
                put(COL_DT_EMI, dataRecebimento)
                put(COL_DT_VCTO, lancamento.dtVcto)
                put(COL_DOCUMENTO, lancamento.documento)
                put(COL_CLASSIF, "9.001.003")
                put(COL_DESCR, baixaDescr)
                put(COL_VALOR, -(valorRecebido - juros))
                put(COL_DT_APR, dataRecebimento)
                put(COL_STATUS_MOV, "RC")
                put(COL_PREV, "V")
            }
            val baixaId = db.insertOrThrow(TABLE_LANCAMENTOS, null, baixaValues)
            if (baixaId == -1L) return false

            // Registrar a entrada no recurso utilizado
            val recebimentoDescr = "Recto.de ${lancamento.Descr} -RC- $idMov"
            val recebimentoValues = ContentValues().apply {
                put(COL_RECURSO, recursoUtilizado)
                put(COL_VRECURSO, fkGpPrinc)
                put(COL_CLIFOR, lancamento.clifor)
                put(COL_VCLIFOR, lancamento.vCliFor)
                put(COL_DTLANCTO, dataRecebimento)
                put(COL_DT_EMI, dataRecebimento)
                put(COL_DT_VCTO, lancamento.dtVcto)
                put(COL_DOCUMENTO, lancamento.documento)
                put(COL_CLASSIF, "9.001.003")
                put(COL_DESCR, recebimentoDescr)
                put(COL_VALOR, valorRecebido)
                put(COL_DT_APR, dataRecebimento)
                put(COL_STATUS_MOV, "RC")
                put(COL_PREV, "V")
            }
            val entradaId = db.insertOrThrow(TABLE_LANCAMENTOS, null, recebimentoValues)
            if (entradaId == -1L) return false

            // Excluir o lançamento original
            val rowsDeleted =
                db.delete(TABLE_LANCAMENTOS, "$COL_ID_MOV = ?", arrayOf(idMov.toString()))
            if (rowsDeleted != 1) return false

            db.setTransactionSuccessful()
            return true
        } catch (e: SQLiteException) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun getRecursoByCodigo(codigo: String): Recurso? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_RECURSOS,
            null,
            "$COL_CODIGO = ?",
            arrayOf(codigo),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                Recurso(
                    codigo = it.getString(it.getColumnIndexOrThrow(COL_CODIGO)) ?: "",
                    nomebco = it.getString(it.getColumnIndexOrThrow(COL_NOMEBCO)) ?: "",
                    agencia = it.getString(it.getColumnIndexOrThrow(COL_AGENCIA)),
                    fluxo = it.getString(it.getColumnIndexOrThrow(COL_FLUXO)),
                    limite = if (it.isNull(it.getColumnIndexOrThrow(COL_LIMITE))) null else it.getDouble(
                        it.getColumnIndexOrThrow(COL_LIMITE)
                    ),
                    abertura = it.getString(it.getColumnIndexOrThrow(COL_ABERTURA)) ?: "",
                    encerramento = it.getString(it.getColumnIndexOrThrow(COL_ENCERRAMENTO))
                        ?: "",
                    status = it.getString(it.getColumnIndexOrThrow(COL_STATUS)) ?: "",
                    fk_gpprinc = it.getString(it.getColumnIndexOrThrow(COL_FK_GPPRINC)) ?: ""
                )
            } else {
                null
            }
        }
    }

    fun getLancamentoById(idMov: Int): Lancamento? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LANCAMENTOS,
            null,
            "$COL_ID_MOV = ?",
            arrayOf(idMov.toString()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) mapLancamento(it)
            else null
        }
    }

    fun receberLancamento(idMov: Int, dataAtual: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_DT_APR, dataAtual)
            put(COL_STATUS_MOV, "RC")
        }
        return db.update(
            TABLE_LANCAMENTOS,
            values,
            "$COL_ID_MOV = ?",
            arrayOf(idMov.toString())
        )
    }

    fun getAllLancamentos(debug: Boolean = false): List<Lancamento> {
        val lancamentos = mutableListOf<Lancamento>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_LANCAMENTOS ORDER BY $COL_DTLANCTO DESC LIMIT 50",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val lancamento = mapLancamento(it)
                lancamentos.add(lancamento)
                if (debug) {
                    Log.d(
                        "DatabaseHelper",
                        "Lançamento: ID=${lancamento.idMov}, Descr=${lancamento.Descr}, Valor=${lancamento.Valor}, dtVcto=${lancamento.dtVcto}, dtApr=${lancamento.dtApr}"
                    )
                }
            }
        }
        return lancamentos
    }

    fun getLancamentosPendentes(sixtyDaysAgo: String): List<Lancamento> {
        val lancamentos = mutableListOf<Lancamento>()
        val db = readableDatabase
        val query = """
        SELECT * FROM $TABLE_LANCAMENTOS
        WHERE $COL_DT_APR IS NULL AND DATE($COL_DT_VCTO) >= ?
        ORDER BY $COL_DT_VCTO ASC
        LIMIT 50
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(sixtyDaysAgo))
        cursor.use {
            while (it.moveToNext()) {
                lancamentos.add(mapLancamento(it))
            }
        }
        return lancamentos
    }

    fun getLancamentosPendentesWithOffset(
        startDate: String,
        offset: Int,
        pageSize: Int
    ): List<Lancamento> {
        val lancamentos = mutableListOf<Lancamento>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT * FROM $TABLE_LANCAMENTOS
        WHERE $COL_DT_APR IS NULL AND $COL_DT_VCTO >= ?
        ORDER BY $COL_DT_VCTO ASC, $COL_ID_MOV DESC
        LIMIT ? OFFSET ?
        """,
            arrayOf(startDate, pageSize.toString(), offset.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                lancamentos.add(mapLancamento(it))
            }
        }
        return lancamentos
    }

    fun getLancamentosByRecurso(
        recurso: String,
        startDate: String,
        offset: Int,
        pageSize: Int
    ): Pair<List<Lancamento>, Double> {
        val lancamentos = mutableListOf<Lancamento>()
        var saldoInicial = 0.0
        val db = readableDatabase
        db.beginTransactionNonExclusive()
        try {
            // Calcular saldo inicial
            val saldoCursor = db.rawQuery(
                """
                SELECT SUM($COL_VALOR) as saldo
                FROM $TABLE_LANCAMENTOS
                WHERE $COL_RECURSO = ? AND $COL_DTLANCTO < ?
                """,
                arrayOf(recurso, startDate)
            )
            saldoCursor.use {
                if (it.moveToFirst()) {
                    saldoInicial = it.getDouble(it.getColumnIndexOrThrow("saldo"))
                }
            }

            // Buscar lançamentos
            val cursor = db.rawQuery(
                """
                SELECT * FROM $TABLE_LANCAMENTOS
                WHERE $COL_RECURSO = ? AND $COL_DTLANCTO >= ?
                ORDER BY $COL_DT_VCTO ASC, $COL_ID_MOV DESC
                LIMIT ? OFFSET ?
                """,
                arrayOf(recurso, startDate, pageSize.toString(), offset.toString())
            )
            cursor.use {
                while (it.moveToNext()) {
                    lancamentos.add(mapLancamento(it))
                }
            }
            db.setTransactionSuccessful()
            return Pair(lancamentos, saldoInicial)
        } finally {
            db.endTransaction()
        }
    }

    fun getLancamentosByClifor(
        codCliFor: String,
        offset: Int,
        pageSize: Int = 50 // Limite padrão de 50 registros
    ): Pair<List<Lancamento>, Double> {
        val lancamentos = mutableListOf<Lancamento>()
        var saldoInicial = 0.0
        val db = readableDatabase
        db.beginTransactionNonExclusive()
        try {
            // Calcular saldo inicial
            val saldoCursor = db.rawQuery(
                """
                SELECT SUM($COL_VALOR) as saldo
                FROM $TABLE_LANCAMENTOS
                WHERE $COL_CLIFOR = ?
                AND $COL_ID_MOV NOT IN (
                    SELECT $COL_ID_MOV FROM $TABLE_LANCAMENTOS
                    WHERE $COL_CLIFOR = ?
                    ORDER BY $COL_DTLANCTO DESC, $COL_ID_MOV DESC
                    LIMIT ? OFFSET ?
                )
                """,
                arrayOf(codCliFor, codCliFor, pageSize.toString(), offset.toString())
            )
            saldoCursor.use {
                if (it.moveToFirst()) {
                    saldoInicial = it.getDouble(it.getColumnIndexOrThrow("saldo"))
                }
            }

            // Buscar lançamentos com limite
            val cursor = db.rawQuery(
                """
                SELECT * FROM $TABLE_LANCAMENTOS
                WHERE $COL_CLIFOR = ?
                ORDER BY $COL_DTLANCTO DESC, $COL_ID_MOV DESC
                LIMIT ? OFFSET ?
                """,
                arrayOf(codCliFor, pageSize.toString(), offset.toString())
            )
            cursor.use {
                while (it.moveToNext()) {
                    lancamentos.add(mapLancamento(it))
                }
            }
            db.setTransactionSuccessful()
            return Pair(lancamentos, saldoInicial)
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro ao buscar lançamentos por clifor: ${e.message}")
            return Pair(emptyList(), 0.0)
        } finally {
            db.endTransaction()
        }
    }

    fun getLancamentosCartao(offset: Int, pageSize: Int = 50): Pair<List<Lancamento>, Double> {
        val lancamentos = mutableListOf<Lancamento>()
        var saldoInicial = 0.0
        val db = readableDatabase
        db.beginTransactionNonExclusive()
        try {
            // Calcular saldo inicial
            val saldoCursor = db.rawQuery(
                """
                SELECT SUM($COL_VALOR) as saldo
                FROM $TABLE_LANCAMENTOS
                WHERE $COL_VRECURSO = '2.001.003'
                AND $COL_ID_MOV NOT IN (
                    SELECT $COL_ID_MOV FROM $TABLE_LANCAMENTOS
                    WHERE $COL_VRECURSO = '2.001.003'
                    ORDER BY $COL_DTLANCTO DESC, $COL_ID_MOV DESC
                    LIMIT ? OFFSET ?
                )
                """,
                arrayOf(pageSize.toString(), offset.toString())
            )
            saldoCursor.use {
                if (it.moveToFirst()) {
                    saldoInicial = it.getDouble(it.getColumnIndexOrThrow("saldo"))
                }
            }

            // Buscar lançamentos com limite
            val cursor = db.rawQuery(
                """
                SELECT * FROM $TABLE_LANCAMENTOS
                WHERE $COL_VRECURSO = '2.001.003'
                ORDER BY $COL_DTLANCTO DESC, $COL_ID_MOV DESC
                LIMIT ? OFFSET ?
                """,
                arrayOf(pageSize.toString(), offset.toString())
            )
            cursor.use {
                while (it.moveToNext()) {
                    lancamentos.add(mapLancamento(it))
                }
            }
            db.setTransactionSuccessful()
            return Pair(lancamentos, saldoInicial)
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro ao buscar lançamentos de cartão: ${e.message}")
            return Pair(emptyList(), 0.0)
        } finally {
            db.endTransaction()
        }
    }

    fun getLancamentosFiltered(
        dtLancto: String?,
        dtEmi: String?,
        recurso: String?,
        clifor: String?,
        isRecursoCartao: Boolean,
        offset: Int,
        pageSize: Int = 50
    ): Pair<List<Lancamento>, Double> {
        val lancamentos = mutableListOf<Lancamento>()
        val db = readableDatabase
        
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()
        
        // 1. Recurso and Card filters
        if (!recurso.isNullOrEmpty()) {
            if (recurso == "0401") {
                conditions.add("$COL_CLIFOR = ?")
                args.add("0022")
            } else if (recurso == "0402") {
                conditions.add("$COL_CLIFOR = ?")
                args.add("0079")
            } else if (isRecursoCartao) {
                conditions.add("$COL_VRECURSO = '2.001.003'")
            } else {
                conditions.add("$COL_RECURSO = ?")
                args.add(recurso)
            }
        }
        
        // 2. Clifor filter
        if (!clifor.isNullOrEmpty()) {
            conditions.add("$COL_CLIFOR = ?")
            args.add(clifor)
        }
        
        // 3. Date filters (OR logic between dtLancto and dtEmi if both are filled)
        val dateConditions = mutableListOf<String>()
        if (!dtLancto.isNullOrEmpty()) {
            dateConditions.add("$COL_DTLANCTO = ?")
            args.add(dtLancto)
        }
        if (!dtEmi.isNullOrEmpty()) {
            dateConditions.add("$COL_DT_EMI = ?")
            args.add(dtEmi)
        }
        
        if (dateConditions.isNotEmpty()) {
            val datesClause = "(" + dateConditions.joinToString(" OR ") + ")"
            conditions.add(datesClause)
        }
        
        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE " + conditions.joinToString(" AND ")
        } else {
            ""
        }
        
        db.beginTransactionNonExclusive()
        try {
            val query = """
                SELECT * FROM $TABLE_LANCAMENTOS
                $whereClause
                ORDER BY $COL_DT_VCTO ASC, $COL_ID_MOV DESC
                LIMIT ? OFFSET ?
            """.trimIndent()
            
            val queryArgs = args.toMutableList().apply {
                add(pageSize.toString())
                add(offset.toString())
            }.toTypedArray()
            
            val cursor = db.rawQuery(query, queryArgs)
            cursor.use {
                while (it.moveToNext()) {
                    lancamentos.add(mapLancamento(it))
                }
            }
            
            db.setTransactionSuccessful()
            return Pair(lancamentos, 0.0)
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro ao filtrar lançamentos: ${e.message}")
            return Pair(emptyList(), 0.0)
        } finally {
            db.endTransaction()
        }
    }

    fun pagarLancamentoParcial(
        idMov: Int,
        dataPagamento: String,
        valorPago: Double,
        valorRestante: Double,
        recursoUtilizado: String,
        fkGpPrinc: String,
        juros: Double = 0.0
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val lancamentoOriginal = getLancamentoById(idMov) ?: return false
            if (lancamentoOriginal.dtApr != null || lancamentoOriginal.Prev != "V") return false
            if (valorPago <= 0 || valorRestante <= 0 || juros < 0 || juros > valorPago) return false

            // Atualizar o registro original com o valor pago
            val values = ContentValues().apply {
                put(COL_DT_APR, dataPagamento)
                put(COL_STATUS_MOV, "PG")
                put(COL_VALOR, -valorPago)
            }
            val rows =
                db.update(
                    TABLE_LANCAMENTOS,
                    values,
                    "$COL_ID_MOV = ?",
                    arrayOf(idMov.toString())
                )
            if (rows <= 0) return false

            // Criar novo registro para o valor restante
            val lancamentoRestante = lancamentoOriginal.copy(
                idMov = 0,
                Valor = -valorRestante,
                dtApr = null,
                statusMov = ""
            )
            val restanteId = insertLancamento(lancamentoRestante)
            if (restanteId == -1L) return false

            // Gerar lançamento de entrada
            val lancamentoEntrada = Lancamento(
                idMov = 0,
                recurso = lancamentoOriginal.recurso,
                vrecurso = lancamentoOriginal.vrecurso,
                clifor = lancamentoOriginal.clifor,
                vCliFor = lancamentoOriginal.vCliFor,
                dtLancto = dataPagamento,
                dtEmi = lancamentoOriginal.dtEmi,
                dtVcto = lancamentoOriginal.dtVcto,
                documento = lancamentoOriginal.documento,
                classif = "9.001.003",
                Descr = "${lancamentoOriginal.Descr} -PG- $idMov",
                Valor = valorPago,
                dtApr = dataPagamento,
                statusMov = "PG",
                Prev = "V"
            )
            val entradaId = insertLancamento(lancamentoEntrada)
            if (entradaId == -1L) return false

            // Gerar lançamento de saída
            val lancamentoSaida = Lancamento(
                idMov = 0,
                recurso = recursoUtilizado,
                vrecurso = fkGpPrinc,
                clifor = lancamentoOriginal.clifor,
                vCliFor = lancamentoOriginal.vCliFor,
                dtLancto = dataPagamento,
                dtEmi = lancamentoOriginal.dtEmi,
                dtVcto = lancamentoOriginal.dtVcto,
                documento = lancamentoOriginal.documento,
                classif = "9.001.003",
                Descr = "${lancamentoOriginal.Descr} -PG- $idMov",
                Valor = -valorPago,
                dtApr = dataPagamento,
                statusMov = "PG",
                Prev = "V"
            )
            val saidaId = insertLancamento(lancamentoSaida)
            if (saidaId == -1L) return false

            // Gerar lançamento de juros, se houver
            if (juros > 0) {
                val planoContas = getPlanoContasByCod("4.008.003") ?: return false
                val lancamentoJuros = Lancamento(
                    idMov = 0,
                    recurso = recursoUtilizado,
                    vrecurso = "4.008.003",
                    clifor = lancamentoOriginal.clifor,
                    vCliFor = lancamentoOriginal.vCliFor,
                    dtLancto = dataPagamento,
                    dtEmi = lancamentoOriginal.dtEmi,
                    dtVcto = lancamentoOriginal.dtVcto,
                    documento = lancamentoOriginal.documento,
                    classif = "4.008.003",
                    Descr = "${lancamentoOriginal.Descr} -Juros Pagos- $idMov",
                    Valor = -juros,
                    dtApr = dataPagamento,
                    statusMov = "JP",
                    Prev = "V"
                )
                val jurosId = insertLancamento(lancamentoJuros)
                if (jurosId == -1L) return false
            }

            db.setTransactionSuccessful()
            return true
        } catch (e: SQLiteException) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun registrarTransferencia(
        origemRecurso: String,
        origemVrecurso: String,
        destinoRecurso: String,
        destinoVrecurso: String,
        documento: String,
        data: String,
        valor: Double
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Saída (origem)
            val saidaValues = ContentValues().apply {
                put(COL_RECURSO, origemRecurso)
                put(COL_VRECURSO, origemVrecurso)
                put(COL_CLIFOR, origemRecurso)
                put(COL_VCLIFOR, origemVrecurso)
                put(COL_DTLANCTO, data)
                put(COL_DT_EMI, data)
                put(COL_DT_VCTO, data)
                put(COL_DOCUMENTO, documento)
                put(COL_CLASSIF, "9.001.001")
                put(COL_DESCR, "TRANSFERIDO PARA: $destinoRecurso")
                put(COL_VALOR, -valor)
                put(COL_DT_APR, data)
                put(COL_STATUS_MOV, "TO")
                put(COL_PREV, "V")
            }
            val idMovSaida = db.insertOrThrow(TABLE_LANCAMENTOS, null, saidaValues).toInt()
            if (idMovSaida.toLong() == -1L) return false

            // Entrada (destino)
            val entradaValues = ContentValues().apply {
                put(COL_RECURSO, destinoRecurso)
                put(COL_VRECURSO, destinoVrecurso)
                put(COL_CLIFOR, destinoRecurso)
                put(COL_VCLIFOR, destinoVrecurso)
                put(COL_DTLANCTO, data)
                put(COL_DT_EMI, data)
                put(COL_DT_VCTO, data)
                put(COL_DOCUMENTO, documento)
                put(COL_CLASSIF, "9.001.002")
                put(COL_DESCR, "TRANSFERIDO DE: $origemRecurso")
                put(COL_VALOR, valor)
                put(COL_DT_APR, data)
                put(COL_STATUS_MOV, "TD")
                put(COL_PREV, "V")
            }
            val idMovEntrada = db.insertOrThrow(TABLE_LANCAMENTOS, null, entradaValues).toInt()
            if (idMovEntrada.toLong() == -1L) return false

            db.setTransactionSuccessful()
            return true
        } catch (e: SQLiteException) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    // Métodos para tbclifor
    fun insertClifor(clifor: Clifor): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_COD_CLIFOR, clifor.codCliFor)
            put(COL_TIPO, clifor.Tipo)
            put(COL_NOME_CLIFOR, clifor.nomeCliFor)
            put(COL_APELIDO_CLIFOR, clifor.apelidoCliFor)
            put(COL_EMAIL, clifor.email)
            put(COL_CELULAR, clifor.celular)
            put(COL_TELEFONE, clifor.telefone)
            put(COL_CEP, clifor.cep)
            put(COL_ENDERECO, clifor.endereco)
            put(COL_NUMERO, clifor.numero)
            put(COL_COMPLEMENTO, clifor.complemento)
            put(COL_BAIRRO, clifor.bairro)
            put(COL_CIDADE, clifor.cidade)
            put(COL_ESTADO, clifor.estado)
            put(COL_RG, clifor.rg)
            put(COL_CPF, clifor.cpf)
            put(COL_CONTATO_CLIFOR, clifor.contatoCliFor)
            put(COL_OBS, clifor.obs)
            put(COL_FK_CLIFOR_GP, clifor.fkCliForGp)
        }
        return db.insert(TABLE_CLIFOR, null, values)
    }

    fun updateClifor(codCliFor: String, clifor: Clifor): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TIPO, clifor.Tipo)
            put(COL_NOME_CLIFOR, clifor.nomeCliFor)
            put(COL_APELIDO_CLIFOR, clifor.apelidoCliFor)
            put(COL_EMAIL, clifor.email)
            put(COL_CELULAR, clifor.celular)
            put(COL_TELEFONE, clifor.telefone)
            put(COL_CEP, clifor.cep)
            put(COL_ENDERECO, clifor.endereco)
            put(COL_NUMERO, clifor.numero)
            put(COL_COMPLEMENTO, clifor.complemento)
            put(COL_BAIRRO, clifor.bairro)
            put(COL_CIDADE, clifor.cidade)
            put(COL_ESTADO, clifor.estado)
            put(COL_RG, clifor.rg)
            put(COL_CPF, clifor.cpf)
            put(COL_CONTATO_CLIFOR, clifor.contatoCliFor)
            put(COL_OBS, clifor.obs)
            put(COL_FK_CLIFOR_GP, clifor.fkCliForGp)
        }
        return db.update(TABLE_CLIFOR, values, "$COL_COD_CLIFOR = ?", arrayOf(codCliFor))
    }

    fun deleteClifor(codCliFor: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_CLIFOR, "$COL_COD_CLIFOR = ?", arrayOf(codCliFor))
    }

    fun getAllClifors(): List<Clifor> {
        val clifors = mutableListOf<Clifor>()
        val db = readableDatabase
        val cursor =
            db.query(TABLE_CLIFOR, null, null, null, null, null, "$COL_NOME_CLIFOR ASC")
        cursor.use {
            while (it.moveToNext()) {
                clifors.add(
                    Clifor(
                        codCliFor = it.getString(it.getColumnIndexOrThrow(COL_COD_CLIFOR))
                            ?: "",
                        Tipo = it.getString(it.getColumnIndexOrThrow(COL_TIPO)) ?: "",
                        nomeCliFor = it.getString(it.getColumnIndexOrThrow(COL_NOME_CLIFOR))
                            ?: "",
                        apelidoCliFor = it.getString(it.getColumnIndexOrThrow(COL_APELIDO_CLIFOR)),
                        email = it.getString(it.getColumnIndexOrThrow(COL_EMAIL)),
                        celular = it.getString(it.getColumnIndexOrThrow(COL_CELULAR)),
                        telefone = it.getString(it.getColumnIndexOrThrow(COL_TELEFONE)),
                        cep = it.getString(it.getColumnIndexOrThrow(COL_CEP)),
                        endereco = it.getString(it.getColumnIndexOrThrow(COL_ENDERECO)),
                        numero = it.getInt(it.getColumnIndexOrThrow(COL_NUMERO)),
                        complemento = it.getString(it.getColumnIndexOrThrow(COL_COMPLEMENTO)),
                        bairro = it.getString(it.getColumnIndexOrThrow(COL_BAIRRO)),
                        cidade = it.getString(it.getColumnIndexOrThrow(COL_CIDADE)),
                        estado = it.getString(it.getColumnIndexOrThrow(COL_ESTADO)),
                        rg = it.getString(it.getColumnIndexOrThrow(COL_RG)),
                        cpf = it.getString(it.getColumnIndexOrThrow(COL_CPF)),
                        contatoCliFor = it.getString(it.getColumnIndexOrThrow(COL_CONTATO_CLIFOR)),
                        obs = it.getString(it.getColumnIndexOrThrow(COL_OBS)),
                        fkCliForGp = it.getString(it.getColumnIndexOrThrow(COL_FK_CLIFOR_GP))
                    )
                )
            }
        }
        return clifors
    }

    fun getClientes(): List<Clifor> {
        val clifors = mutableListOf<Clifor>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CLIFOR,
            null,
            "$COL_TIPO = ?",
            arrayOf("CLI"),
            null,
            null,
            "$COL_NOME_CLIFOR ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                clifors.add(
                    Clifor(
                        codCliFor = it.getString(it.getColumnIndexOrThrow(COL_COD_CLIFOR))
                            ?: "",
                        Tipo = it.getString(it.getColumnIndexOrThrow(COL_TIPO)) ?: "",
                        nomeCliFor = it.getString(it.getColumnIndexOrThrow(COL_NOME_CLIFOR))
                            ?: "",
                        apelidoCliFor = it.getString(it.getColumnIndexOrThrow(COL_APELIDO_CLIFOR)),
                        email = it.getString(it.getColumnIndexOrThrow(COL_EMAIL)),
                        celular = it.getString(it.getColumnIndexOrThrow(COL_CELULAR)),
                        telefone = it.getString(it.getColumnIndexOrThrow(COL_TELEFONE)),
                        cep = it.getString(it.getColumnIndexOrThrow(COL_CEP)),
                        endereco = it.getString(it.getColumnIndexOrThrow(COL_ENDERECO)),
                        numero = it.getInt(it.getColumnIndexOrThrow(COL_NUMERO)),
                        complemento = it.getString(it.getColumnIndexOrThrow(COL_COMPLEMENTO)),
                        bairro = it.getString(it.getColumnIndexOrThrow(COL_BAIRRO)),
                        cidade = it.getString(it.getColumnIndexOrThrow(COL_CIDADE)),
                        estado = it.getString(it.getColumnIndexOrThrow(COL_ESTADO)),
                        rg = it.getString(it.getColumnIndexOrThrow(COL_RG)),
                        cpf = it.getString(it.getColumnIndexOrThrow(COL_CPF)),
                        contatoCliFor = it.getString(it.getColumnIndexOrThrow(COL_CONTATO_CLIFOR)),
                        obs = it.getString(it.getColumnIndexOrThrow(COL_OBS)),
                        fkCliForGp = it.getString(it.getColumnIndexOrThrow(COL_FK_CLIFOR_GP))
                    )
                )
            }
        }
        return clifors
    }

    fun getFornecedores(): List<Clifor> {
        val clifors = mutableListOf<Clifor>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CLIFOR,
            null,
            "$COL_TIPO = ?",
            arrayOf("FOR"),
            null,
            null,
            "$COL_NOME_CLIFOR ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                clifors.add(
                    Clifor(
                        codCliFor = it.getString(it.getColumnIndexOrThrow(COL_COD_CLIFOR))
                            ?: "",
                        Tipo = it.getString(it.getColumnIndexOrThrow(COL_TIPO)) ?: "",
                        nomeCliFor = it.getString(it.getColumnIndexOrThrow(COL_NOME_CLIFOR))
                            ?: "",
                        apelidoCliFor = it.getString(it.getColumnIndexOrThrow(COL_APELIDO_CLIFOR)),
                        email = it.getString(it.getColumnIndexOrThrow(COL_EMAIL)),
                        celular = it.getString(it.getColumnIndexOrThrow(COL_CELULAR)),
                        telefone = it.getString(it.getColumnIndexOrThrow(COL_TELEFONE)),
                        cep = it.getString(it.getColumnIndexOrThrow(COL_CEP)),
                        endereco = it.getString(it.getColumnIndexOrThrow(COL_ENDERECO)),
                        numero = it.getInt(it.getColumnIndexOrThrow(COL_NUMERO)),
                        complemento = it.getString(it.getColumnIndexOrThrow(COL_COMPLEMENTO)),
                        bairro = it.getString(it.getColumnIndexOrThrow(COL_BAIRRO)),
                        cidade = it.getString(it.getColumnIndexOrThrow(COL_CIDADE)),
                        estado = it.getString(it.getColumnIndexOrThrow(COL_ESTADO)),
                        rg = it.getString(it.getColumnIndexOrThrow(COL_RG)),
                        cpf = it.getString(it.getColumnIndexOrThrow(COL_CPF)),
                        contatoCliFor = it.getString(it.getColumnIndexOrThrow(COL_CONTATO_CLIFOR)),
                        obs = it.getString(it.getColumnIndexOrThrow(COL_OBS)),
                        fkCliForGp = it.getString(it.getColumnIndexOrThrow(COL_FK_CLIFOR_GP))
                    )
                )
            }
        }
        return clifors
    }

    fun suggestFornecedorCode(): String {
        val db = readableDatabase
        val query = """
        SELECT MIN(CAST(codCliFor AS INTEGER) + 1) AS next_code
        FROM (
            SELECT -1 AS codCliFor UNION ALL
            SELECT codCliFor FROM tbclifor WHERE CAST(codCliFor AS INTEGER) BETWEEN 0 AND 999
        ) AS nums
        WHERE NOT EXISTS (
            SELECT 1 FROM tbclifor WHERE CAST(codCliFor AS INTEGER) = nums.codCliFor + 1
        ) AND nums.codCliFor + 1 <= 999
    """.trimIndent()
        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val nextCode = cursor.getInt(cursor.getColumnIndexOrThrow("next_code"))
                return String.format("%04d", nextCode) // Formata como "0009"
            }
        }
        return "0000" // Caso não haja códigos disponíveis, retorna o primeiro
    }

    fun suggestClienteCode(): String {
        val db = readableDatabase
        val query = """
        SELECT MIN(CAST(codCliFor AS INTEGER) + 1) AS next_code
        FROM (
            SELECT 999 AS codCliFor UNION ALL
            SELECT codCliFor FROM tbclifor WHERE CAST(codCliFor AS INTEGER) >= 1000
        ) AS nums
        WHERE NOT EXISTS (
            SELECT 1 FROM tbclifor WHERE CAST(codCliFor AS INTEGER) = nums.codCliFor + 1
        )
    """.trimIndent()
        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val nextCode = cursor.getInt(cursor.getColumnIndexOrThrow("next_code"))
                return String.format("%04d", nextCode) // Formata como "1000"
            }
        }
        return "1000" // Caso não haja códigos disponíveis, retorna o primeiro
    }

    fun isCodeUnique(code: String): Boolean {
        val db = readableDatabase
        db.rawQuery("SELECT 1 FROM tbclifor WHERE codCliFor = ?", arrayOf(code)).use { cursor ->
            return !cursor.moveToFirst()
        }
    }


    // Métodos para gpprincipal
    fun insertPlanoContas(plano: PlanoContas): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_COD_GERAL, plano.cod_Geral)
            put(COL_NOME_P, plano.nome_P)
            put(COL_NOME_S, plano.nome_S)
            put(COL_NOME_C, plano.nome_C)
        }
        return db.insert(TABLE_GPPRINCIPAL, null, values)
    }

    fun updatePlanoContas(codGeral: String, plano: PlanoContas): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NOME_P, plano.nome_P)
            put(COL_NOME_S, plano.nome_S)
            put(COL_NOME_C, plano.nome_C)
        }
        return db.update(TABLE_GPPRINCIPAL, values, "$COL_COD_GERAL = ?", arrayOf(codGeral))
    }

    fun deletePlanoContas(codGeral: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_GPPRINCIPAL, "$COL_COD_GERAL = ?", arrayOf(codGeral))
    }

    fun getAllPlanosContas(): List<PlanoContas> {
        val planos = mutableListOf<PlanoContas>()
        val db = readableDatabase
        val cursor =
            db.query(TABLE_GPPRINCIPAL, null, null, null, null, null, "$COL_COD_GERAL ASC")
        cursor.use {
            while (it.moveToNext()) {
                planos.add(
                    PlanoContas(
                        cod_Geral = it.getString(it.getColumnIndexOrThrow(COL_COD_GERAL)) ?: "",
                        nome_P = it.getString(it.getColumnIndexOrThrow(COL_NOME_P)) ?: "",
                        nome_S = it.getString(it.getColumnIndexOrThrow(COL_NOME_S)) ?: "",
                        nome_C = it.getString(it.getColumnIndexOrThrow(COL_NOME_C)) ?: ""
                    )
                )
            }
        }
        return planos
    }

    fun getPlanoContasByCod(codGeral: String): PlanoContas? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GPPRINCIPAL,
            null,
            "$COL_COD_GERAL = ?",
            arrayOf(codGeral),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                PlanoContas(
                    cod_Geral = it.getString(it.getColumnIndexOrThrow(COL_COD_GERAL)) ?: "",
                    nome_P = it.getString(it.getColumnIndexOrThrow(COL_NOME_P)) ?: "",
                    nome_S = it.getString(it.getColumnIndexOrThrow(COL_NOME_S)) ?: "",
                    nome_C = it.getString(it.getColumnIndexOrThrow(COL_NOME_C)) ?: ""
                )
            } else {
                null
            }
        }
    }

    // Funções para Parcelamentos
    fun carregarRecursos(): List<Pair<String, Pair<String, String>>> {
        val recursos = mutableListOf<Pair<String, Pair<String, String>>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT codigo, nomebco, fk_gpprinc FROM tbrecursos WHERE fk_gpprinc != '2.001.003' ORDER BY nomebco",
            null
        )
        while (cursor.moveToNext()) {
            val codigo = cursor.getString(0)
            val nome = cursor.getString(1)
            val fk_gpprinc = cursor.getString(2)
            recursos.add(Pair("$nome - $codigo - $fk_gpprinc", Pair(codigo, fk_gpprinc)))
        }
        //cursor.close()
        //db.close()
        return recursos
    }

    fun carregarRecursosCartoes(): List<Pair<String, Pair<String, String>>> {
        val recursos = mutableListOf<Pair<String, Pair<String, String>>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT codigo, nomebco, fk_gpprinc FROM tbrecursos WHERE fk_gpprinc = '2.001.003' ORDER BY nomebco",
            null
        )
        while (cursor.moveToNext()) {
            val codigo = cursor.getString(0)
            val nome = cursor.getString(1)
            val fk_gpprinc = cursor.getString(2)
            recursos.add(Pair("$nome - $codigo - $fk_gpprinc", Pair(codigo, fk_gpprinc)))
        }
        cursor.close()
        db.close()
        return recursos
    }

    fun carregarClifor(): List<Pair<String, Pair<String, String>>> {
        val clifor = mutableListOf<Pair<String, Pair<String, String>>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT codCliFor, apelidoCliFor, fkCliForGp FROM tbclifor ORDER BY apelidoCliFor",
            null
        )
        while (cursor.moveToNext()) {
            val codCliFor = cursor.getString(0)
            val apelido = cursor.getString(1)
            val fkCliForGp = cursor.getString(2)
            clifor.add(Pair("$apelido - $codCliFor - $fkCliForGp", Pair(codCliFor, fkCliForGp)))
        }
        cursor.close()
        db.close()
        return clifor
    }

    fun carregarClassificacao(): List<Pair<String, String>> {
        val classif = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT cod_Geral, nome_C FROM gpprincipal WHERE nome_C <> '-' ORDER BY nome_C",
            null
        )
        while (cursor.moveToNext()) {
            val codGeral = cursor.getString(0)
            val nomeC = cursor.getString(1)
            classif.add(Pair("$nomeC - $codGeral", codGeral))
        }
        cursor.close()
        db.close()
        return classif
    }

    fun inserirLancamentoParcelado(
        tipo: String, recurso: String, vrecurso: String, clifor: String, vCliFor: String,
        dtEmi: String, dtVcto: String, documento: String, classif: String, descr: String,
        valorTotal: Double, valorParcela: Double, numParcelas: Int, isCartao: Boolean,
        periodicidade: String, corrigeEmissao: Boolean, statusMov: String, prev: String
    ): Boolean {
        val db = writableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendarVcto = Calendar.getInstance()
        val calendarEmi = Calendar.getInstance()

        // Inicializamos as variáveis EXATAMENTE com o que veio dos parâmetros
        var currentDtEmi = dtEmi
        var currentDtVcto = dtVcto
        var currentDtLancto = dtEmi

        try {
            calendarVcto.time = dateFormat.parse(dtVcto) ?: return false
            val vctoDayOfMonth = calendarVcto.get(Calendar.DAY_OF_MONTH)

            var emiDayOfMonth = vctoDayOfMonth
            if (corrigeEmissao) {
                calendarEmi.time = dateFormat.parse(dtEmi) ?: return false
                emiDayOfMonth = calendarEmi.get(Calendar.DAY_OF_MONTH)
            }

            for (i in 1..numParcelas) {
                val roundedValorParcela = String.format(
                    Locale.US,
                    "%.2f",
                    if (tipo == "Saída") -Math.abs(valorParcela) else Math.abs(valorParcela)
                ).toDouble()

                val contentValues = ContentValues().apply {
                    put("recurso", recurso)
                    put("vrecurso", vrecurso)
                    put("clifor", clifor)
                    put("vCliFor", vCliFor)
                    put("dtlancto", currentDtLancto)
                    put("dtEmi", currentDtEmi)
                    put("dtVcto", currentDtVcto)
                    put("documento", "$documento-$i")
                    put("classif", classif)
                    put("Descr", "parc.$i/$numParcelas-$descr")
                    put("valor", roundedValorParcela)
                    put(
                        "dtApr",
                        if (statusMov == "PG" || statusMov == "RC") currentDtVcto else null
                    )
                    put("statusMov", statusMov)
                    put("Prev", prev)
                }

                val result = db.insert("tbmovimento", null, contentValues)
                if (result == -1L) return false

                // SÓ INCREMENTA SE HOUVER UMA PRÓXIMA PARCELA
                if (i < numParcelas) {
                    // 1. Ajustar Vencimento
                    calendarVcto.set(Calendar.DAY_OF_MONTH, vctoDayOfMonth)
                    when (periodicidade) {
                        "Mensal" -> calendarVcto.add(Calendar.MONTH, 1)
                        "Bimestral" -> calendarVcto.add(Calendar.MONTH, 2)
                        "Trimestral" -> calendarVcto.add(Calendar.MONTH, 3)
                        "Quadrimestral" -> calendarVcto.add(Calendar.MONTH, 4)
                        "Anual" -> calendarVcto.add(Calendar.YEAR, 1)
                        "Semanal" -> calendarVcto.add(Calendar.WEEK_OF_MONTH, 1)
                        "Diário" -> calendarVcto.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    currentDtVcto = dateFormat.format(calendarVcto.time)

                    // 2. Ajustar Emissão e Lançamento
                    if (corrigeEmissao) {
                        when (periodicidade) {
                            "Mensal" -> calendarEmi.add(Calendar.MONTH, 1)
                            "Bimestral" -> calendarEmi.add(Calendar.MONTH, 2)
                            "Trimestral" -> calendarEmi.add(Calendar.MONTH, 3)
                            "Quadrimestral" -> calendarEmi.add(Calendar.MONTH, 4)
                            "Anual" -> calendarEmi.add(Calendar.YEAR, 1)
                            "Semanal" -> calendarEmi.add(Calendar.WEEK_OF_MONTH, 1)
                            "Diário" -> calendarEmi.add(Calendar.DAY_OF_MONTH, 1)
                        }

                        val maxDayOfMonth = calendarEmi.getActualMaximum(Calendar.DAY_OF_MONTH)
                        calendarEmi.set(Calendar.DAY_OF_MONTH, minOf(emiDayOfMonth, maxDayOfMonth))

                        currentDtEmi = dateFormat.format(calendarEmi.time)
                        currentDtLancto = currentDtEmi
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao inserir: ${e.message}")
            return false
        } finally {
            db.close()
        }
    }

    fun inserirLancamentoParceladoComAjuste(
        tipo: String, recurso: String, vrecurso: String, clifor: String, vCliFor: String,
        dtEmi: String, dtVcto: String, documento: String, classif: String, descr: String,
        valorTotal: Double, valorParcela: Double, numParcelas: Int, isCartao: Boolean,
        periodicidade: String, corrigeEmissao: Boolean, statusMov: String, prev: String,
        parcelaAjustada: Int, diff: Double
    ): Boolean {
        val db = writableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendarVcto = Calendar.getInstance()
        val calendarEmi = Calendar.getInstance()

        // Inicialização segura das variáveis de controle
        var currentDtEmi = dtEmi
        var currentDtVcto = dtVcto
        var currentDtLancto = dtEmi

        try {
            calendarVcto.time = dateFormat.parse(dtVcto) ?: return false
            val vctoDayOfMonth = calendarVcto.get(Calendar.DAY_OF_MONTH)

            var emiDayOfMonth = vctoDayOfMonth
            if (corrigeEmissao) {
                calendarEmi.time = dateFormat.parse(dtEmi) ?: return false
                emiDayOfMonth = calendarEmi.get(Calendar.DAY_OF_MONTH)
            }

            for (i in 1..numParcelas) {
                // Lógica de cálculo do valor com o ajuste (diff)
                val adjustedValor = if (i == parcelaAjustada) {
                    if (tipo == "Saída") {
                        if (valorParcela < 0) (valorParcela - diff) else -(Math.abs(valorParcela) + diff)
                    } else {
                        if (valorParcela < 0) (-Math.abs(valorParcela) - diff) else (valorParcela + diff)
                    }
                } else {
                    if (tipo == "Saída") -Math.abs(valorParcela) else Math.abs(valorParcela)
                }

                val roundedAdjustedValor = String.format(Locale.US, "%.2f", adjustedValor).toDouble()

                val contentValues = ContentValues().apply {
                    put("recurso", recurso)
                    put("vrecurso", vrecurso)
                    put("clifor", clifor)
                    put("vCliFor", vCliFor)
                    put("dtlancto", currentDtLancto)
                    put("dtEmi", currentDtEmi)
                    put("dtVcto", currentDtVcto)
                    put("documento", "$documento-$i")
                    put("classif", classif)
                    put("Descr", "parc.$i/$numParcelas-$descr")
                    put("valor", roundedAdjustedValor)
                    put(
                        "dtApr",
                        if (statusMov == "PG" || statusMov == "RC") currentDtVcto else null
                    )
                    put("statusMov", statusMov) // Mantive o status original aqui
                    put("Prev", prev)
                }

                val result = db.insert("tbmovimento", null, contentValues)
                if (result == -1L) return false

                // INCREMENTO APENAS PARA A PRÓXIMA PARCELA
                if (i < numParcelas) {
                    // 1. Atualizar Vencimento
                    calendarVcto.set(Calendar.DAY_OF_MONTH, vctoDayOfMonth)
                    when (periodicidade) {
                        "Mensal" -> calendarVcto.add(Calendar.MONTH, 1)
                        "Bimestral" -> calendarVcto.add(Calendar.MONTH, 2)
                        "Trimestral" -> calendarVcto.add(Calendar.MONTH, 3)
                        "Quadrimestral" -> calendarVcto.add(Calendar.MONTH, 4)
                        "Anual" -> calendarVcto.add(Calendar.YEAR, 1)
                        "Semanal" -> calendarVcto.add(Calendar.WEEK_OF_MONTH, 1)
                        "Diário" -> calendarVcto.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    currentDtVcto = dateFormat.format(calendarVcto.time)

                    // 2. Atualizar Emissão e Lançamento
                    if (corrigeEmissao) {
                        when (periodicidade) {
                            "Mensal" -> calendarEmi.add(Calendar.MONTH, 1)
                            "Bimestral" -> calendarEmi.add(Calendar.MONTH, 2)
                            "Trimestral" -> calendarEmi.add(Calendar.MONTH, 3)
                            "Quadrimestral" -> calendarEmi.add(Calendar.MONTH, 4)
                            "Anual" -> calendarEmi.add(Calendar.YEAR, 1)
                            "Semanal" -> calendarEmi.add(Calendar.WEEK_OF_MONTH, 1)
                            "Diário" -> calendarEmi.add(Calendar.DAY_OF_MONTH, 1)
                        }

                        val maxDay = calendarEmi.getActualMaximum(Calendar.DAY_OF_MONTH)
                        calendarEmi.set(Calendar.DAY_OF_MONTH, minOf(emiDayOfMonth, maxDay))

                        currentDtEmi = dateFormat.format(calendarEmi.time)
                        currentDtLancto = currentDtEmi
                    }
                }

                Log.d("DatabaseHelper", "Parcela $i: valor=$roundedAdjustedValor, dtVcto=$currentDtVcto")
            }
            return true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao inserir parcelado com ajuste: ${e.message}")
            return false
        } finally {
            db.close()
        }
    }

    fun inserirLancamentoParceladoCartao(
        recursoOriginal: String,
        vrecursoOriginal: String,
        clifor: String,
        vCliFor: String,
        dtEmi: String,
        dtVcto: String,
        documento: String,
        classif: String,
        descr: String,
        valorTotal: Double,
        valorParcela: Double,
        numParcelas: Int,
        periodicidade: String,
        corrigeEmissao: Boolean,
        prev: String,
        parcelaAjustada: Int,
        diff: Double,
        cartaoRecurso: String,
        cartaoVrecurso: String
    ): Boolean {
        val db = writableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendarVcto = Calendar.getInstance()
        val calendarEmi = Calendar.getInstance()

        // Variáveis de controle de data iniciam com os valores passados
        var currentDtEmi = dtEmi
        var currentDtVcto = dtVcto
        var currentDtLancto = dtEmi

        try {
            calendarVcto.time = dateFormat.parse(dtVcto) ?: return false
            val vctoDayOfMonth = calendarVcto.get(Calendar.DAY_OF_MONTH)

            var emiDayOfMonth = vctoDayOfMonth
            if (corrigeEmissao) {
                calendarEmi.time = dateFormat.parse(dtEmi) ?: return false
                emiDayOfMonth = calendarEmi.get(Calendar.DAY_OF_MONTH)
            }

            for (i in 1..numParcelas) {
                // Cálculo do valor com ajuste de diferença (se for a parcela do ajuste)
                val adjustedValor = if (i == parcelaAjustada) {
                    valorParcela + diff
                } else {
                    valorParcela
                }
                val roundedValue = String.format(Locale.US, "%.2f", Math.abs(adjustedValor)).toDouble()

                // 1. LANÇAMENTO NO FAVORECIDO (Contas a Pagar)
                val contentValuesPagar = ContentValues().apply {
                    put("recurso", "0079")
                    put("vrecurso", "2.001.003")
                    put("clifor", clifor)
                    put("vCliFor", vCliFor)
                    put("dtlancto", currentDtLancto)
                    put("dtEmi", currentDtEmi)
                    put("dtVcto", currentDtVcto)
                    put("documento", "$documento-$i")
                    put("classif", classif)
                    put("Descr", "parc.$i/$numParcelas-$descr")
                    put("valor", -roundedValue)
                    put("dtApr", currentDtVcto)
                    put("statusMov", "PG")
                    put("Prev", prev)
                }
                db.insert("tbmovimento", null, contentValuesPagar)

                // 2. BAIXA NO FAVORECIDO (Zera o fornecedor)
                val contentValuesBaixa = ContentValues().apply {
                    put("recurso", "0079")
                    put("vrecurso", "2.001.003")
                    put("clifor", clifor)
                    put("vCliFor", vCliFor)
                    put("dtlancto", currentDtLancto)
                    put("dtEmi", currentDtEmi)
                    put("dtVcto", currentDtVcto)
                    put("documento", "$documento-$i")
                    put("classif", "9.001.003")
                    put("Descr", "parc.$i/$numParcelas-$descr")
                    put("valor", roundedValue)
                    put("dtApr", currentDtVcto)
                    put("statusMov", "PG")
                    put("Prev", prev)
                }
                db.insert("tbmovimento", null, contentValuesBaixa)

                // 3. LANÇAMENTO REAL NO CARTÃO
                val contentValuesCartao = ContentValues().apply {
                    put("recurso", cartaoRecurso)
                    put("vrecurso", cartaoVrecurso)
                    put("clifor", clifor)
                    put("vCliFor", vCliFor)
                    put("dtlancto", currentDtLancto)
                    put("dtEmi", currentDtEmi)
                    put("dtVcto", currentDtVcto)
                    put("documento", "$documento-$i")
                    put("classif", "9.001.003")
                    put("Descr", "parc.$i/$numParcelas-$descr")
                    put("valor", -roundedValue)
                    putNull("dtApr")
                    put("statusMov", "")
                    put("Prev", prev)
                }
                db.insert("tbmovimento", null, contentValuesCartao)

                // Lógica de incremento para a PRÓXIMA parcela
                if (i < numParcelas) {
                    // --- Incremento do Vencimento ---
                    calendarVcto.set(Calendar.DAY_OF_MONTH, vctoDayOfMonth)
                    when (periodicidade) {
                        "Mensal" -> calendarVcto.add(Calendar.MONTH, 1)
                        "Bimestral" -> calendarVcto.add(Calendar.MONTH, 2)
                        "Trimestral" -> calendarVcto.add(Calendar.MONTH, 3)
                        "Quadrimestral" -> calendarVcto.add(Calendar.MONTH, 4)
                        "Anual" -> calendarVcto.add(Calendar.YEAR, 1)
                        "Semanal" -> calendarVcto.add(Calendar.WEEK_OF_MONTH, 1)
                        "Diário" -> calendarVcto.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    currentDtVcto = dateFormat.format(calendarVcto.time)

                    // --- Incremento da Emissão e Lançamento ---
                    if (corrigeEmissao) {
                        // Agora a Emissão pula o mesmo intervalo que o Vencimento
                        when (periodicidade) {
                            "Mensal" -> calendarEmi.add(Calendar.MONTH, 1)
                            "Bimestral" -> calendarEmi.add(Calendar.MONTH, 2)
                            "Trimestral" -> calendarEmi.add(Calendar.MONTH, 3)
                            "Quadrimestral" -> calendarEmi.add(Calendar.MONTH, 4)
                            "Anual" -> calendarEmi.add(Calendar.YEAR, 1)
                            "Semanal" -> calendarEmi.add(Calendar.WEEK_OF_MONTH, 1)
                            "Diário" -> calendarEmi.add(Calendar.DAY_OF_MONTH, 1)
                        }

                        // Ajuste de fim de mês (ex: evitar 31 de abril)
                        val maxDay = calendarEmi.getActualMaximum(Calendar.DAY_OF_MONTH)
                        calendarEmi.set(Calendar.DAY_OF_MONTH, minOf(emiDayOfMonth, maxDay))

                        currentDtEmi = dateFormat.format(calendarEmi.time)
                        currentDtLancto = currentDtEmi
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao inserir parcelado cartão: ${e.message}")
            return false
        } finally {
            db.close()
        }
    }

//Metodos para Consulta de Favorecidos

    fun carregarFavorecidos(): List<Pair<String, Pair<String, String>>> {
        val favorecidos = mutableListOf<Pair<String, Pair<String, String>>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT codCliFor, Tipo, apelidoCliFor FROM $TABLE_CLIFOR ORDER BY apelidoCliFor",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val codCliFor = it.getString(it.getColumnIndexOrThrow("codCliFor")) ?: continue
                val tipo = it.getString(it.getColumnIndexOrThrow("Tipo")) ?: ""
                val apelidoCliFor =
                    it.getString(it.getColumnIndexOrThrow("apelidoCliFor")) ?: "Sem Nome"
                Log.d(
                    "DatabaseHelper",
                    "Carregado favorecido: codCliFor=$codCliFor, Tipo=$tipo, apelidoCliFor=$apelidoCliFor"
                )
                favorecidos.add(Pair("$apelidoCliFor - $codCliFor", Pair(codCliFor, tipo)))
            }
        }
        db.close()
        return favorecidos
    }

    fun getSaldoByClifor(clifor: String, dataIni: String): Double {
        val db = readableDatabase
        val cursorClifor =
            db.rawQuery("SELECT Tipo FROM $TABLE_CLIFOR WHERE codCliFor = ?", arrayOf(clifor))
        var recurso: String? = null
        cursorClifor.use {
            if (it.moveToFirst()) {
                val tipo = it.getString(it.getColumnIndexOrThrow("Tipo")) ?: ""
                recurso = when (tipo) {
                    "CLI" -> "0022"
                    "FOR" -> "0079"
                    else -> null
                }
                Log.d("DatabaseHelper", "clifor=$clifor, Tipo=$tipo, recurso=$recurso")
            } else {
                Log.w("DatabaseHelper", "Nenhum Tipo encontrado para clifor=$clifor")
            }
        }
        if (recurso == null) {
            db.close()
            Log.w("DatabaseHelper", "Tipo inválido para clifor=$clifor, saldo não calculado")
            return 0.0
        }
        val cursor = db.rawQuery(
            "SELECT SUM(Valor) as saldo FROM $TABLE_LANCAMENTOS WHERE dtVcto < ? AND clifor = ? AND recurso = ?",
            arrayOf(dataIni, clifor, recurso)
        )
        cursor.use {
            if (it.moveToFirst()) {
                val saldo = it.getDouble(it.getColumnIndexOrThrow("saldo"))
                Log.d(
                    "DatabaseHelper",
                    "Saldo anterior para clifor=$clifor, recurso=$recurso, dataIni=$dataIni: $saldo"
                )
                db.close()
                return saldo
            }
        }
        Log.d(
            "DatabaseHelper",
            "Nenhum saldo encontrado para clifor=$clifor, recurso=$recurso, dataIni=$dataIni"
        )
        db.close()
        return 0.0
    }


    // Funções de backup e restauração
    fun backupDatabase(outputStream: OutputStream): Boolean {
        try {
            val currentDB = context.getDatabasePath(DATABASE_NAME)
            FileInputStream(currentDB).use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("DatabaseHelper", "Backup realizado com sucesso")
            return true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao realizar backup: ${e.message}", e)
            return false
        }
    }

    fun restoreDatabase(inputStream: InputStream): Boolean {
        try {
            // 1. Fechar conexões
            close()

            // 2. Caminho do banco
            val currentDB = context.getDatabasePath(DATABASE_NAME)
            currentDB.parentFile?.mkdirs()

            // 3. Copiar backup
            FileOutputStream(currentDB).use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }

            if (!currentDB.exists() || currentDB.length() == 0L) {
                Log.e("DatabaseHelper", "Arquivo restaurado vazio ou inexistente")
                return false
            }

            // 4. Abrir banco restaurado
            val db = SQLiteDatabase.openDatabase(
                currentDB.path,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )

            db.use {
                // === VERIFICAÇÕES ===
                // Integridade
                it.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                    if (!cursor.moveToFirst() || cursor.getString(0) != "ok") {
                        Log.e("DatabaseHelper", "Integridade falhou")
                        return false
                    }
                }

                // Tabelas essenciais
                val tables = listOf(TABLE_LANCAMENTOS, TABLE_GPPRINCIPAL, TABLE_RECURSOS)
                for (table in tables) {
                    it.rawQuery(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                        arrayOf(table)
                    ).use { cursor ->
                        if (!cursor.moveToFirst()) {
                            Log.e("DatabaseHelper", "Tabela $table não encontrada")
                            return false
                        }
                    }
                }

                // === VERSÃO DO BANCO ===
                val restoredVersion = it.rawQuery("PRAGMA user_version", null).use { cursor ->
                    cursor.moveToFirst()
                    cursor.getInt(0)
                }
                Log.d("DatabaseHelper", "Banco restaurado: versão $restoredVersion")

                // === MIGRAÇÃO SEGURA (sem dropar tabelas!) ===
                if (restoredVersion < DATABASE_VERSION) {
                    Log.d("DatabaseHelper", "Migrando: $restoredVersion → $DATABASE_VERSION")
                    recreateAllViews(it)
                    
                    // Criar tb_plano_diretor se não existir
                    if (restoredVersion < 13) {
                        Log.d("DatabaseHelper", "Criando tb_plano_diretor...")
                        it.execSQL("""
                            CREATE TABLE IF NOT EXISTS $TABLE_PLANO_DIRETOR (
                                $COL_PD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                $COL_PD_ANO_REFERENCIA INTEGER,
                                $COL_PD_MES INTEGER,
                                $COL_PD_CLASSIFICACAO TEXT,
                                $COL_PD_CONTA TEXT,
                                $COL_PD_VALOR_PLANEJADO REAL,
                                $COL_PD_INFLACAO_PREMISSA REAL,
                                $COL_PD_DATA_SNAPSHOT DATETIME DEFAULT CURRENT_TIMESTAMP
                            )
                        """.trimIndent())
                    }
                }

                // === FORÇAR VERSÃO ATUAL ===
                it.execSQL("PRAGMA user_version = $DATABASE_VERSION")
                Log.d("DatabaseHelper", "Versão forçada para $DATABASE_VERSION")
            }

            // === GARANTIR VIEWS ===
            if (!ensureViewsExist()) {
                Log.e("DatabaseHelper", "Falha ao recriar views após restauração")
                return false
            }

            // === TESTE FINAL ===
            writableDatabase.use { testDb ->
                testDb.rawQuery("SELECT 1 FROM viewmovrd LIMIT 1", null).use { cursor ->
                    if (cursor.moveToFirst()) {
                        Log.d("DatabaseHelper", "viewmovrd OK após restauração")
                    } else {
                        Log.w("DatabaseHelper", "viewmovrd vazia, mas existe")
                    }
                }
            }

            Log.d("DatabaseHelper", "Restauração concluída com sucesso")
            
            // Verificar e criar tb_plano_diretor se necessário
            writableDatabase.use { db ->
                verifyTableExists(db, "tb_plano_diretor", """
                    CREATE TABLE $TABLE_PLANO_DIRETOR (
                        $COL_PD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                        $COL_PD_ANO_REFERENCIA INTEGER,
                        $COL_PD_MES INTEGER,
                        $COL_PD_CLASSIFICACAO TEXT,
                        $COL_PD_CONTA TEXT,
                        $COL_PD_VALOR_PLANEJADO REAL,
                        $COL_PD_INFLACAO_PREMISSA REAL,
                        $COL_PD_DATA_SNAPSHOT DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """.trimIndent())
            }
            
            return true

        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro na restauração: ${e.message}", e)
            return false
        }
    }

    // Metodo auxiliar para criar tabela se não existir
    private fun verifyTableExists(db: android.database.sqlite.SQLiteDatabase, tableName: String, createSQL: String) {
        try {
            db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(tableName)).use { cursor ->
                if (!cursor.moveToFirst()) {
                    Log.d("DatabaseHelper", "Criando tabela: $tableName")
                    db.execSQL(createSQL)
                    Log.d("DatabaseHelper", "Tabela $tableName criada com sucesso")
                } else {
                    Log.d("DatabaseHelper", "Tabela $tableName já existe")
                }
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao verificar/criar tabela $tableName: ${e.message}")
        }
    }

    // Metodo auxiliar para verificar integridade do banco
    fun checkDatabaseIntegrity(): Boolean {
        val db = readableDatabase
        val tables = listOf(
            TABLE_USUARIO,
            TABLE_RECURSOS,
            TABLE_LANCAMENTOS,
            TABLE_CLIFOR,
            TABLE_GPPRINCIPAL
        )
        try {
            tables.forEach { table ->
                db.rawQuery("SELECT 1 FROM $table LIMIT 1", null).use { cursor ->
                    if (!cursor.moveToFirst()) {
                        Log.w("DatabaseHelper", "Tabela $table está vazia ou não acessível")
                    }
                }
            }
            return true
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro na verificação de integridade: ${e.message}")
            return false
        }
    }

    // Metodo para limpar cache manualmente
    fun clearCache() {
        cachedRecursos = null
        Log.d("DatabaseHelper", "Cache de recursos limpo")
    }

    // Metodo para obter saldo total por recurso
    fun getSaldoByRecurso(recurso: String, dataInicio: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_VALOR) as saldo FROM $TABLE_LANCAMENTOS WHERE $COL_RECURSO = ? AND $COL_DT_VCTO < ?",
            arrayOf(recurso, dataInicio)
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getDouble(it.getColumnIndexOrThrow("saldo")) ?: 0.0
            }
        }
        return 0.0
    }


    // Metodo para obter saldo inicial para consulta Ref.Cruzada
    fun getInitialBalanceByGpPrinc(dataInicio: String): Double {
        val db = readableDatabase
        val query = """
        SELECT COALESCE(SUM(Valor), 0.0) AS initial_balance
        FROM view_mov
        WHERE nome_C IN ('Bancos', 'Caixa', 'Conta Poupança')
        AND dtApr < ?
        AND nomebcoR NOT IN ('CONTAS À PAGAR', 'CONTAS À RECEBER')
    """
        val cursor = db.rawQuery(query, arrayOf(dataInicio))
        var totalBalance = 0.0
        cursor.use {
            if (it.moveToFirst()) {
                totalBalance = it.getDouble(it.getColumnIndexOrThrow("initial_balance"))
                Log.d(
                    "DatabaseHelper",
                    "Saldo inicial total para dataInicio $dataInicio: $totalBalance"
                )
            } else {
                Log.w(
                    "DatabaseHelper",
                    "Nenhum saldo inicial encontrado para dataInicio: $dataInicio"
                )
            }
        }
        return totalBalance
    }

    // Metodo para obter lançamentos por período
    fun getLancamentosByPeriodo(startDate: String, endDate: String): List<Lancamento> {
        val lancamentos = mutableListOf<Lancamento>()
        val db = readableDatabase
        val query = """
        SELECT * FROM $TABLE_LANCAMENTOS
        WHERE $COL_DTLANCTO BETWEEN ? AND ?
        ORDER BY $COL_DTLANCTO ASC
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        cursor.use {
            while (it.moveToNext()) {
                lancamentos.add(mapLancamento(it))
            }
        }
        return lancamentos
    }

    // Metodo para contar lançamentos pendentes
    fun countLancamentosPendentes(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) as count FROM $TABLE_LANCAMENTOS WHERE $COL_DT_APR IS NULL",
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(it.getColumnIndexOrThrow("count"))
            }
        }
        return 0
    }

    fun verifyOpenCursors(): Int {
        var openCursors = 0
        readableDatabase.use { db ->
            db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table'", null)
                .use { cursor ->
                    if (cursor.moveToFirst()) {
                        openCursors = cursor.getInt(0)
                    }
                }
        }
        return openCursors
    }

    // Métodos para a tabela testbirth
    fun addAniversario(name: String, bdate: String): Boolean {
        val db = writableDatabase
        try {
            // Validar e formatar data para yyyy-MM-dd
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            inputFormat.isLenient = false
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = try {
                inputFormat.parse(bdate)?.let { outputFormat.format(it) } ?: return false
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Formato de data inválido: $bdate, erro: ${e.message}")
                return false
            }

            val values = ContentValues().apply {
                put(COL_NAME, name)
                put(COL_BDATE, parsedDate)
            }
            val result = db.insert(TABLE_TESTBIRTH, null, values)
            Log.d(
                "DatabaseHelper",
                "Adicionado compromisso: $name, $parsedDate, resultado: $result"
            )
            return result != -1L
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro ao adicionar compromisso: ${e.message}")
            return false
        } finally {
            db.close()
        }
    }

    fun getAniversarios(): List<Triple<Int, String, String>> {
        val aniversarios = mutableListOf<Triple<Int, String, String>>()
        val db = readableDatabase
        try {
            val cursor = db.query(
                TABLE_TESTBIRTH,
                arrayOf(COL_ID, COL_NAME, COL_BDATE),
                null,
                null,
                null,
                null,
                "strftime('%m%d', $COL_BDATE) ASC, $COL_NAME ASC"
            )
            Log.d("DatabaseHelper", "Registros encontrados na tabela testbirth: ${cursor.count}")
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)) ?: ""
                val bdate = cursor.getString(cursor.getColumnIndexOrThrow(COL_BDATE)) ?: ""
                Log.d("DatabaseHelper", "Compromisso: id=$id, name=$name, bdate=$bdate")
                aniversarios.add(Triple(id, name, bdate))
            }
            cursor.close()
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro ao listar compromissos: ${e.message}")
        } finally {
            db.close()
        }
        return aniversarios
    }

    fun updateAniversario(id: Int, name: String, bdate: String): Boolean {
        val db = writableDatabase
        try {
            // Validar e formatar data para yyyy-MM-dd
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            inputFormat.isLenient = false
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = try {
                inputFormat.parse(bdate)?.let { outputFormat.format(it) } ?: return false
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Formato de data inválido: $bdate, erro: ${e.message}")
                return false
            }

            val values = ContentValues().apply {
                put(COL_NAME, name)
                put(COL_BDATE, parsedDate)
            }
            val result = db.update(TABLE_TESTBIRTH, values, "$COL_ID = ?", arrayOf(id.toString()))
            Log.d(
                "DatabaseHelper",
                "Atualizado compromisso: id=$id, name=$name, bdate=$parsedDate, resultado: $result"
            )
            return result > 0
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro ao atualizar compromisso: ${e.message}")
            return false
        } finally {
            db.close()
        }
    }

    fun deleteAniversario(id: Int): Boolean {
        val db = writableDatabase
        try {
            val result = db.delete(TABLE_TESTBIRTH, "$COL_ID = ?", arrayOf(id.toString()))
            Log.d("DatabaseHelper", "Excluído compromisso: id=$id, resultado: $result")
            return result > 0
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro ao excluir compromisso: ${e.message}")
            return false
        } finally {
            db.close()
        }
    }

    data class MovimentoSimples(val descricao: String, val valor: Double)

    data class RelatorioPainel(
        var saldoInicial: Double = 0.0,
        var entradas: MutableList<MovimentoSimples> = mutableListOf(),
        var saidas: MutableList<MovimentoSimples> = mutableListOf(),
        var saldoFinalDia: Double = 0.0,
        var reserva: Double = 500.00,
        var sugestaoAplicar: Double = 0.0,
        var sugestaoResgate: Double = 0.0,
        var saldoPoupanca: Double = 0.0,
        var alerta: String = "",
        var menorSaldoProjetado: Double = 0.0,
        var dataMenorSaldo: String = "",
        var sugestaoSeguraAplicar: Double = 0.0,
        var totalEntradasDia: Double = 0.0,
        var totalSaidasDia: Double = 0.0
    ) {
        fun totalEntradas(): Double = totalEntradasDia
        fun totalSaidas(): Double = totalSaidasDia
    }

    fun gerarRelatorioDiario(data: String, reserva: Double = 500.00): RelatorioPainel {

        val rel = RelatorioPainel(reserva = reserva)
        // Formatos para moeda e data
        val currencyFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
        val dfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dfOutput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val db = readableDatabase

            // 1. Saldo Inicial (Bancos e Caixa)
            val saldoInicialQuery = "SELECT COALESCE(SUM(saldos), 0.0) FROM vsaldos WHERE conta IN ('Bancos', 'Caixa')"
            db.rawQuery(saldoInicialQuery, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    rel.saldoInicial = cursor.getDouble(0)
                }
            }

            // 2. Entradas Pendentes (Vencidas e Hoje)
            val entradasQuery = """
                SELECT Descr, Valor FROM view_movimento 
                WHERE dtVcto <= ? AND nome_C = 'Contas à Receber' AND (dtApr IS NULL OR dtApr = '')
                ORDER BY dtVcto ASC
            """.trimIndent()
            db.rawQuery(entradasQuery, arrayOf(data)).use { cursor ->
                while (cursor.moveToNext()) {
                    val valor = cursor.getDouble(1)
                    rel.entradas.add(MovimentoSimples(cursor.getString(0) ?: "", valor))
                    rel.totalEntradasDia += valor
                }
            }

            // 3. Saídas Pendentes (Vencidas e Hoje)
            val saidasQuery = """
                SELECT Descr, Valor 
                FROM view_movimento 
                WHERE dtVcto <= ? AND (
                    (nome_C = 'Contas à Pagar' AND (dtApr IS NULL OR dtApr = ''))
                    OR 
                    ((recurso = '0079' OR vrecurso = '2.001.003') AND (dtApr IS NULL OR dtApr = ''))
                )
                AND statusMov NOT IN ('PG', 'TD', 'SI')
                ORDER BY dtVcto ASC
            """.trimIndent()
            db.rawQuery(saidasQuery, arrayOf(data)).use { cursor ->
                while (cursor.moveToNext()) {
                    val valor = cursor.getDouble(1)
                    rel.saidas.add(MovimentoSimples(cursor.getString(0) ?: "", valor))
                    rel.totalSaidasDia += valor
                }
            }

            val pouancaQuery = "SELECT COALESCE(SUM(saldos), 0.0) FROM vsaldos WHERE conta = 'Conta Poupança'"
            db.rawQuery(pouancaQuery, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    rel.saldoPoupanca = cursor.getDouble(0)
                }
            }

            rel.saldoFinalDia = rel.saldoInicial + rel.totalEntradas() + rel.totalSaidas()

            var saldoAcumulado = rel.saldoFinalDia
            rel.menorSaldoProjetado = saldoAcumulado
            rel.dataMenorSaldo = data

            val futuroQuery = """
                SELECT dtVcto, SUM(Valor) as total_dia FROM view_movimento
                WHERE dtVcto > ? AND (dtApr IS NULL OR dtApr = '')
                GROUP BY dtVcto ORDER BY dtVcto ASC LIMIT 120
            """.trimIndent()
            db.rawQuery(futuroQuery, arrayOf(data)).use { cursor ->
                while (cursor.moveToNext()) {
                    val movDia = cursor.getDouble(1)
                    saldoAcumulado += movDia

                    if (saldoAcumulado < rel.menorSaldoProjetado) {
                        rel.menorSaldoProjetado = saldoAcumulado
                        rel.dataMenorSaldo = cursor.getString(0)
                    }
                }
            }

            when {
                rel.saldoFinalDia > 0 -> {
                    if (rel.saldoFinalDia > rel.reserva) {
                        rel.sugestaoAplicar = rel.saldoFinalDia - rel.reserva
                    }

                    val margemNecessaria = if (rel.menorSaldoProjetado < rel.saldoFinalDia) {
                        rel.saldoFinalDia - rel.menorSaldoProjetado
                    } else 0.0

                    val totalRetencao = rel.reserva + margemNecessaria
                    rel.sugestaoSeguraAplicar = if (rel.saldoFinalDia > totalRetencao) {
                        rel.saldoFinalDia - totalRetencao
                    } else 0.0

                    val currencyFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
                    rel.alerta = when {
                        rel.menorSaldoProjetado < 0 -> "Fluxo futuro indica queda para ${currencyFormat.format(rel.menorSaldoProjetado)} em ${rel.dataMenorSaldo}"
                        rel.sugestaoSeguraAplicar <= 0 -> "Saldo projeto futuro requer toda a margem."
                        else -> "Aplicar em poupança: ${currencyFormat.format(rel.sugestaoSeguraAplicar)}"
                    }
                }
                rel.saldoFinalDia < 0 -> {
                    rel.sugestaoResgate = -rel.saldoFinalDia
                    rel.alerta = if (rel.sugestaoResgate > rel.saldoPoupanca) {
                        "SALDO INSUFICIENTE NA POUPANÇA! Sugestão: Tomar Empréstimo."
                    } else {
                        "Considere resgate de ${currencyFormat.format(rel.sugestaoResgate)} da poupança."
                    }
                }
                else -> {
                    rel.alerta = "Saldo zero. Nenhuma sugestão."
                }
            }

Log.d("DatabaseHelper", "Relatório diário gerado para $data")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao gerar relatório diário: ${e.message}", e)
            rel.alerta = "Erro ao gerar relatório: ${e.message}"
        }
        return rel
    }

    // === MÉTODOS PARA TB_PLANO_DIRETOR ===
    fun salvarPlanoDiretor(
        anoReferencia: Int,
        mes: Int,
        classificacao: String,
        conta: String,
        valorPlanejado: Double,
        inflacaoPremissa: Double
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PD_ANO_REFERENCIA, anoReferencia)
            put(COL_PD_MES, mes)
            put(COL_PD_CLASSIFICACAO, classificacao)
            put(COL_PD_CONTA, conta)
            put(COL_PD_VALOR_PLANEJADO, valorPlanejado)
            put(COL_PD_INFLACAO_PREMISSA, inflacaoPremissa)
        }
        return db.insert(TABLE_PLANO_DIRETOR, null, values)
    }

    fun salvarPlanoDiretorBatch(
        registros: List<PlanoDiretorRecord>
    ): Int {
        var insertedCount = 0
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (registro in registros) {
                val values = ContentValues().apply {
                    put(COL_PD_ANO_REFERENCIA, registro.anoReferencia)
                    put(COL_PD_MES, registro.mes)
                    put(COL_PD_CLASSIFICACAO, registro.classificacao)
                    put(COL_PD_CONTA, registro.conta)
                    put(COL_PD_VALOR_PLANEJADO, registro.valorPlanejado)
                    put(COL_PD_INFLACAO_PREMISSA, registro.inflacaoPremissa)
                }
                val result = db.insert(TABLE_PLANO_DIRETOR, null, values)
                if (result != -1L) insertedCount++
            }
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Erro ao salvar plano diretor em batch: ${e.message}")
        } finally {
            db.endTransaction()
        }
        return insertedCount
    }

    fun getPlanoDiretor(anoReferencia: Int): List<PlanoDiretorRecord> {
        val registros = mutableListOf<PlanoDiretorRecord>()
        val db = readableDatabase
        val query = """
            SELECT $COL_PD_ANO_REFERENCIA, $COL_PD_MES, $COL_PD_CLASSIFICACAO, 
                   $COL_PD_CONTA, $COL_PD_VALOR_PLANEJADO, $COL_PD_INFLACAO_PREMISSA
            FROM $TABLE_PLANO_DIRETOR
            WHERE $COL_PD_ANO_REFERENCIA = ?
            ORDER BY $COL_PD_MES, $COL_PD_CONTA
        """.trimIndent()
        db.rawQuery(query, arrayOf(anoReferencia.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                registros.add(
                    PlanoDiretorRecord(
                        anoReferencia = cursor.getInt(0),
                        mes = cursor.getInt(1),
                        classificacao = cursor.getString(2) ?: "",
                        conta = cursor.getString(3) ?: "",
                        valorPlanejado = cursor.getDouble(4),
                        inflacaoPremissa = cursor.getDouble(5)
                    )
                )
            }
        }
        return registros
    }

    fun getPlanoDiretorPorPeriodo(anoReferencia: Int, mesInicio: Int, mesFim: Int): List<PlanoDiretorRecord> {
        val registros = mutableListOf<PlanoDiretorRecord>()
        val db = readableDatabase
        val query = """
            SELECT $COL_PD_ANO_REFERENCIA, $COL_PD_MES, $COL_PD_CLASSIFICACAO, 
                   $COL_PD_CONTA, $COL_PD_VALOR_PLANEJADO, $COL_PD_INFLACAO_PREMISSA
            FROM $TABLE_PLANO_DIRETOR
            WHERE $COL_PD_ANO_REFERENCIA = ? AND $COL_PD_MES BETWEEN ? AND ?
            ORDER BY $COL_PD_MES, $COL_PD_CONTA
        """.trimIndent()
        db.rawQuery(query, arrayOf(anoReferencia.toString(), mesInicio.toString(), mesFim.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                registros.add(
                    PlanoDiretorRecord(
                        anoReferencia = cursor.getInt(0),
                        mes = cursor.getInt(1),
                        classificacao = cursor.getString(2) ?: "",
                        conta = cursor.getString(3) ?: "",
                        valorPlanejado = cursor.getDouble(4),
                        inflacaoPremissa = cursor.getDouble(5)
                    )
                )
            }
        }
        return registros
    }

    fun deletePlanoDiretor(anoReferencia: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_PLANO_DIRETOR, "$COL_PD_ANO_REFERENCIA = ?", arrayOf(anoReferencia.toString()))
    }

    fun getInflacaoPremissa(anoReferencia: Int): Double {
        val db = readableDatabase
        val query = "SELECT $COL_PD_INFLACAO_PREMISSA FROM $TABLE_PLANO_DIRETOR WHERE $COL_PD_ANO_REFERENCIA = ? LIMIT 1"
        db.rawQuery(query, arrayOf(anoReferencia.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0)
            }
        }
        return 0.0
    }

}

data class PlanoDiretorRecord(
    val anoReferencia: Int,
    val mes: Int,
    val classificacao: String,
    val conta: String,
    val valorPlanejado: Double,
    val inflacaoPremissa: Double
)