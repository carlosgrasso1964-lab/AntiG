package dao;

import utilitarios.Conexao;
import utilitarios.Criptografar;
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
import model.Usuarios;
import view.Tela_CadUsu;

/**
 *
 * @author CARLOS
 */
public class UsuariosDAO {

    private Conexao conexao;

    public UsuariosDAO() throws SQLException, ClassNotFoundException { //Metodo construtor (recebe o mesmo nome da Classe)
        //Instanciando a classe
        conexao = new Conexao();
    }

    public void Salvar(Usuarios obj) {

        if (obj.getUsuario().equals("") || obj.getSenha().equals("")) {
            JOptionPane.showMessageDialog(null, "Usuário e/ou Senha em branco!");
            return;
        }

        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();

            // 1. Verificação de Duplicidade
            String sqlCheck = "SELECT COUNT(*) FROM dados_senhas WHERE usuario = ?";
            try (PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck)) {
                stmtCheck.setString(1, obj.getUsuario());
                ResultSet rs = stmtCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(null, "Este nome de usuário já existe!");
                    return; // Interrompe o salvamento
                }
            }

            // 2. Inserção (Já com a correção da senha que fizemos)
            String sql = "INSERT INTO dados_senhas(usuario, senha) VALUES (?,?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, obj.getUsuario());
                stmt.setString(2, obj.getSenha());
                stmt.execute();
            }

            JOptionPane.showMessageDialog(null, "Usuário cadastrado com sucesso!");

        } catch (SQLException ex) {
            Logger.getLogger(Tela_CadUsu.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao salvar: " + ex.getMessage());
        } finally {
            conexao.fecharConexao();
        }
    }

    public Usuarios Abrir(int id) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * FROM dados_senhas WHERE id = ?";
            PreparedStatement stmt;
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            Usuarios obj = new Usuarios();
            if (rs.next()) {
                obj.setId(rs.getInt("id"));
                obj.setUsuario(rs.getString("usuario"));
                obj.setSenha(rs.getString("senha"));
            }
            rs.close();
            stmt.close();
            conn.close();
            return obj;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar Usuário! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    public Usuarios BuscarUsuario(int id) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * FROM dados_senhas WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            Usuarios obj = new Usuarios();
            if (rs.next()) {
                obj.setId(rs.getInt("id"));
                obj.setUsuario(rs.getString("usuario"));
                obj.setSenha(rs.getString("senha"));
            }
            rs.close();
            stmt.close();
            conn.close();
            return obj;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar Usuario! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    public List<Usuarios> Listar() {
        List<Usuarios> lista = new ArrayList<>();
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * from dados_senhas";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Usuarios obj = new Usuarios();
                obj.setId(rs.getInt("id"));
                obj.setUsuario(rs.getString("usuario"));
                obj.setSenha(rs.getString("senha"));
                lista.add(obj);
            }
            return lista;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao criar Lista!!! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    public List<Usuarios> Filtrar(int id) {
        List<Usuarios> lista = new ArrayList<>();
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * from dados_senhas WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Usuarios obj = new Usuarios();
                obj.setId(rs.getInt("id"));
                obj.setUsuario(rs.getString("usuario"));
                obj.setSenha(rs.getString("senha"));
                lista.add(obj);
            }
            stmt.close();
            rs.close();
            conn.close();
            return lista;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao criar Lista!!! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    public void Editar(Usuarios obj) { //Vai pegar os dados em Model Clientes encapsular e enviar como objeto para DAO
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            // criar o SQL
            String sql = "UPDATE dados_senhas SET usuario=?, senha=? WHERE id=?";

            try ( // prepara a conexao com o sql
                PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, obj.getUsuario());
                stmt.setString(2, Criptografar.encriptografar(obj.getSenha()));
                stmt.setInt(3, obj.getId());
                //executar o sql
                stmt.execute();
                //fechar a conexão
                stmt.close();
                conn.close();
                JOptionPane.showMessageDialog(null, "Usuário alterado com sucesso!!!");
            }
        } catch (HeadlessException | SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao alterar Usuário! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }

    public void Excluir(Usuarios obj) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "DELETE FROM dados_senhas WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, obj.getId());
                stmt.execute();
                stmt.close();
                conn.close();
            }
            JOptionPane.showMessageDialog(null, "Usuário excluído com sucesso!");
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir o Usuário! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }
}
