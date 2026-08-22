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
import javax.swing.*;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RelPlano {

    public RelPlano() {
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

                Document document = new Document(PageSize.A4);
                document.setMargins(10f, 10f, 10f, 10f);
                PdfWriter.getInstance(document, new FileOutputStream(file_name));
                document.open();

                Image Imagem = Image.getInstance("src\\Imagens\\Home.png");
                Imagem.scaleAbsolute(45f, 50f);
                Imagem.setAlignment(Element.ALIGN_LEFT);
                document.add(Imagem);

                Paragraph tituloDoRelatorio = new Paragraph(new Phrase(20F, "RELATÓRIO DE PLANO DE CONTAS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18F)));
                tituloDoRelatorio.setAlignment(Element.ALIGN_CENTER);
                document.add(tituloDoRelatorio);

                PdfPTable tabela = new PdfPTable(4);
                tabela.setWidthPercentage(100f);
                tabela.setWidths(new float[]{11f, 14f, 35f, 40f});

                PdfPCell cod_Geral = new PdfPCell(new Phrase("Conta"));
                cod_Geral.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell nome_P = new PdfPCell(new Phrase("Grupo Principal"));
                nome_P.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell nome_S = new PdfPCell(new Phrase("Grupo Secundário"));
                nome_S.setBackgroundColor(BaseColor.YELLOW);
                PdfPCell nome_C = new PdfPCell(new Phrase("Conta"));
                nome_C.setBackgroundColor(BaseColor.YELLOW);

                tabela.addCell(cod_Geral);
                tabela.addCell(nome_P);
                tabela.addCell(nome_S);
                tabela.addCell(nome_C);

                document.add(new Paragraph(" "));

                //String arquivo = "gpprincipal";
                PreparedStatement ps = null;
                ResultSet rs = null;
                Connection con;
                con = Conexao.faz_conexao();
                //String query = "SELECT * FROM " + arquivo + "ORDER BY " + "cod_Geral";
                String query = "Select * FROM gpprincipal ORDER BY \"cod_Geral\"";
                ps = con.prepareStatement(query);
                rs = ps.executeQuery();

                while (rs.next()) {
                    tabela.addCell(rs.getString("cod_Geral"));
                    tabela.addCell(rs.getString("nome_P"));
                    tabela.addCell(rs.getString("nome_S"));
                    tabela.addCell(rs.getString("nome_C"));
                }

                document.add(tabela);
                document.close();
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) {
        RelPlano gen = new RelPlano();
    }
}