package utilitarios;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class PercentualRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value != null && !value.toString().trim().isEmpty()) {
            // Supondo que o valor é um número, converta-o para um Double
            try {
                Double val = Double.parseDouble(value.toString());
                setText(val + "%");  // Adiciona o símbolo de porcentagem

                // Verifica se o valor é negativo e altera a cor para vermelho se for
                if (val < 0) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);  // Define a cor padrão (preta) para valores não negativos
                }
            } catch (NumberFormatException e) {
                setText(value.toString() + "%");  // Caso não consiga converter para número, ainda adiciona o símbolo de porcentagem
                c.setForeground(Color.BLACK);  // Define a cor padrão (preta) caso de erro de formatação
            }
        } else {
            setText("");
            c.setForeground(Color.BLACK);  // Define a cor padrão (preta) para células vazias
        }

        setHorizontalAlignment(SwingConstants.RIGHT);
        return c;
    }
}

/*package classes;

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
}*/