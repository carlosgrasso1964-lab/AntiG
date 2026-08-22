
package classes;

import utilitarios.Conexao;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class ImageDBHandler {
    
    public static void saveImageToDatabase(File file) {
        try {
            Connection con;
            con = Conexao.faz_conexao();
            String sql = "UPDATE tbconfig SET id=?, ITela=? WHERE id=1";
            //String sql = "INSERT INTO tbconfig (id, ITela) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, 1); // ID da configuração
            ps.setBlob(2, new FileInputStream(file));
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static void retrieveImageFromDatabase(String destinationPath) throws IOException {
        try {
            Connection con1;
            con1 = Conexao.faz_conexao();
            String sql1 = "SELECT ITela FROM tbconfig WHERE id = ?";
            try (PreparedStatement ps1 = con1.prepareStatement(sql1)) {
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
            con1.close();
        } catch (SQLException | IOException e) {
        }
    }
}

