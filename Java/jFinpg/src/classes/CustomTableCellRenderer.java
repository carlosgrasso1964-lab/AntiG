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

    private final int columnIndex;
    private final int fontSize;
    private final boolean isDate;

    public CustomTableCellRenderer(int columnIndex, int fontSize, boolean isDate) {
        this.columnIndex = columnIndex;
        this.fontSize = fontSize;
        this.isDate = isDate;
        setHorizontalAlignment(SwingConstants.RIGHT);
        setFont(new Font("Tahoma", Font.PLAIN, fontSize));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {

        // === PROTEÇÃO CONTRA NULL ===
        if (value == null) {
            value = "";
        }

        // Chama o renderer padrão
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // === APLICA APENAS NA COLUNA ESPECÍFICA ===
        if (column == columnIndex) {

            // --- FORMATAÇÃO DE DATA ---
            if (isDate) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    if (value instanceof String && !((String) value).isEmpty()) {
                        Date date = inputFormat.parse((String) value);
                        setText(outputFormat.format(date));
                    }
                    setForeground(Color.BLACK);
                } catch (ParseException e) {
                    setForeground(Color.RED);
                    setText("DATA INVÁLIDA");
                }

                // --- FORMATAÇÃO DE VALORES NUMÉRICOS ---
            } else {
                String text = value.toString().trim();
                if (text.isEmpty() || text.equals("0") || text.equals("0,00") || text.equals("0.00")) {
                    setText("");
                    setForeground(Color.BLACK);
                } else {
                    try {
                        // Remove pontos e troca vírgula por ponto
                        String clean = text.replace(".", "").replace(",", ".");
                        double num = Double.parseDouble(clean);

                        if (num > 0) {
                            setForeground(Color.BLACK);
                        } else if (num < 0) {
                            setForeground(Color.RED);
                        } else {
                            setText("");
                            setForeground(Color.BLACK);
                        }
                    } catch (NumberFormatException e) {
                        setForeground(Color.BLACK); // texto não numérico
                    }
                }
            }

            // Aplica fonte
            setFont(new Font("Tahoma", Font.PLAIN, fontSize));
        } else {
            setForeground(Color.BLACK);
        }

        return this;
    }

}
