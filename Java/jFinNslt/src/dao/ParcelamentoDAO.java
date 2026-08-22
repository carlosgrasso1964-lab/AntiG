package dao;

import model.Parcelamento;
import utilitarios.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ParcelamentoDAO {

    //private Connection conn;
    Conexao conexao = new Conexao();

    public void salvar(Parcelamento p) throws SQLException {
        String sql = "INSERT INTO tbmovimento(recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento,"
                + " classif, Descr, valor, dtApr, statusMov, Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        conexao.abrirConexao();
        try (Connection con = conexao.getConexao(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, p.getRecurso());
            stmt.setString(2, p.getVrecurso());
            stmt.setString(3, p.getClifor());
            stmt.setString(4, p.getVCliFor());
            stmt.setString(5, p.getDtlancto());
            stmt.setString(6, p.getDtEmi());
            stmt.setString(7, p.getDtVcto());
            stmt.setString(8, p.getDocumento());
            stmt.setString(9, p.getClassif());
            stmt.setString(10, p.getDescricao());
            stmt.setBigDecimal(11, p.getValor());
            stmt.setString(12, null);
            stmt.setString(13, p.getStatusMov());
            stmt.setString(14, p.getPrev());
            stmt.executeUpdate();
        }
    }
}
