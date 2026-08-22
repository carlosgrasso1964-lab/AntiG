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
import java.io.FileOutputStream;
import javax.swing.JFileChooser;

public class RelRecursos {

    public RelRecursos() {
        try {
            //String file_name = "outprt\\RelRecursos.pdf";
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
                Paragraph tituloDoRelatorio = new Paragraph(new Phrase(20F, "RELATÓRIO DE RECURSOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18F)));
                tituloDoRelatorio.setAlignment(Element.ALIGN_CENTER);
                document.add(tituloDoRelatorio);
                document.add(new Paragraph(" "));
                PdfPTable tabela = new PdfPTable(9);
                tabela.setWidthPercentage(100f);
                tabela.setWidths(new float[]{6f, 23f, 9f, 6f, 12f, 13f, 14f, 7f, 10f});
                PdfPCell codigo = new PdfPCell(new Phrase("Cód"));
                codigo.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell nomebco = new PdfPCell(new Phrase("Nome Banco"));
                nomebco.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell agencia = new PdfPCell(new Phrase("Agência"));
                agencia.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell fluxo = new PdfPCell(new Phrase("Fluxo"));
                fluxo.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell limite = new PdfPCell(new Phrase("Limite"));
                limite.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell abertura = new PdfPCell(new Phrase("Abertura"));
                abertura.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell encerramento = new PdfPCell(new Phrase("Encerramento"));
                encerramento.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell status = new PdfPCell(new Phrase("Status"));
                status.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell fk_gpprinc = new PdfPCell(new Phrase("Vinculo"));
                fk_gpprinc.setBackgroundColor(BaseColor.YELLOW);
                tabela.addCell(codigo);
                tabela.addCell(nomebco);
                tabela.addCell(agencia);
                tabela.addCell(fluxo);
                tabela.addCell(limite);
                tabela.addCell(abertura);
                tabela.addCell(encerramento);
                tabela.addCell(status);
                tabela.addCell(fk_gpprinc);
                String arquivo = "tbrecursos";
                PreparedStatement ps = null;
                ResultSet rs = null;
                Connection con;
                con = Conexao.faz_conexao();
                String query = "SELECT * FROM " + arquivo;
                ps = con.prepareStatement(query);
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabela.addCell(rs.getString("codigo"));
                    tabela.addCell(rs.getString("nomebco"));
                    tabela.addCell(rs.getString("agencia"));
                    tabela.addCell(rs.getString("fluxo"));
                    tabela.addCell(rs.getString("limite"));
                    tabela.addCell(rs.getString("abertura"));
                    tabela.addCell(rs.getString("encerramento"));
                    tabela.addCell(rs.getString("status"));
                    tabela.addCell(rs.getString("fk_gpprinc"));
                }
                document.add(tabela);
                document.close();
            }

        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) {
        RelRecursos recur = new RelRecursos();
    }
}
