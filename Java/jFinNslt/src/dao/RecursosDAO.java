package dao;

import utilitarios.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import model.Placon;
import model.Recursos;
import view.Tela_CadRecursos;

/**
 *
 * @author CARLOS
 */
public class RecursosDAO {

    private Conexao conexao;

    public RecursosDAO() throws SQLException, ClassNotFoundException { //Metodo construtor (recebe o mesmo nome da Classe)
        //Instanciando a classe
         conexao = new Conexao();
    }

    public void Salvar(Recursos obj) throws ParseException {
        if (obj.getCodigo().equals("")) {
            JOptionPane.showMessageDialog(null, "Código em branco!");
        } else {
            try {
                conexao.abrirConexao(); // Abre a conexão
                Connection conn = conexao.getConexao(); // Obtém a conexão
                String sql = "INSERT INTO tbrecursos(codigo,nomebco,agencia,fluxo,limite,abertura,encerramento,status,fk_gpprinc) VALUES (?,?,?,?,?,?,?,?,?)";
                SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat formatoSaida = new SimpleDateFormat("yyyy-MM-dd");
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, obj.getCodigo());
                    stmt.setString(2, obj.getNomebco());
                    stmt.setString(3, obj.getAgencia());
                    stmt.setString(4, obj.getFluxo());
                    stmt.setDouble(5, obj.getLimite());
                    // Converte a data de abertura para o formato ano-mes-dia
                    String dataAberturaTexto = obj.getAbertura();
                    java.util.Date dataAberturaUtil = formatoEntrada.parse(dataAberturaTexto);
                    String dataAberturaSql = formatoSaida.format(dataAberturaUtil);
                    stmt.setString(6, dataAberturaSql);
                    // Converte a data de encerramento para o formato ano-mes-dia
                    String dataEncerramentoTexto = obj.getEncerramento();
                    java.util.Date dataEncerramentoUtil = formatoEntrada.parse(dataEncerramentoTexto);
                    String dataEncerramentoSql = formatoSaida.format(dataEncerramentoUtil);
                    stmt.setString(7, dataEncerramentoSql);
                    stmt.setString(8, obj.getStatus());
                    stmt.setString(9, obj.getFk_gpprinc().getCod_Geral());
                    stmt.execute();
                    stmt.close();
                    conn.close();
                }
                JOptionPane.showMessageDialog(null, "Conta cadastrada com sucesso!");

            } catch (SQLException ex) {
                Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
            }
        }
    }

    public Recursos Abrir(String codigo) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * FROM tbrecursos WHERE codigo=? BY ASC";
            PreparedStatement stmt;
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            Recursos obj = new Recursos();
            if (rs.next()) {
                obj.setCodigo(rs.getString("codigo"));
                obj.setNomebco(rs.getString("nomebco"));
                obj.setAgencia(rs.getString("agencia"));
                obj.setFluxo(rs.getString("fluxo"));
                obj.setLimite(rs.getDouble("limite"));
                obj.setAbertura(rs.getString("abertura"));
                obj.setEncerramento(rs.getString("encerramento"));
                obj.setStatus(rs.getString("status"));
                // Buscar a FK na tabela grupoprincipal
                String fk_gpprinc = rs.getString("fk_gpprinc");
                Placon placon = buscarGrupoPrincipal(fk_gpprinc);
                // Definir a FK como objeto GrupoPrincipal
                obj.setFk_gpprinc(placon);
            }
            rs.close();
            stmt.close();
            conn.close();
            return obj;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar Recurso! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    // Método para buscar o Plano de Contas pelo código
    public Placon buscarGrupoPrincipal(String cod_Geral) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * FROM gpprincipal WHERE cod_Geral=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, cod_Geral);
            ResultSet rs = stmt.executeQuery();
            Placon placon = null;
            if (rs.next()) {
                placon = new Placon();
                placon.setCod_Geral(rs.getString("cod_Geral"));
            }
            rs.close();
            stmt.close();
            return placon;

        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar código no Grupo Principal! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    public Recursos BuscarRecursos(String codigo) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * FROM tbrecursos WHERE codigo =?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            Recursos obj = new Recursos();
            if (rs.next()) {
                obj.setCodigo(rs.getString("codigo"));
                obj.setNomebco(rs.getString("nomebco"));
                obj.setAgencia(rs.getString("agencia"));
                obj.setFluxo(rs.getString("fluxo"));
                obj.setLimite(rs.getDouble("limite"));
                obj.setAbertura(rs.getString("abertura"));
                obj.setEncerramento(rs.getString("encerramento"));
                obj.setStatus(rs.getString("status"));
                // Buscar a FK na tabela grupoprincipal
                String fk_gpprinc = rs.getString("fk_gpprinc");
                Placon placon = buscarGrupoPrincipal(fk_gpprinc);
                // Definir a FK como objeto GrupoPrincipal
                obj.setFk_gpprinc(placon);
            }
            rs.close();
            stmt.close();
            conn.close();
            return obj;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar Recurso ! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return null;
    }

    public List<Recursos> Listar() {
        List<Recursos> lista = new ArrayList<>();
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            
            String sql = "SELECT r.codigo, r.nomebco, r.agencia, r.fluxo, r.limite, r.abertura, r.encerramento, r.status, r.fk_gpprinc, g.cod_Geral FROM tbrecursos r "
                    + "INNER JOIN gpprincipal g ON (r.fk_gpprinc = g.cod_Geral) "
                    + "ORDER BY r.codigo ASC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Recursos obj = new Recursos();
                Placon g = new Placon();
                
                g.setCod_Geral(rs.getString("Cod_Geral"));
                
                obj.setCodigo(rs.getString("codigo"));
                obj.setNomebco(rs.getString("nomebco"));
                obj.setAgencia(rs.getString("agencia"));
                obj.setFluxo(rs.getString("fluxo"));
                obj.setLimite(rs.getDouble("limite"));
                obj.setAbertura(rs.getString("abertura"));
                obj.setEncerramento(rs.getString("encerramento"));
                obj.setStatus(rs.getString("status"));
                // Buscar a FK na tabela grupoprincipal
                //String fk_gpprinc = rs.getString("fk_gpprinc");
                //Placon placon = buscarGrupoPrincipal(fk_gpprinc);
                // Definir a FK como objeto GrupoPrincipal
                obj.setFk_gpprinc(g);
                lista.add(obj);
            }
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao criar Lista!!! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return lista;
        //return null;
    }

    public List<Recursos> Filtrar(String codigo) {
        List<Recursos> lista = new ArrayList<>();
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT * FROM tbrecursos WHERE codigo = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Recursos obj = new Recursos();
                obj.setCodigo(rs.getString("codigo"));
                obj.setNomebco(rs.getString("nomebco"));
                obj.setAgencia(rs.getString("agencia"));
                obj.setFluxo(rs.getString("fluxo"));
                obj.setLimite(rs.getDouble("limite"));
                obj.setAbertura(rs.getString("abertura"));
                obj.setEncerramento(rs.getString("encerramento"));
                obj.setStatus(rs.getString("status"));
                // Buscar a FK na tabela grupoprincipal
                String fk_gpprinc = rs.getString("fk_gpprinc");
                Placon placon = buscarGrupoPrincipal(fk_gpprinc);
                // Definir a FK como objeto GrupoPrincipal
                obj.setFk_gpprinc(placon);
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

    public void Editar(Recursos obj) throws ParseException { //Vai pegar os dados em Model Recursos encapsular e enviar como objeto para DAO
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            // SQL de update
            String sql = "UPDATE tbrecursos SET nomebco=?, agencia=?, fluxo=?, limite=?, abertura=?, encerramento=?, status=?, fk_gpprinc=? WHERE codigo=?";
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat formatoSaida = new SimpleDateFormat("yyyy-MM-dd");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, obj.getNomebco());
            stmt.setString(2, obj.getAgencia());
            stmt.setString(3, obj.getFluxo());
            stmt.setDouble(4, obj.getLimite());
            // Conversão da data de abertura
            if (obj.getAbertura() != null && !obj.getAbertura().isEmpty()) {
                java.util.Date dataAberturaUtil = formatoEntrada.parse(obj.getAbertura());
                String dataAberturaSql = formatoSaida.format(dataAberturaUtil);
                stmt.setString(5, dataAberturaSql);
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            // Conversão da data de encerramento
            if (obj.getEncerramento() != null && !obj.getEncerramento().isEmpty()) {
                java.util.Date dataEncerramentoUtil = formatoEntrada.parse(obj.getEncerramento());
                String dataEncerramentoSql = formatoSaida.format(dataEncerramentoUtil);
                stmt.setString(6, dataEncerramentoSql);
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }
            stmt.setString(7, obj.getStatus());
            stmt.setString(8, obj.getFk_gpprinc().getCod_Geral());
            stmt.setString(9, obj.getCodigo());
            stmt.executeUpdate();
            stmt.close();
            conn.close();
            JOptionPane.showMessageDialog(null, "Recurso alterado com sucesso!");
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "Erro ao converter a data! " + e.getMessage());
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao alterar Recurso! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }

    public void Excluir(Recursos obj) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "DELETE FROM tbrecursos WHERE codigo=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, obj.getCodigo());
                stmt.execute();
                stmt.close();
                conn.close();
            }
            JOptionPane.showMessageDialog(null, "Recurso excluído com sucesso!");
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir o Recurso! " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }
}
