package dao;

import utilitarios.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import model.CliFor;
import model.Placon;
import view.Tela_CadCliFor;

/**
 *
 * @author CARLOS
 */
public class CliForDAO {

    private Conexao conexao;

    public CliForDAO() throws SQLException, ClassNotFoundException { //Metodo construtor (recebe o mesmo nome da Classe)
        //Instanciando a classe
        conexao = new Conexao();
    }

    public String suggestClienteCode() {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = """
                SELECT MIN(CAST(codCliFor AS INTEGER) + 1) AS next_code
                FROM (
                    SELECT 999 AS codCliFor UNION ALL
                    SELECT codCliFor FROM tbclifor WHERE CAST(codCliFor AS INTEGER) >= 1000
                ) AS nums
                WHERE NOT EXISTS (
                    SELECT 1 FROM tbclifor WHERE CAST(codCliFor AS INTEGER) = nums.codCliFor + 1
                )
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int nextCode = rs.getInt("next_code");
                    return String.format("%04d", nextCode); // Formata como "1000"
                }
            }
            return "1000"; // Caso não haja códigos disponíveis
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao sugerir código de cliente: " + ex.getMessage());
            return "1000";
        } finally {
            conexao.fecharConexao();
        }
    }

    public boolean isCodeUnique(String code) {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT 1 FROM tbclifor WHERE codCliFor = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, code);
                try (ResultSet rs = stmt.executeQuery()) {
                    return !rs.next(); // Retorna true se o código não existe
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao verificar unicidade do código: " + ex.getMessage());
            return false;
        } finally {
            conexao.fecharConexao();
        }
    }

    public void Salvar(CliFor obj) throws ParseException {
        if (obj.getCodCliFor().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Código em branco!");
            return;
        }
        // Validar formato do código
        try {
            int codeInt = Integer.parseInt(obj.getCodCliFor());
            if (obj.getCodCliFor().length() > 4) {
                JOptionPane.showMessageDialog(null, "Código inválido, use até 4 dígitos!");
                return;
            }
            if ("FOR".equals(obj.getTipo()) && (codeInt < 0 || codeInt > 999)) {
                JOptionPane.showMessageDialog(null, "Código de fornecedor deve estar entre 0000 e 0999!");
                return;
            } else if ("CLI".equals(obj.getTipo()) && codeInt < 1000) {
                JOptionPane.showMessageDialog(null, "Código de cliente deve ser maior ou igual a 1000!");
                return;
            }
            // Validar unicidade
            if (!isCodeUnique(obj.getCodCliFor())) {
                JOptionPane.showMessageDialog(null, "Código já está em uso!");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Código inválido, use apenas dígitos!");
            return;
        }
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "INSERT INTO tbclifor(codCliFor, Tipo, nomeCliFor, apelidoCliFor, email, celular, telefone, cep, endereco, numero, complemento, bairro, cidade, estado, rg, cpf, contatoCliFor, obs, fkCliForGp) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, obj.getCodCliFor());
                stmt.setString(2, obj.getTipo());
                stmt.setString(3, obj.getNomeCliFor());
                stmt.setString(4, obj.getApelidoCliFor());
                stmt.setString(5, obj.getEmail());
                stmt.setString(6, obj.getCelular());
                stmt.setString(7, obj.getTelefone());
                stmt.setString(8, obj.getCep());
                stmt.setString(9, obj.getEndereco());
                stmt.setInt(10, obj.getNumero());
                stmt.setString(11, obj.getComplemento());
                stmt.setString(12, obj.getBairro());
                stmt.setString(13, obj.getCidade());
                stmt.setString(14, obj.getEstado());
                stmt.setString(15, obj.getRg());
                stmt.setString(16, obj.getCpf());
                stmt.setString(17, obj.getContatoCliFor());
                stmt.setString(18, obj.getObs());
                if (obj.getFkCliForGp() != null) {
                    stmt.setString(19, obj.getFkCliForGp().getCod_Geral());
                } else {
                    stmt.setString(19, null);
                }
                stmt.execute();
            }
            JOptionPane.showMessageDialog(null, "Favorecido cadastrado com sucesso!");
        } catch (SQLException ex) {
            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao salvar favorecido: " + ex.getMessage());
        } finally {
            conexao.fecharConexao();
        }
    }

    public void Editar(CliFor obj) throws ParseException {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            // Validar formato do código
            try {
                int codeInt = Integer.parseInt(obj.getCodCliFor());
                if (obj.getCodCliFor().length() > 4) {
                    JOptionPane.showMessageDialog(null, "Código inválido, use até 4 dígitos!");
                    return;
                }
                if ("FOR".equals(obj.getTipo()) && (codeInt < 0 || codeInt > 999)) {
                    JOptionPane.showMessageDialog(null, "Código de fornecedor deve estar entre 0000 e 0999!");
                    return;
                } else if ("CLI".equals(obj.getTipo()) && codeInt < 1000) {
                    JOptionPane.showMessageDialog(null, "Código de cliente deve ser maior ou igual a 1000!");
                    return;
                }
                // Validar unicidade (exceto se for o mesmo código)
                CliFor existing = BuscarCliFor(obj.getCodCliFor());
                if (existing != null && !existing.getCodCliFor().equals(obj.getCodCliFor())) {
                    JOptionPane.showMessageDialog(null, "Código já está em uso!");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Código inválido, use apenas dígitos!");
                return;
            }
            String sql = "UPDATE tbclifor SET Tipo=?, nomeCliFor=?, apelidoCliFor=?, email=?, celular=?, telefone=?, "
                    + "cep=?, endereco=?, numero=?, complemento=?, bairro=?, cidade=?, estado=?, "
                    + "rg=?, cpf=?, contatoCliFor=?, obs=?, fkCliForGp=? WHERE codCliFor=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, obj.getTipo());
                stmt.setString(2, obj.getNomeCliFor());
                stmt.setString(3, obj.getApelidoCliFor());
                stmt.setString(4, obj.getEmail());
                stmt.setString(5, obj.getCelular());
                stmt.setString(6, obj.getTelefone());
                stmt.setString(7, obj.getCep());
                stmt.setString(8, obj.getEndereco());
                stmt.setInt(9, obj.getNumero());
                stmt.setString(10, obj.getComplemento());
                stmt.setString(11, obj.getBairro());
                stmt.setString(12, obj.getCidade());
                stmt.setString(13, obj.getEstado());
                stmt.setString(14, obj.getRg());
                stmt.setString(15, obj.getCpf());
                stmt.setString(16, obj.getContatoCliFor());
                stmt.setString(17, obj.getObs());
                if (obj.getFkCliForGp() != null) {
                    stmt.setString(18, obj.getFkCliForGp().getCod_Geral());
                } else {
                    stmt.setString(18, null);
                }
                stmt.setString(19, obj.getCodCliFor());
                stmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Favorecido alterado com sucesso!");
        } catch (SQLException ex) {
            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao alterar favorecido: " + ex.getMessage());
        } finally {
            conexao.fecharConexao();
        }
    }

    public void Excluir(CliFor obj) {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "DELETE FROM tbclifor WHERE codCliFor=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, obj.getCodCliFor());
                stmt.execute();
            }
            JOptionPane.showMessageDialog(null, "Favorecido excluído com sucesso!");
        } catch (SQLException ex) {
            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao excluir favorecido: " + ex.getMessage());
        } finally {
            conexao.fecharConexao();
        }
    }

    public CliFor BuscarCliFor(String codCliFor) {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT * FROM tbclifor WHERE codCliFor = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, codCliFor);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        CliFor obj = new CliFor();
                        obj.setCodCliFor(rs.getString("codCliFor"));
                        obj.setTipo(rs.getString("Tipo"));
                        obj.setNomeCliFor(rs.getString("nomeCliFor"));
                        obj.setApelidoCliFor(rs.getString("apelidoCliFor"));
                        obj.setEmail(rs.getString("email"));
                        obj.setCelular(rs.getString("celular"));
                        obj.setTelefone(rs.getString("telefone"));
                        obj.setCep(rs.getString("cep"));
                        obj.setEndereco(rs.getString("endereco"));
                        obj.setNumero(rs.getInt("numero"));
                        obj.setComplemento(rs.getString("complemento"));
                        obj.setBairro(rs.getString("bairro"));
                        obj.setCidade(rs.getString("cidade"));
                        obj.setEstado(rs.getString("estado"));
                        obj.setRg(rs.getString("rg"));
                        obj.setCpf(rs.getString("cpf"));
                        obj.setContatoCliFor(rs.getString("contatoCliFor"));
                        obj.setObs(rs.getString("obs"));
                        String fkCliForGp = rs.getString("fkCliForGp");
                        if (fkCliForGp != null) {
                            Placon placon = buscarGrupoPrincipal(fkCliForGp);
                            obj.setFkCliForGp(placon);
                        }
                        return obj;
                    }
                }
            }
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao buscar favorecido: " + ex.getMessage());
            return null;
        } finally {
            conexao.fecharConexao();
        }
    }

    public Placon buscarGrupoPrincipal(String cod_Geral) {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT * FROM gpprincipal WHERE cod_Geral=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cod_Geral);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Placon placon = new Placon();
                        placon.setCod_Geral(rs.getString("cod_Geral"));
                        return placon;
                    }
                }
            }
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao buscar código no Grupo Principal: " + ex.getMessage());
            return null;
        } finally {
            conexao.fecharConexao();
        }
    }

    public List<CliFor> Listar() {
        List<CliFor> lista = new ArrayList<>();
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT c.codCliFor, c.Tipo, c.nomeCliFor, c.apelidoCliFor, c.email, c.celular, c.telefone, "
                    + "c.cep, c.endereco, c.numero, c.complemento, c.bairro, c.cidade, c.estado, "
                    + "c.rg, c.cpf, c.contatoCliFor, c.obs, c.fkCliForGp, g.cod_Geral "
                    + "FROM tbclifor c "
                    + "LEFT JOIN gpprincipal g ON (c.fkCliForGp = g.cod_Geral)";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CliFor obj = new CliFor();
                    obj.setCodCliFor(rs.getString("codCliFor"));
                    obj.setTipo(rs.getString("Tipo"));
                    obj.setNomeCliFor(rs.getString("nomeCliFor"));
                    obj.setApelidoCliFor(rs.getString("apelidoCliFor"));
                    obj.setEmail(rs.getString("email"));
                    obj.setCelular(rs.getString("celular"));
                    obj.setTelefone(rs.getString("telefone"));
                    obj.setCep(rs.getString("cep"));
                    obj.setEndereco(rs.getString("endereco"));
                    obj.setNumero(rs.getInt("numero"));
                    obj.setComplemento(rs.getString("complemento"));
                    obj.setBairro(rs.getString("bairro"));
                    obj.setCidade(rs.getString("cidade"));
                    obj.setEstado(rs.getString("estado"));
                    obj.setRg(rs.getString("rg"));
                    obj.setCpf(rs.getString("cpf"));
                    obj.setContatoCliFor(rs.getString("contatoCliFor"));
                    obj.setObs(rs.getString("obs"));
                    String codGeral = rs.getString("cod_Geral");
                    if (codGeral != null) {
                        Placon g = new Placon();
                        g.setCod_Geral(codGeral);
                        obj.setFkCliForGp(g);
                    }
                    lista.add(obj);
                }
            }
            return lista;
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao criar lista: " + ex.getMessage());
            return lista;
        } finally {
            conexao.fecharConexao();
        }
    }

    public List<CliFor> Filtrar(String codCliFor) {
        List<CliFor> lista = new ArrayList<>();
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT c.codCliFor, c.Tipo, c.nomeCliFor, c.apelidoCliFor, c.email, c.celular, c.telefone, "
                    + "c.cep, c.endereco, c.numero, c.complemento, c.bairro, c.cidade, c.estado, "
                    + "c.rg, c.cpf, c.contatoCliFor, c.obs, c.fkCliForGp, g.cod_Geral "
                    + "FROM tbclifor c "
                    + "LEFT JOIN gpprincipal g ON (c.fkCliForGp = g.cod_Geral) "
                    + "WHERE c.codCliFor = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, codCliFor);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        CliFor obj = new CliFor();
                        obj.setCodCliFor(rs.getString("codCliFor"));
                        obj.setTipo(rs.getString("Tipo"));
                        obj.setNomeCliFor(rs.getString("nomeCliFor"));
                        obj.setApelidoCliFor(rs.getString("apelidoCliFor"));
                        obj.setEmail(rs.getString("email"));
                        obj.setCelular(rs.getString("celular"));
                        obj.setTelefone(rs.getString("telefone"));
                        obj.setCep(rs.getString("cep"));
                        obj.setEndereco(rs.getString("endereco"));
                        obj.setNumero(rs.getInt("numero"));
                        obj.setComplemento(rs.getString("complemento"));
                        obj.setBairro(rs.getString("bairro"));
                        obj.setCidade(rs.getString("cidade"));
                        obj.setEstado(rs.getString("estado"));
                        obj.setRg(rs.getString("rg"));
                        obj.setCpf(rs.getString("cpf"));
                        obj.setContatoCliFor(rs.getString("contatoCliFor"));
                        obj.setObs(rs.getString("obs"));
                        String codGeral = rs.getString("cod_Geral");
                        if (codGeral != null) {
                            Placon g = new Placon();
                            g.setCod_Geral(codGeral);
                            obj.setFkCliForGp(g);
                        }
                        lista.add(obj);
                    }
                }
            }
            return lista;
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao filtrar lista: " + ex.getMessage());
            return lista;
        } finally {
            conexao.fecharConexao();
        }
    }

    public List<CliFor> ListarF() {
        List<CliFor> lista = new ArrayList<>();
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT * FROM tbclifor WHERE Tipo = 'FOR' ORDER BY nomeCliFor";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
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
                    lista.add(obj);
                }
            }
            return lista;
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao criar lista: " + ex.getMessage());
            return null;
        } finally {
            conexao.fecharConexao();
        }
    }

    public CliFor BuscarFornecedor(String nome) {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT * FROM tbclifor WHERE nomeCliFor = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                try (ResultSet rs = stmt.executeQuery()) {
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
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao buscar fornecedor: " + ex.getMessage());
            return null;
        } finally {
            conexao.fecharConexao();
        }
    }

    public CliFor BuscarCliente(String nome) {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT * FROM tbclifor WHERE nomeCliFor = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        CliFor obj = new CliFor();
                        obj.setCodCliFor(rs.getString("codCliFor"));
                        obj.setNomeCliFor(rs.getString("nomeCliFor"));
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
                        obj.setRg(rs.getString("rg"));
                        obj.setCpf(rs.getString("cpf"));
                        return obj;
                    }
                }
            }
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao buscar cliente: " + ex.getMessage());
            return null;
        } finally {
            conexao.fecharConexao();
        }
    }

    public CliFor BuscarClienteCPF(String cpf) {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT * FROM tbclifor WHERE cpf = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cpf);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        CliFor obj = new CliFor();
                        obj.setCodCliFor(rs.getString("codCliFor"));
                        obj.setNomeCliFor(rs.getString("nomeCliFor"));
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
                        obj.setRg(rs.getString("rg"));
                        obj.setCpf(rs.getString("cpf"));
                        return obj;
                    }
                }
            }
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao buscar cliente: " + ex.getMessage());
            return null;
        } finally {
            conexao.fecharConexao();
        }
    }

    public List<CliFor> ListarC() {
        List<CliFor> lista = new ArrayList<>();
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT * FROM tbclifor WHERE Tipo = 'CLI' ORDER BY nomeCliFor";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CliFor obj = new CliFor();
                    obj.setCodCliFor(rs.getString("codCliFor"));
                    obj.setNomeCliFor(rs.getString("nomeCliFor"));
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
                    obj.setRg(rs.getString("rg"));
                    obj.setCpf(rs.getString("cpf"));
                    lista.add(obj);
                }
            }
            return lista;
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao criar lista: " + ex.getMessage());
            return null;
        } finally {
            conexao.fecharConexao();
        }
    }

    public List<CliFor> FiltrarC(String nome) {
        List<CliFor> lista = new ArrayList<>();
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT * FROM tbclifor WHERE nomeCliFor LIKE ? ORDER BY nomeCliFor";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        CliFor obj = new CliFor();
                        obj.setCodCliFor(rs.getString("codCliFor"));
                        obj.setNomeCliFor(rs.getString("nomeCliFor"));
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
                        obj.setRg(rs.getString("rg"));
                        obj.setCpf(rs.getString("cpf"));
                        lista.add(obj);
                    }
                }
            }
            return lista;
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao criar lista: " + ex.getMessage());
            return null;
        } finally {
            conexao.fecharConexao();
        }
    }

    public String suggestFornecedorCode() {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = """
                SELECT MIN(CAST(codCliFor AS INTEGER) + 1) AS next_code
                FROM (
                    SELECT -1 AS codCliFor UNION ALL
                    SELECT codCliFor FROM tbclifor WHERE CAST(codCliFor AS INTEGER) BETWEEN 0 AND 999
                ) AS nums
                WHERE NOT EXISTS (
                    SELECT 1 FROM tbclifor WHERE CAST(codCliFor AS INTEGER) = nums.codCliFor + 1
                ) AND nums.codCliFor + 1 <= 999
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int nextCode = rs.getInt("next_code");
                    return String.format("%04d", nextCode); // Formata como "0009"
                }
            }
            return "0000"; // Caso não haja códigos disponíveis
        } catch (SQLException ex) {
            Logger.getLogger(CliForDAO.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao sugerir código de fornecedor: " + ex.getMessage());
            return "0000";
        } finally {
            conexao.fecharConexao();
        }
    }
}