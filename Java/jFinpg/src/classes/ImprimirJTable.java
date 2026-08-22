package classes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JOptionPane;
import javax.swing.JTable;

/**
 * Classe que imprime uma JTable
 *
 * @author	Guilherme I. Keller TODO - Uma melhor explicacao & comentarios.
 */
public class ImprimirJTable implements Printable {

    private JTable tabela;
    private PrinterJob printerJob;
    private Graphics graphics;
    private Graphics2D graphics2d;

    /**
     * Metodo construtor da classe
     *
     * @param	tabela Uma JTable a ser impressa
     */
    public ImprimirJTable(JTable tabela){

        //MessageFormat cabecalho = new MessageFormat("Relatório Previsão de Caixa - ");
        //MessageFormat rodape = new MessageFormat("Página {0,number,integer}");
        //this.tabela.print(JTable.PrintMode.NORMAL,cabecalho, rodape);
        
        this.tabela = tabela;
        printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(this);
        printerJob.setJobName("ImprFlx");
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(null, "Ocorreu um erro durante a impress�o. \n" + ex.toString(), "Erro do Sistema", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Método responsável pela impressão da JTable
     * @param g
     * @throws java.awt.print.PrinterException
     */
    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException{
        graphics2d = (Graphics2D) g;
        graphics2d.setColor(Color.black);
        
        int fontHeight = graphics2d.getFontMetrics().getHeight();
        //int fontDesent = graphics2d.getFontMetrics().getDescent();
        
        // Deixe espaço para o n�mero da página
        double pageHeight = pageFormat.getImageableHeight() - fontHeight;
        double pageWidth = pageFormat.getImageableWidth();
        double tableWidth = (double) tabela.getColumnModel().getTotalColumnWidth();
        double scale = 1;
        if (tableWidth >= pageWidth) {
            scale = pageWidth / tableWidth;
        }
        double headerHeightOnPage = tabela.getTableHeader().getHeight() * scale;
        double tableWidthOnPage = tableWidth * scale;
        double oneRowHeight = (tabela.getRowHeight() + tabela.getRowMargin()) * scale;
        int numRowsOnAPage = (int) ((pageHeight - headerHeightOnPage) / oneRowHeight);
        double pageHeightForTable = oneRowHeight * numRowsOnAPage;
        int totalNumPages = (int) Math.ceil(((double) tabela.getRowCount()) / numRowsOnAPage);
        if (pageIndex >= totalNumPages) {
            return NO_SUCH_PAGE; //Não há mais páginas
        }
        graphics2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        graphics2d.translate(0f, headerHeightOnPage);
        graphics2d.translate(0f, -pageIndex * pageHeightForTable);

        // Se este pedaço da tabela for menor que o tamanho disponível,
        // recorte para os limites apropriados.
        if (pageIndex + 1 == totalNumPages) {
            int lastRowPrinted = numRowsOnAPage * pageIndex;
            int numRowsLeft = tabela.getRowCount() - lastRowPrinted;
            graphics2d.setClip(0,
                    (int) (pageHeightForTable * pageIndex),
                    (int) Math.ceil(tableWidthOnPage),
                    (int) Math.ceil(oneRowHeight * numRowsLeft)
            );
        } // caso contrário, recorte para toda a área disponível.
        else {
            graphics2d.setClip(0,
                    (int) (pageHeightForTable * pageIndex),
                    (int) Math.ceil(tableWidthOnPage),
                    (int) Math.ceil(pageHeightForTable)
            );
        }
        graphics2d.scale(scale, scale);
        //String s = "RELATÓRIO DE PREVISÃO DE CAIXA";
        tabela.paint(graphics2d);
        graphics2d.scale(1/scale, 1/scale);
        graphics2d.translate(0f, pageIndex * pageHeightForTable);
        graphics2d.translate(0f, -headerHeightOnPage);
        graphics2d.setClip(0, 0,(int) Math.ceil(tableWidthOnPage),(int) Math.ceil(headerHeightOnPage));
        graphics2d.scale(scale, scale);
        //tabela.getTableHeader().add(s, tabela);
        //tabela.getTableHeader().paint(graphics2d);	// pintar cabeçalho no topo
        return Printable.PAGE_EXISTS;                   // continua imprimindo
    }
}
