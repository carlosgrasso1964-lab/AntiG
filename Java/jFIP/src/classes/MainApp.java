package classes;

import utilitarios.Conexao;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import view.Tela_de_Acesso;
import java.sql.SQLException;

public class MainApp {
    // Perguntar ao usuário se deseja usar Nuvem ou Local
    public static void main(String[] args) {
        // Exibir a caixa de diálogo para escolher o tipo de conexão
        Object[] options = {"Local", "Nuvem"};
        int escolha = JOptionPane.showOptionDialog(
                null,
                "Escolha o Local do Banco de Dados:",
                "Opção de Local",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        // Configurar o tipo de conexão com base na escolha do usuário
        if (escolha == 0) {
            Conexao.tipoDeConexao = "L"; // Local
        } else if (escolha == 1) {
            Conexao.tipoDeConexao = "N"; // Nuvem
        } else {
            JOptionPane.showMessageDialog(null, "Nenhuma opção selecionada! O sistema será encerrado.");
            System.exit(0);
        }

        // Inicializar a Tela de Acesso
        SwingUtilities.invokeLater(() -> {
            try {
                new Tela_de_Acesso().setVisible(true);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erro ao inicializar a Tela de Acesso! " + e.getMessage());
            }
        });
    }
}
