package dao;

import utilitarios.Conexao;
import java.awt.HeadlessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import model.Placon;
import view.Tela_CadUsu;

/**
 *
 * @author CARLOS
 */
public class PlaconDAO {

    private Conexao conexao;

    public PlaconDAO() throws SQLException, ClassNotFoundException { //Metodo construtor (recebe o mesmo nome da Classe)
        //Instanciando a classe
        conexao = new Conexao();
    }

    public void Salvar(Placon obj) {
        if (obj.getCod_Geral().equals("")) {
            JOptionPane.showMessageDialog(null, "Conta em branco!");
        } else {
            try {
                conexao.abrirConexao(); // Abre a conexão
                Connection conn = conexao.getConexao(); // Obtém a conexão
                String sql = "INSERT INTO gpprincipal(cod_Geral,nome_P,nome_S,nome_C) VALUES (?,?,?,?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, obj.getCod_Geral());
                    stmt.setString(2, obj.getNome_P());
                    stmt.setString(3, obj.getNome_S());
                    stmt.setString(4, obj.getNome_C());
                    stmt.execute();
                    stmt.close();
                    conn.close();
                }
                JOptionPane.showMessageDialog(null, "Conta cadastrada com sucesso!");

            } catch (SQLException ex) {
                Logger.getLogger(Tela_CadUsu.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
            }
        }

    }

    public Placon Abrir(String cod_Geral) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * FROM gpprincipal WHERE cod_Geral =?";
            PreparedStatement stmt;
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cod_Geral);
            ResultSet rs = stmt.executeQuery();
            Placon obj = new Placon();
            if (rs.next()) {
                obj.setCod_Geral(rs.getString("cod_Geral"));
                obj.setNome_P(rs.getString("nome_P"));
                obj.setNome_S(rs.getString("nome_S"));
                obj.setNome_C(rs.getString("nome_C"));
            }
            rs.close();
            stmt.close();
            conn.close();
            return obj;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar Conta! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    public Placon BuscarConta(String cod_Geral) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * FROM gpprincipal WHERE cod_Geral =?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, cod_Geral);
            ResultSet rs = stmt.executeQuery();
            Placon obj = new Placon();
            if (rs.next()) {
                obj.setCod_Geral(rs.getString("cod_Geral"));
                obj.setNome_P(rs.getString("nome_P"));
                obj.setNome_S(rs.getString("nome_S"));
                obj.setNome_C(rs.getString("nome_C"));
            }
            rs.close();
            stmt.close();
            conn.close();
            return obj;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar Conta! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    public List<Placon> Listar() {
        List<Placon> lista = new ArrayList<>();
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * FROM gpprincipal ORDER BY cod_Geral";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Placon obj = new Placon();
                obj.setCod_Geral(rs.getString("cod_Geral"));
                obj.setNome_P(rs.getString("nome_P"));
                obj.setNome_S(rs.getString("nome_S"));
                obj.setNome_C(rs.getString("nome_C"));
                lista.add(obj);
            }
            rs.close();
            stmt.close();
            conn.close();
            return lista;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao criar Lista!!! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    public List<Placon> Filtrar(String cod_Geral) {
        List<Placon> lista = new ArrayList<>();
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * FROM gpprincipal WHERE cod_Geral =?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, cod_Geral);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Placon obj = new Placon();
                obj.setCod_Geral(rs.getString("cod_Geral"));
                obj.setNome_P(rs.getString("nome_P"));
                obj.setNome_S(rs.getString("nome_S"));
                obj.setNome_C(rs.getString("nome_C"));
                lista.add(obj);
            }
            rs.close();
            stmt.close();
            conn.close();
            return lista;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao criar Lista!!! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    public void Editar(Placon obj) { //Vai pegar os dados em Model Placon encapsular e enviar como objeto para DAO
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            // criar o SQL
            String sql = "UPDATE gpprincipal SET nome_P=?,nome_S=?,nome_C=? WHERE cod_Geral=?";

            try ( // prepara a conexao com o sql
                PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, obj.getNome_P());
                stmt.setString(2, obj.getNome_S());
                stmt.setString(3, obj.getNome_C());
                stmt.setString(4, obj.getCod_Geral());
                //executar o sql
                stmt.execute();
                //fechar a conexão
                stmt.close();
                conn.close();
                JOptionPane.showMessageDialog(null, "Conta alterada com sucesso!!!");
            }
        } catch (HeadlessException | SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao alterar Conta! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }

    public void Excluir(Placon obj) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "DELETE FROM gpprincipal WHERE cod_Geral=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, obj.getCod_Geral());
                stmt.execute();
                stmt.close();
                conn.close();
            }
            JOptionPane.showMessageDialog(null, "Conta excluída com sucesso!");
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir a Conta! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }
}
