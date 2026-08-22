package utilitarios;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class ImageDBHandler {

    public static void saveImageToDatabase(File file) {
        Conexao conexao = new Conexao();
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection con = conexao.getConexao(); // Obtém a conexão
            String sql = "UPDATE tbconfig SET id=?, ITela=? WHERE id=1";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, 1); // ID da configuração

            // Ler o arquivo como array de bytes
            byte[] fileContent = Files.readAllBytes(file.toPath());
            ps.setBytes(2, fileContent);

            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }

    public static void retrieveImageFromDatabase(String destinationPath) throws IOException {
        Conexao conexao = new Conexao();
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection con = conexao.getConexao(); // Obtém a conexão
            String sql1 = "SELECT ITela FROM tbconfig WHERE id = ?";
            try (PreparedStatement ps1 = con.prepareStatement(sql1)) {
                ps1.setInt(1, 1); // ID da configuração
                try (ResultSet rs1 = ps1.executeQuery()) {
                    if (rs1.next()) {
                        byte[] imageData = rs1.getBytes("ITela");
                        try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
                            fos.write(imageData);
                        }
                    }
                }
            } // ID da configuração // ID da configuração
            con.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }
}
