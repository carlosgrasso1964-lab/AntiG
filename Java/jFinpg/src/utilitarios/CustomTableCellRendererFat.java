
package utilitarios;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Usuário
 */
public class CustomTableCellRendererFat extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        if (value instanceof String && value.toString().matches("\\d+(,\\d{1,2})?")) {
            // Aplica a formatação para números
            setHorizontalAlignment(SwingConstants.RIGHT);
        } else {
            setHorizontalAlignment(SwingConstants.LEFT);
        }
        super.setValue(value);
    }
}