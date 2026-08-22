package classes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class PercentualRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value != null && !value.toString().trim().isEmpty()) {
            setText(value.toString() + "%");  // Adiciona o símbolo de porcentagem
        } else {
            setText("");
        }
        setHorizontalAlignment(SwingConstants.RIGHT);
        return c;
    }
}