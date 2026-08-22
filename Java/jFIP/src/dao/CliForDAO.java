package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import model.CliFor;
import model.Placon;
import utilitarios.Conexao;

public class CliForDAO {

    // ==================== SUGESTÃO DE CÓDIGO ====================
    public String suggestClienteCode() {
        String sql = """
            SELECT COALESCE(MIN(CAST(codCliFor AS UNSIGNED)) + 1, 1000) AS proximo
            FROM tbclifor
            WHERE CAST(codCliFor AS UNSIGNED) >= 1000
              AND CAST(codCliFor AS UNSIGNED) + 1 NOT IN (
                  SELECT CAST(codCliFor AS UNSIGNED) FROM tbclifor WHERE CAST(codCliFor AS UNSIGNED) >= 1000
              )
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next() && rs.getObject("proximo") != null) {
                return String.format("%04d", rs.getInt("proximo"));
            }
            return "1000"; // primeiro cliente

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao sugerir código de cliente: " + e.getMessage());
            return "1000";
        }
    }

    public String suggestFornecedorCode() {
        String sql = """
            SELECT COALESCE(MIN(CAST(codCliFor AS UNSIGNED)) + 1, 0) AS proximo
            FROM tbclifor
            WHERE CAST(codCliFor AS UNSIGNED) < 1000
              AND CAST(codCliFor AS UNSIGNED) + 1 NOT IN (
                  SELECT CAST(codCliFor AS UNSIGNED) FROM tbclifor WHERE CAST(codCliFor AS UNSIGNED) < 1000
              )
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next() && rs.getObject("proximo") != null) {
                int next = rs.getInt("proximo");
                if (next > 999) {
                    next = 0; // não ultrapassa 0999
                }
                return String.format("%04d", next);
            }
            return "0000"; // primeiro fornecedor

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao sugerir código de fornecedor: " + e.getMessage());
            return "0000";
        }
    }

    // ==================== SALVAR ====================
    public void salvar(CliFor c) throws SQLException {
        String sql = """
            INSERT INTO tbclifor (
                codCliFor, Tipo, nomeCliFor, apelidoCliFor, email, celular, telefone,
                cep, endereco, numero, complemento, bairro, cidade, estado,
                rg, cpf, contatoCliFor, obs, fkCliForGp
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            preencherStatement(ps, c);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Favorecido cadastrado com sucesso!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar: " + e.getMessage());
            throw e;
        }
    }

    // ==================== ATUALIZAR ====================
    public void atualizar(CliFor c) throws SQLException {
        String sql = """
            UPDATE tbclifor SET
                Tipo=?, nomeCliFor=?, apelidoCliFor=?, email=?, celular=?, telefone=?,
                cep=?, endereco=?, numero=?, complemento=?, bairro=?, cidade=?, estado=?,
                rg=?, cpf=?, contatoCliFor=?, obs=?, fkCliForGp=?
            WHERE codCliFor=?
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            preencherStatement(ps, c);
            ps.setString(19, c.getCodCliFor());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Favorecido atualizado com sucesso!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar: " + e.getMessage());
            throw e;
        }
    }

    // ==================== EXCLUIR ====================
    public void excluir(String codCliFor) throws SQLException {
        String sql = "DELETE FROM tbclifor WHERE codCliFor = ?";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codCliFor);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Favorecido excluído com sucesso!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir: " + e.getMessage());
            throw e;
        }
    }

    // ==================== BUSCAR POR CÓDIGO ====================
    public CliFor buscarPorCodigo(String codCliFor) throws SQLException {
        String sql = "SELECT * FROM tbclifor WHERE codCliFor = ?";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codCliFor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarObjeto(rs);
                }
            }
        }
        return null;
       //Recebe como String para não perder zeros à esquerda
        
    }

    public CliFor buscarPorCodigoPg(String codigo) throws SQLException {
        String sql = "SELECT * FROM tbclifor WHERE REPLACE(TRIM(codCliFor), ' ', '') = REPLACE(TRIM(?), ' ', '')";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CliFor c = new CliFor();
                    c.setCodCliFor(rs.getString("codCliFor").trim());
                    c.setNomeCliFor(rs.getString("nomeCliFor"));
                    c.setCpf(rs.getString("cpf"));
                    return c;
                }
            }
        }
        return null;
    }

    // ==================== LISTAR TODOS ====================
    public List<CliFor> listarTodos() throws SQLException {
        List<CliFor> lista = new ArrayList<>();
        String sql = """
            SELECT c.*, g.cod_Geral
            FROM tbclifor c
            LEFT JOIN gpprincipal g ON c.fkCliForGp = g.cod_Geral
            ORDER BY c.nomeCliFor
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(montarObjeto(rs));
            }
        }
        return lista;
    }

    // ==================== BUSCAR GRUPO PRINCIPAL ====================
    public Placon buscarGrupoPrincipal(String cod_Geral) throws SQLException {
        String sql = "SELECT cod_Geral FROM gpprincipal WHERE cod_Geral = ?";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cod_Geral);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Placon p = new Placon();
                    p.setCod_Geral(rs.getString("cod_Geral"));
                    return p;
                }
            }
        }
        return null;
    }

    public CliFor BuscarFornecedor(String nome) throws SQLException {

        String sql = "SELECT * FROM tbclifor WHERE nomeCliFor = ?";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement fs = con.prepareStatement(sql)) {

            fs.setString(1, nome);
            try (ResultSet rs = fs.executeQuery()) {
                if (rs.next()) {
                    CliFor obj = new CliFor();
                    obj.setCodCliFor(rs.getString("codCliFor"));
                    obj.setNomeCliFor(rs.getString("nomeCliFor"));
                    obj.setCpf(rs.getString("cpf"));
                    obj.setEmail(rs.getString("email"));
                    obj.setTelefone(rs.getString("telefone"));
                    obj.setCelular(rs.getString("celular"));
                    obj.setCep(rs.getString("cep"));
                    obj.setEndereco(rs.getString("endereco"));
                    obj.setNumero(rs.getInt("numero"));
                    obj.setComplemento(rs.getString("complemento"));
                    obj.setBairro(rs.getString("bairro"));
                    obj.setCidade(rs.getString("cidade"));
                    obj.setEstado(rs.getString("estado"));
                    return obj;
                }
            }
        }
        return null;
    }

    // ==================== MÉTODOS AUXILIARES ====================
    private void preencherStatement(PreparedStatement ps, CliFor c) throws SQLException {
        ps.setString(1, c.getCodCliFor());
        ps.setString(2, c.getTipo());
        ps.setString(3, c.getNomeCliFor());
        ps.setString(4, nullToEmpty(c.getApelidoCliFor()));
        ps.setString(5, nullToEmpty(c.getEmail()));
        ps.setString(6, nullToEmpty(c.getCelular()));
        ps.setString(7, nullToEmpty(c.getTelefone()));
        ps.setString(8, nullToEmpty(c.getCep()));
        ps.setString(9, nullToEmpty(c.getEndereco()));
        ps.setInt(10, c.getNumero());
        ps.setString(11, nullToEmpty(c.getComplemento()));
        ps.setString(12, nullToEmpty(c.getBairro()));
        ps.setString(13, nullToEmpty(c.getCidade()));
        ps.setString(14, nullToEmpty(c.getEstado()));
        ps.setString(15, nullToEmpty(c.getRg()));
        ps.setString(16, nullToEmpty(c.getCpf()));
        ps.setString(17, nullToEmpty(c.getContatoCliFor()));
        ps.setString(18, nullToEmpty(c.getObs()));
        ps.setString(19, c.getFkCliForGp() != null ? c.getFkCliForGp().getCod_Geral() : null);
    }

    private CliFor montarObjeto(ResultSet rs) throws SQLException {
        CliFor c = new CliFor();
        c.setCodCliFor(rs.getString("codCliFor"));
        c.setTipo(rs.getString("Tipo"));
        c.setNomeCliFor(rs.getString("nomeCliFor"));
        c.setApelidoCliFor(rs.getString("apelidoCliFor"));
        c.setEmail(rs.getString("email"));
        c.setCelular(rs.getString("celular"));
        c.setTelefone(rs.getString("telefone"));
        c.setCep(rs.getString("cep"));
        c.setEndereco(rs.getString("endereco"));
        c.setNumero(rs.getInt("numero"));
        c.setComplemento(rs.getString("complemento"));
        c.setBairro(rs.getString("bairro"));
        c.setCidade(rs.getString("cidade"));
        c.setEstado(rs.getString("estado"));
        c.setRg(rs.getString("rg"));
        c.setCpf(rs.getString("cpf"));
        c.setContatoCliFor(rs.getString("contatoCliFor"));
        c.setObs(rs.getString("obs"));

        String fk = rs.getString("fkCliForGp");
        if (fk != null) {
            Placon p = new Placon();
            p.setCod_Geral(fk);
            c.setFkCliForGp(p);
        }
        return c;
    }

    // ==================== BUSCAR POR ID (INT) ====================
    public CliFor buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM tbclifor WHERE CAST(codCliFor AS UNSIGNED) = ?";
        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarObjeto(rs);
                }
            }
        }
        return null; // não encontrado
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
