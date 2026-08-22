package classes;

import utilitarios.Conexao;
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

        String sql = "UPDATE tbconfig SET \"iTela\"=? WHERE id=1";
        // Use try-with-resources para fechar TUDO automaticamente
        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            byte[] imageBytes = Files.readAllBytes(file.toPath());
            ps.setBytes(1, imageBytes);
            ps.executeUpdate();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void retrieveImageFromDatabase(String destinationPath) throws IOException {
        try {
            Connection con1;
            con1 = Conexao.faz_conexao();
            String sql1 = "SELECT \"iTela\" FROM tbconfig WHERE id = ?";
            try (PreparedStatement ps1 = con1.prepareStatement(sql1)) {
                ps1.setInt(1, 1); // ID da configuração
                try (ResultSet rs1 = ps1.executeQuery()) {
                    if (rs1.next()) {
                        byte[] imageData = rs1.getBytes("iTela");
                        try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
                            fos.write(imageData);
                        }
                    }
                }
            } // ID da configuração // ID da configuração
            con1.close();
        } catch (SQLException | IOException e) {
        }
    }
}
