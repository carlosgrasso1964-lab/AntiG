package relatorios;

import utilitarios.Conexao;
import com.itextpdf.text.BaseColor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.swing.JFileChooser;

public class RelCliFor {

    public RelCliFor() throws FileNotFoundException {
        try {
            // Cria um JFileChooser para selecionar o local de salvamento
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
                //String file_name = "outprt\\RelFavorecidos.pdf";

                Document document = new Document(PageSize.A4);
                document.setMargins(10f, 10f, 10f, 10f);
                PdfWriter.getInstance(document, new FileOutputStream(file_name));
                document.open();

                Image Imagem = Image.getInstance("src\\Imagens\\Home.png");
                Imagem.scaleAbsolute(45f, 50f);
                Imagem.setAlignment(Element.ALIGN_LEFT);
                document.add(Imagem);

                Paragraph tituloDoRelatorio = new Paragraph(new Phrase(20F, "RELATÓRIO DE FAVORECIDOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18F)));
                tituloDoRelatorio.setAlignment(Element.ALIGN_CENTER);
                document.add(tituloDoRelatorio);
                
                PdfPTable tabela = new PdfPTable(7);
                tabela.setWidthPercentage(100f);
                tabela.setWidths(new float[]{7f, 6f, 35f, 20f, 5f, 13f, 14f});
                
                PdfPCell codCliFor = new PdfPCell(new Phrase("Conta"));
                codCliFor.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell Tipo = new PdfPCell(new Phrase("Tipo"));
                Tipo.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell nomeCliFor = new PdfPCell(new Phrase("Razão Social"));
                nomeCliFor.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell apelidoCliFor = new PdfPCell(new Phrase("Nome Fantasia"));
                apelidoCliFor.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell contatoCliFor = new PdfPCell(new Phrase("Contato"));
                contatoCliFor.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell obs = new PdfPCell(new Phrase("Observações"));
                obs.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell fkCliForGp = new PdfPCell(new Phrase("Ví­nculo"));
                fkCliForGp.setBackgroundColor(BaseColor.YELLOW);
                
                tabela.addCell(codCliFor);
                tabela.addCell(Tipo);
                tabela.addCell(nomeCliFor);
                tabela.addCell(apelidoCliFor);
                tabela.addCell(contatoCliFor);
                tabela.addCell(obs);
                tabela.addCell(fkCliForGp);
                
                document.add(new Paragraph(" "));
                
                //String arquivo = "tbclifor";
                PreparedStatement ps = null;
                ResultSet rs = null;
                Connection con;
                con = Conexao.faz_conexao();
                //String query = "SELECT * FROM " + arquivo;
                String query = "Select * FROM tbclifor ORDER BY \"nomeCliFor\"";
                ps = con.prepareStatement(query);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    tabela.addCell(rs.getString("codCliFor"));
                    tabela.addCell(rs.getString("Tipo"));
                    tabela.addCell(rs.getString("nomeCliFor"));
                    tabela.addCell(rs.getString("apelidoCliFor"));
                    tabela.addCell(rs.getString("contatoCliFor"));
                    tabela.addCell(rs.getString("obs"));
                    tabela.addCell(rs.getString("fkCliForGp"));
                }
                
                document.add(tabela);
                document.close();
                
            }
            }catch (Exception e) {
            System.err.println(e);
        }
    }
    

    public static void main(String[] args) throws FileNotFoundException {
        RelCliFor cf = new RelCliFor();
    }
}
