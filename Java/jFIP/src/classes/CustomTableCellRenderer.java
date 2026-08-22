package classes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//este código é usado para formatar as jTable, fornecendo os parâmetros de qual coluna
// - qual coluna deseja alterar na jTable;
// - Qual o tamanho da Fonte à ser utilizada;
// - Se a coluna é do tipo Data = True (jTablePesquisa.getColumnModel().getColumn(0).setCellRenderer(new CustomTableCellRenderer(0, 14, true));
// - Se for de outro tipo = False (jTablePesquisa.getColumnModel().getColumn(0).setCellRenderer(new CustomTableCellRenderer(0, 14, false)); ).
// // Para colunas de valores numéricos
//jTablePesquisa.getColumnModel().getColumn(0).setCellRenderer(new CustomTableCellRenderer(0, 14, false));
// Para colunas de datas
//jTablePesquisa.getColumnModel().getColumn(0).setCellRenderer(new CustomTableCellRenderer(0, 14, true));

public class CustomTableCellRenderer extends DefaultTableCellRenderer {

    private int targetColumn;
    private Font customFont;
    private boolean isDateColumn;

    public CustomTableCellRenderer(int targetColumn, int fontSize, boolean isDateColumn) {
        this.targetColumn = targetColumn;
        this.customFont = new Font("Tahoma", Font.PLAIN, fontSize);
        this.isDateColumn = isDateColumn;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component componente = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        componente.setFont(customFont);
        this.setHorizontalAlignment(RIGHT);

        if (column == targetColumn) {
            if (isDateColumn) {
                // Assumindo que as datas estão no formato "yyyy-MM-dd"
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = sdf.parse(value.toString());
                    this.setForeground(Color.BLACK);
                } catch (ParseException e) {
                    this.setForeground(Color.RED);  // Ou alguma outra lógica para valores inválidos
                }
            } else {
                try {
                    String cellValueStr = value.toString().replaceAll(",", "");
                    Double cellValue = Double.valueOf(cellValueStr);

                    if (cellValue > 0) {
                        this.setForeground(Color.BLACK);
                    } else if (cellValue < 0) {
                        this.setForeground(Color.RED);
                    } else {
                        this.setForeground(Color.BLACK);
                        componente.setForeground(Color.BLACK);
                        this.setText("");  // Define a célula como vazia para valores 0 ou 0,00
                    }
                } catch (NumberFormatException e) {
                    this.setForeground(Color.BLACK);  // Mantém a cor padrão para strings
                }
            }
        } else {
            this.setForeground(Color.BLACK);
        }

        return componente;
    }
}
