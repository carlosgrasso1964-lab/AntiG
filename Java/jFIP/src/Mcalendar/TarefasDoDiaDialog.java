package Mcalendar;

import dao.TarefaDAO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import model.Tarefa;

/**
 * Dialog de tarefas de um dia (Calendario To-Do).
 * Espelha o painel lateral "Tarefas do Dia" do calendario.php:
 * adicionar, concluir/reabrir (alternar) e excluir.
 * Usa a MESMA tabela tb_tarefas do MySQL jfin (sincronizado com o web).
 *
 * @author CARLOS
 */
public class TarefasDoDiaDialog extends JDialog {

    private final String dataStr;            // YYYY-MM-DD
    private final Runnable aoFechar;         // callback p/ recarregar badges
    private final TarefaDAO tarefaDAO = new TarefaDAO();

    private final DefaultListModel<String> modelo = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modelo);
    private final JTextField campoDescricao = new JTextField(22);
    private final JButton btnAdicionar = new JButton("Adicionar");
    private final JButton btnAlternar = new JButton("Concluir/Reabrir");
    private final JButton btnExcluir = new JButton("Excluir");

    public TarefasDoDiaDialog(JFrame dono, String dataStr, Runnable aoFechar) {
        super(dono, "Tarefas do Dia", true);
        this.dataStr = dataStr;
        this.aoFechar = aoFechar;

        setLayout(new BorderLayout(8, 8));
        setSize(420, 340);
        setLocationRelativeTo(dono);

        String[] p = dataStr.split("-");
        JLabel titulo = new JLabel("Tarefas de " + p[2] + "/" + p[1] + "/" + p[0]);
        titulo.setFont(new Font("Tahoma", Font.BOLD, 14));
        add(titulo, BorderLayout.NORTH);

        // Lista central
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setFont(new Font("Tahoma", Font.PLAIN, 13));
        add(new JScrollPane(lista), BorderLayout.CENTER);

        // Rodape: campo + botoes
        JPanel rodape = new JPanel(new BorderLayout(6, 6));
        JPanel linhaAdd = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        campoDescricao.addActionListener(e -> adicionarTarefa());
        btnAdicionar.addActionListener(e -> adicionarTarefa());
        linhaAdd.add(campoDescricao);
        linhaAdd.add(btnAdicionar);
        rodape.add(linhaAdd, BorderLayout.NORTH);

        JPanel linhaBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnAlternar.addActionListener(e -> alternarTarefa());
        btnExcluir.addActionListener(e -> excluirTarefa());
        linhaBotoes.add(btnAlternar);
        linhaBotoes.add(btnExcluir);
        rodape.add(linhaBotoes, BorderLayout.SOUTH);
        add(rodape, BorderLayout.SOUTH);

        // Duplo clique = alternar (como o botao de check do web)
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !lista.isSelectionEmpty()) {
                    alternarTarefa();
                }
            }
        });

        recarregarLista();
    }

    private void adicionarTarefa() {
        String descricao = campoDescricao.getText().trim();
        if (descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe a descricao da tarefa!");
            return;
        }
        if (tarefaDAO.adicionar(dataStr, descricao)) {
            campoDescricao.setText("");
            recarregarLista();
            if (aoFechar != null) aoFechar.run();  // Refresh badges após adicionar
        }
    }

    private void alternarTarefa() {
        int idx = lista.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma tarefa na lista!");
            return;
        }
        Tarefa t = tarefas.get(idx);
        if (tarefaDAO.alternar(t.getId())) {
            recarregarLista();
            if (aoFechar != null) aoFechar.run();
        }
    }

    private void excluirTarefa() {
        int idx = lista.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma tarefa na lista!");
            return;
        }
        Tarefa t = tarefas.get(idx);
        int resp = JOptionPane.showConfirmDialog(this,
                "Excluir tarefa?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION && tarefaDAO.excluir(t.getId())) {
            recarregarLista();
            if (aoFechar != null) aoFechar.run();
        }
    }

    private List<Tarefa> tarefas;

    private void recarregarLista() {
        tarefas = tarefaDAO.listarDia(dataStr);
        modelo.clear();
        for (Tarefa t : tarefas) {
            String prefixo = t.isConcluida() ? "[X] " : "[ ] ";
            modelo.addElement(prefixo + t.getDescricao());
        }
        lista.setSelectedIndex(modelo.size() - 1);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (aoFechar != null) {
            aoFechar.run();
        }
    }
}
