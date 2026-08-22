package relatorios;

import utilitarios.Conexao;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JFileChooser;

public class RelUsers {

    public RelUsers() {
        try {
            //String file_name = "outprt\\RelUsers.pdf";
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar Relatório");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String file_name = fileChooser.getSelectedFile().getAbsolutePath();

                // Adiciona a extensão .pdf se o usuário não o fizer
                if (!file_name.toLowerCase().endsWith(".pdf")) {
                    file_name += ".pdf";
                }

                Document document = new Document(PageSize.A4);
                document.setMargins(10f, 10f, 10f, 10f);
                PdfWriter.getInstance(document, new FileOutputStream(file_name));
                document.open();
                Image Imagem = Image.getInstance("src\\Imagens\\Home.png");
                Imagem.scaleAbsolute(45f, 50f);
                Imagem.setAlignment(Element.ALIGN_LEFT);
                document.add(Imagem);
                //document.add(Image.getInstance("Imagens/Home.png"));
                Paragraph tituloDoRelatorio = new Paragraph(new Phrase(20F, "RELATÓRIO DE USUÁRIOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18F)));
                tituloDoRelatorio.setAlignment(Element.ALIGN_CENTER);
                document.add(tituloDoRelatorio);
                PdfPTable tabela = new PdfPTable(3);
                tabela.setWidthPercentage(100f);
                tabela.setWidths(new float[]{5f, 35f, 60f});
                PdfPCell id = new PdfPCell(new Phrase("Id"));
                id.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell usuario = new PdfPCell(new Phrase("Usuário"));
                usuario.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell senha = new PdfPCell(new Phrase("Senha"));
                senha.setBackgroundColor(BaseColor.YELLOW);
                tabela.addCell(id);
                tabela.addCell(usuario);
                tabela.addCell(senha);
                document.add(new Paragraph(" "));
                String arquivo = "dados_senhas";
                PreparedStatement ps = null;
                ResultSet rs = null;
                Connection con;
                con = Conexao.faz_conexao();
                String query = "SELECT * FROM " + arquivo;
                ps = con.prepareStatement(query);
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabela.addCell(rs.getString("id"));
                    tabela.addCell(rs.getString("usuario"));
                    tabela.addCell(rs.getString("senha"));
                }
                document.add(tabela);
                document.close();
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) {
        RelUsers usu = new RelUsers();
    }
}
