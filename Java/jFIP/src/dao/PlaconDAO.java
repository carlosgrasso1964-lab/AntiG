package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.Placon;
import utilitarios.Conexao;

public class PlaconDAO {

    // ==================== SALVAR ====================
    public void salvar(Placon p) throws SQLException {
        if (p.getCod_Geral() == null || p.getCod_Geral().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Código da conta em branco!");
            return;
        }

        String sql = "INSERT INTO gpprincipal (cod_Geral, nome_P, nome_S, nome_C) VALUES (?, ?, ?, ?)";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCod_Geral().trim());
            ps.setString(2, p.getNome_P());
            ps.setString(3, p.getNome_S());
            ps.setString(4, p.getNome_C());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Conta cadastrada com sucesso!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar conta: " + e.getMessage());
            throw e;
        }
    }

    // ==================== ATUALIZAR ====================
    public void atualizar(Placon p) throws SQLException {
        String sql = "UPDATE gpprincipal SET nome_P = ?, nome_S = ?, nome_C = ? WHERE cod_Geral = ?";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNome_P());
            ps.setString(2, p.getNome_S());
            ps.setString(3, p.getNome_C());
            ps.setString(4, p.getCod_Geral().trim());

            int linhas = ps.executeUpdate();
            if (linhas > 0) {
                JOptionPane.showMessageDialog(null, "Conta atualizada com sucesso!");
            } else {
                JOptionPane.showMessageDialog(null, "Conta não encontrada para atualização.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar conta: " + e.getMessage());
            throw e;
        }
    }

    // ==================== EXCLUIR ====================
    public void excluir(String cod_Geral) throws SQLException {
        String sql = "DELETE FROM gpprincipal WHERE cod_Geral = ?";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cod_Geral.trim());
            int linhas = ps.executeUpdate();

            if (linhas > 0) {
                JOptionPane.showMessageDialog(null, "Conta excluída com sucesso!");
            } else {
                JOptionPane.showMessageDialog(null, "Conta não encontrada para exclusão.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir conta: " + e.getMessage());
            throw e;
        }
    }

    // ==================== BUSCAR POR CÓDIGO ====================
    public Placon buscarPorCodigo(String cod_Geral) throws SQLException {
        String sql = "SELECT * FROM gpprincipal WHERE cod_Geral = ?";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cod_Geral.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Placon p = new Placon();
                    p.setCod_Geral(rs.getString("cod_Geral"));
                    p.setNome_P(rs.getString("nome_P"));
                    p.setNome_S(rs.getString("nome_S"));
                    p.setNome_C(rs.getString("nome_C"));
                    return p;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar conta: " + e.getMessage());
            throw e;
        }
        return null;
    }

    // ==================== LISTAR TODAS ====================
    public List<Placon> listarTodos() throws SQLException {
        List<Placon> lista = new ArrayList<>();
        String sql = "SELECT * FROM gpprincipal ORDER BY cod_Geral";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Placon p = new Placon();
                p.setCod_Geral(rs.getString("cod_Geral"));
                p.setNome_P(rs.getString("nome_P"));
                p.setNome_S(rs.getString("nome_S"));
                p.setNome_C(rs.getString("nome_C"));
                lista.add(p);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar contas: " + e.getMessage());
            throw e;
        }
        return lista;
    }

    // ==================== FILTRAR POR CÓDIGO ====================
    public List<Placon> filtrarPorCodigo(String cod_Geral) throws SQLException {
        List<Placon> lista = new ArrayList<>();
        String sql = "SELECT * FROM gpprincipal WHERE cod_Geral LIKE ? ORDER BY cod_Geral";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + cod_Geral.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Placon p = new Placon();
                    p.setCod_Geral(rs.getString("cod_Geral"));
                    p.setNome_P(rs.getString("nome_P"));
                    p.setNome_S(rs.getString("nome_S"));
                    p.setNome_C(rs.getString("nome_C"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }
}