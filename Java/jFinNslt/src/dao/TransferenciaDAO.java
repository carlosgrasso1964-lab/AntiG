
package dao;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import model.Transferencia;
import utilitarios.Conexao;

public class TransferenciaDAO {

    private Conexao conexao;
    
    public TransferenciaDAO() {
        conexao = new Conexao();
    }

    public boolean registrarTransferencia(Transferencia transferencia) {
        String sql = "INSERT INTO tbmovimento (recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento, classif, Descr, valor, dtApr, statusMov, Prev) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        SimpleDateFormat formato_ISO = new SimpleDateFormat("yyyy-MM-dd");
        try {
            conexao.abrirConexao();
            Connection con = conexao.getConexao();
            
            // Transferência de saída (Débito)
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, transferencia.getOrigemNr());
                stmt.setString(2, transferencia.getOrigemClass());
                stmt.setString(3, transferencia.getOrigemNr());
                stmt.setString(4, transferencia.getOrigemClass());
                stmt.setString(5, formato_ISO.format(new SimpleDateFormat("dd/MM/yyyy").parse(transferencia.getData())));
                stmt.setString(6, formato_ISO.format(new SimpleDateFormat("dd/MM/yyyy").parse(transferencia.getData())));
                stmt.setString(7, formato_ISO.format(new SimpleDateFormat("dd/MM/yyyy").parse(transferencia.getData())));
                stmt.setString(8, transferencia.getDocumento());
                stmt.setString(9, "9.001.001");
                stmt.setString(10, "TRANSFERIDO PARA: " + transferencia.getDestinoNr());
                stmt.setDouble(11, -transferencia.getValor());
                stmt.setString(12, formato_ISO.format(new SimpleDateFormat("dd/MM/yyyy").parse(transferencia.getData())));
                stmt.setString(13, "TO");
                stmt.setString(14, "V");
                stmt.execute();
            }

            // Transferência de entrada (Crédito)
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, transferencia.getDestinoNr());
                stmt.setString(2, transferencia.getDestinoClass());
                stmt.setString(3, transferencia.getDestinoNr());
                stmt.setString(4, transferencia.getDestinoClass());
                stmt.setString(5, formato_ISO.format(new SimpleDateFormat("dd/MM/yyyy").parse(transferencia.getData())));
                stmt.setString(6, formato_ISO.format(new SimpleDateFormat("dd/MM/yyyy").parse(transferencia.getData())));
                stmt.setString(7, formato_ISO.format(new SimpleDateFormat("dd/MM/yyyy").parse(transferencia.getData())));
                stmt.setString(8, transferencia.getDocumento());
                stmt.setString(9, "9.001.002");
                stmt.setString(10, "TRANSFERIDO DE: " + transferencia.getOrigemNr());
                stmt.setDouble(11, transferencia.getValor());
                stmt.setString(12, formato_ISO.format(new SimpleDateFormat("dd/MM/yyyy").parse(transferencia.getData())));
                stmt.setString(13, "TD");
                stmt.setString(14, "V");
                stmt.execute();
            }

            return true;
        } catch (SQLException | ParseException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao registrar transferência: " + ex.getMessage());
            return false;
        } finally {
            conexao.fecharConexao();
        }
    }
}
