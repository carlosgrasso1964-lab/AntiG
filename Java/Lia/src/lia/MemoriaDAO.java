package lia;

import java.sql.Connection;
import java.sql.Statement;

public class MemoriaDAO {

    public static void criarTabela() {
        String sql = """
        CREATE TABLE IF NOT EXISTS memoria (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            pergunta TEXT,
            resposta TEXT,
            data DATETIME DEFAULT CURRENT_TIMESTAMP
        );
        """;
        try (Connection conn = ConexaoSQLite.conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void salvar(String pergunta, String resposta) {
        String sql = "INSERT INTO memoria(pergunta, resposta) VALUES(?, ?)";
        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pergunta);
            ps.setString(2, resposta);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void salvar(String sujeito, String relacao, String objeto) {

        String sql = "INSERT INTO memoria_fatos (sujeito, relacao, objeto) VALUES (?, ?, ?)";

        //try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sujeito);
            ps.setString(2, relacao);
            ps.setString(3, objeto);
            //stmt.executeUpdate();

            var rs = ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String recuperarContexto(int limite) {
        StringBuilder contexto = new StringBuilder();
        String sql = """
        SELECT pergunta, resposta 
        FROM memoria 
        ORDER BY id DESC 
        LIMIT ?
        """;
        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            var rs = ps.executeQuery();
            while (rs.next()) {
                contexto.append("Usuário: ")
                        .append(rs.getString("pergunta"))
                        .append("\n");
                contexto.append("Lia: ")
                        .append(rs.getString("resposta"))
                        .append("\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contexto.toString();
    }

    public static void criarTabelaFatos() {
        String sql = """
            CREATE TABLE IF NOT EXISTS fatos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                chave TEXT,
                valor TEXT,
                data DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Connection conn = ConexaoSQLite.conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void criarTabelaFatosV2() {
        String sql = """
            CREATE TABLE IF NOT EXISTS fatos_v2 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sujeito TEXT,
                relacao TEXT,
                objeto TEXT,
                data DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Connection conn = ConexaoSQLite.conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void salvarFato(String chave, String valor) {
//
//        String sqlVerifica = "SELECT 1 FROM fatos WHERE chave = ? AND valor = ?";
//
//        String sql = "INSERT OR IGNORE INTO fatos(chave, valor) VALUES(?, ?)";
//
//        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement psCheck = conn.prepareStatement(sqlVerifica)) {
//
//            psCheck.setString(1, chave);
//            psCheck.setString(2, valor);
//
//            var rs = psCheck.executeQuery();
//
//            // Se já existe, não salva novamente
//            if (rs.next()) {
//                System.out.println("DEBUG: Fato já existe -> " + chave + " = " + valor);
//                return;
//            }
//
//            try (java.sql.PreparedStatement psInsert = conn.prepareStatement(sql)) {
//
//                psInsert.setString(1, chave);
//                psInsert.setString(2, valor);
//                psInsert.executeUpdate();
//
//                System.out.println("DEBUG: Salvando fato -> " + chave + " = " + valor);
//
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
    public static void salvarFato(String sujeito, String relacao, String objeto) {

        String sql = "INSERT INTO memoria_fatos (sujeito, relacao, objeto) VALUES (?, ?, ?)";

        //try (Connection conn = ConexaoSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sujeito);
            ps.setString(2, relacao);
            ps.setString(3, objeto);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String buscar(String sujeito, String relacao) {

        String sql = "SELECT objeto FROM memoria_fatos WHERE sujeito = ? AND relacao = ? LIMIT 1";

        //try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sujeito);
            ps.setString(2, relacao);

            //ResultSet rs = stmt.executeQuery();
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("objeto");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String buscarFato(String chave) {
        String sql = "SELECT valor FROM fatos WHERE chave = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chave);
            var rs = ps.executeQuery();
            //ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("valor");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String buscarMemoriaParecida(String pergunta) {
        StringBuilder contexto = new StringBuilder();
        String sql = """
        SELECT pergunta, resposta
        FROM memoria
        WHERE pergunta LIKE ?
        ORDER BY id DESC
        LIMIT 3
        """;
        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + pergunta + "%");
            var rs = ps.executeQuery();
            while (rs.next()) {
                contexto.append("Usuário: ")
                        .append(rs.getString("pergunta"))
                        .append("\n");

                contexto.append("Lia: ")
                        .append(rs.getString("resposta"))
                        .append("\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contexto.toString();
    }

    public static java.util.List<String> listarFatos(String chave) {
        java.util.List<String> lista = new java.util.ArrayList<>();
        String sql = "SELECT valor FROM fatos WHERE chave = ?";
        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chave);
            var rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("valor"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static int contarFatos(String chave) {
        String sql = "SELECT COUNT(*) as total FROM fatos WHERE chave = ?";
        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chave);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void salvarFatoTripla(String sujeito, String relacao, String objeto) {

        String sqlCheck = """
        SELECT 1 FROM fatos_v2
        WHERE sujeito = ? AND relacao = ? AND objeto = ?
    """;

        String sqlInsert = """
        INSERT INTO fatos_v2 (sujeito, relacao, objeto)
        VALUES (?, ?, ?)
    """;

        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {

            psCheck.setString(1, sujeito);
            psCheck.setString(2, relacao);
            psCheck.setString(3, objeto);

            var rs = psCheck.executeQuery();

            if (rs.next()) {
                System.out.println("DEBUG: Fato já existe -> "
                        + sujeito + " | " + relacao + " | " + objeto);
                return;
            }

            try (java.sql.PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {

                psInsert.setString(1, sujeito);
                psInsert.setString(2, relacao);
                psInsert.setString(3, objeto);
                psInsert.executeUpdate();

                System.out.println("DEBUG: Salvando tripla -> "
                        + sujeito + " | " + relacao + " | " + objeto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static java.util.List<String> buscarPorRelacao(String sujeito, String relacao) {

        java.util.List<String> lista = new java.util.ArrayList<>();

        String sql = """
        SELECT objeto FROM fatos_v2
        WHERE sujeito = ? AND relacao = ?
    """;

        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sujeito);
            ps.setString(2, relacao);

            var rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(rs.getString("objeto"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    public static int contarTripla(String sujeito, String relacao) {

        String sql = """
        SELECT COUNT(*) as total FROM fatos_v2
        WHERE sujeito = ? AND relacao = ?
    """;

        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sujeito);
            ps.setString(2, relacao);

            var rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static int contarPorSujeito(String sujeito) {

        String sql = "SELECT COUNT(*) FROM memoria_fatos WHERE sujeito = ?";

        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sujeito);

            //ResultSet rs = ps.executeQuery();
            var rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static java.util.List<String> listarPorSujeito(String sujeito) {

        java.util.List<String> lista = new java.util.ArrayList<>();

        String sql = "SELECT objeto FROM memoria_fatos WHERE sujeito = ?";

        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sujeito);

            //ResultSet rs = ps.executeQuery();
            var rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(rs.getString("objeto"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    public static String buscarUltimoSujeito() {

        String sql = "SELECT sujeito FROM memoria_triplas ORDER BY id DESC LIMIT 1";

        try (Connection conn = ConexaoSQLite.conectar(); java.sql.PreparedStatement stmt = conn.prepareStatement(sql); var rs = stmt.executeQuery()) {    

            if (rs.next()) {
                return rs.getString("sujeito");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
