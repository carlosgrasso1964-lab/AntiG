package utilitarios;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class InteiroRenderer extends DefaultTableCellRenderer {

    private int fontSize;

    public InteiroRenderer(int fontSize) {
        this.fontSize = fontSize;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component componente = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        componente.setFont(new Font("Tahoma", Font.PLAIN, fontSize));
        this.setHorizontalAlignment(RIGHT);
        this.setForeground(Color.BLACK);

        if (value != null) {
            String valor = value.toString().trim();
            if (valor.equals("0") || valor.equals("0.0") || valor.equals("0,0")) {
                this.setText(""); // Mostra vazio se for zero
            } else {
                this.setText(valor); // Mostra o número como está
            }
        }

        return componente;
    }
}