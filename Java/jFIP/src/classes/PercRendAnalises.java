package classes;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class PercRendAnalises extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value != null && !value.toString().trim().isEmpty()) {
            try {
                Double val = Double.parseDouble(value.toString().replace("%", ""));
                setText(String.format("%.4f%%", val)); // Adiciona o símbolo de porcentagem

                // Verifica se o valor é negativo e altera a cor para vermelho se for
                if (val < 0) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK); // Define a cor padrão (preta) para valores não negativos
                }
            } catch (NumberFormatException e) {
                setText(value.toString() + "%"); // Caso não consiga converter para número, ainda adiciona o símbolo de porcentagem
                c.setForeground(Color.BLACK); // Define a cor padrão (preta) caso de erro de formatação
            }
        } else {
            setText(""); // Mantém a célula vazia se o valor for nulo ou vazio
            c.setForeground(Color.BLACK); // Define a cor padrão (preta) para células vazias
        }
        setHorizontalAlignment(SwingConstants.RIGHT);
        return c;
    }
}