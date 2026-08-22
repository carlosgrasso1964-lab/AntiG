package utilitarios;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author CARLOS
 */
public class LimpaTela {

    public void LimpaTela(JPanel container) {
        Component conponents[] = container.getComponents();
        for (Component component : conponents) {
            if (component instanceof JTextField jTextField) {
                jTextField.setText(null);
            }
        }
    }
}
