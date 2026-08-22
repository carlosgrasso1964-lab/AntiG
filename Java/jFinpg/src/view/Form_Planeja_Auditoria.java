package view;

import utilitarios.Conexao;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Form_Planeja_Auditoria extends JFrame {

    private JTextField txtConta;
    private JSpinner spnAno;
    private JComboBox<Integer> cmbMes;
    private JTextField txtValor;
    private JTextField txtInflacao;
    private JTable tabela;
    private DefaultTableModel modelo;
    private JButton btnAdicionar, btnAtualizar, btnExcluir, btnFechar;
    private Connection conexao;
    private String dataIni;
    private String dataFim;

    public Form_Planeja_Auditoria() {
        this("", "");
    }

    public Form_Planeja_Auditoria(String dataIni, String dataFim) {
        this.dataIni = dataIni;
        this.dataFim = dataFim;

        try {
            // CORREÇÃO: Atribua diretamente à variável da classe
            this.conexao = Conexao.faz_conexao();

            if (this.conexao == null) {
                throw new SQLException("A conexão retornou nula da classe Utilitários.");
            }

            setTitle("Gerenciar Planejamento - Plano Diretor");
            setSize(900, 500);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            initComponents();
            carregarPlanejamento(); // Agora o this.conexao estará preenchido!
            setLocationRelativeTo(null);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar com o banco: " + e.getMessage());
        }
    }

    private void initComponents() {
        txtConta = new JTextField(25);

        spnAno = new JSpinner(new SpinnerNumberModel(2026, 2020, 2030, 1));

        cmbMes = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            cmbMes.addItem(i);
        }

        txtValor = new JTextField(15);
        txtInflacao = new JTextField(10);
        txtInflacao.setText("5.0");

        btnAdicionar = new JButton("Adicionar");
        btnAtualizar = new JButton("Atualizar");
        btnExcluir = new JButton("Excluir");
        btnFechar = new JButton("Fechar");

        modelo = new DefaultTableModel(new Object[]{"Conta", "Ano", "Mês", "Valor Planejado", "Inflação"}, 0);
        tabela = new JTable(modelo);

        tabela.setSelectionBackground(new java.awt.Color(255, 255, 204));

        JScrollPane scrollPane = new JScrollPane(tabela);

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelCampos.add(new JLabel("Conta:"));
        panelCampos.add(txtConta);
        panelCampos.add(new JLabel("Ano:"));
        panelCampos.add(spnAno);
        panelCampos.add(new JLabel("Mês:"));
        panelCampos.add(cmbMes);
        panelCampos.add(new JLabel("Valor:"));
        panelCampos.add(txtValor);
        panelCampos.add(new JLabel("Inflação %:"));
        panelCampos.add(txtInflacao);

        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotoes.add(btnAdicionar);
        panelBotoes.add(btnAtualizar);
        panelBotoes.add(btnExcluir);
        panelBotoes.add(btnFechar);

        add(panelCampos, "North");
        add(scrollPane, "Center");
        add(panelBotoes, "South");

        btnAdicionar.addActionListener(e -> adicionarPlanejamento());
        btnAtualizar.addActionListener(e -> atualizarPlanejamento());
        btnExcluir.addActionListener(e -> excluirPlanejamento());
        btnFechar.addActionListener(e -> dispose());

        tabela.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tabela.getSelectedRow();
                if (row >= 0) {
                    txtConta.setText(modelo.getValueAt(row, 0).toString());
                    spnAno.setValue(modelo.getValueAt(row, 1));
                    cmbMes.setSelectedItem(modelo.getValueAt(row, 2));
                    txtValor.setText(modelo.getValueAt(row, 3).toString());
                    txtInflacao.setText(modelo.getValueAt(row, 4).toString());
                }
            }
        });
    }

    private int getMesFromDate(String data) {
        if (data == null || data.length() < 5) {
            return 1;
        }
        return Integer.parseInt(data.substring(3, 5));
    }

    private int getAnoFromDate(String data) {
        if (data == null || data.length() < 10) {
            return 2026;
        }
        return Integer.parseInt(data.substring(6, 10));
    }

    private void carregarPlanejamento() {
        modelo.setRowCount(0);

        String sql;
        PreparedStatement stmt;

        if (dataIni != null && !dataIni.isEmpty() && dataFim != null && !dataFim.isEmpty()) {
            int mesIni = getMesFromDate(dataIni);
            int mesFim = getMesFromDate(dataFim);
            int anoIni = getAnoFromDate(dataIni);
            int anoFim = getAnoFromDate(dataFim);

            sql = "SELECT conta, ano_referencia, mes, valor_planejado, inflacao_premissa "
                    + "FROM tb_plano_diretor "
                    + "WHERE ((ano_referencia = ? AND mes >= ?) OR (ano_referencia > ?)) "
                    + "AND ((ano_referencia = ? AND mes <= ?) OR (ano_referencia < ?)) "
                    + "ORDER BY ano_referencia DESC, mes DESC, conta";

            try {
                stmt = conexao.prepareStatement(sql);
                stmt.setInt(1, anoIni);
                stmt.setInt(2, mesIni);
                stmt.setInt(3, anoFim);
                stmt.setInt(4, anoFim);
                stmt.setInt(5, mesFim);
                stmt.setInt(6, anoIni);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar: " + e.getMessage());
                return;
            }
        } else {
            sql = "SELECT conta, ano_referencia, mes, valor_planejado, inflacao_premissa "
                    + "FROM tb_plano_diretor ORDER BY ano_referencia DESC, mes DESC, conta";

            try {
                stmt = conexao.prepareStatement(sql);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar: " + e.getMessage());
                return;
            }
        }

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("conta"),
                    rs.getInt("ano_referencia"),
                    rs.getInt("mes"),
                    rs.getDouble("valor_planejado"),
                    rs.getDouble("inflacao_premissa")
                });
            }
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar: " + e.getMessage());
        }
    }

    private void adicionarPlanejamento() {
        String conta = txtConta.getText().trim();
        int ano = (int) spnAno.getValue();
        int mes = (int) cmbMes.getSelectedItem();

        if (conta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome da Conta!");
            return;
        }

        double valor;
        double inflacao;
        try {
            valor = Double.parseDouble(txtValor.getText().replace(".", "").replace(",", "."));
            inflacao = Double.parseDouble(txtInflacao.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor ou Inflação inválidos!");
            return;
        }

        String sql = "INSERT INTO tb_plano_diretor (ano_referencia, mes, conta, valor_planejado, inflacao_premissa) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, ano);
            stmt.setInt(2, mes);
            stmt.setString(3, conta);
            stmt.setDouble(4, valor);
            stmt.setDouble(5, inflacao);
            stmt.execute();
            JOptionPane.showMessageDialog(this, "Registro adicionado com sucesso!");
            carregarPlanejamento();
            limparCampos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar: " + e.getMessage());
        }
    }

    private void atualizarPlanejamento() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um registro na tabela!");
            return;
        }

        String contaAntiga = modelo.getValueAt(row, 0).toString();
        int anoAntigo = (int) modelo.getValueAt(row, 1);
        int mesAntigo = (int) modelo.getValueAt(row, 2);

        String contaNova = txtConta.getText().trim();
        int anoNovo = (int) spnAno.getValue();
        int mesNovo = (int) cmbMes.getSelectedItem();

        if (contaNova.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome da Conta!");
            return;
        }

        double valor;
        double inflacao;
        try {
            valor = Double.parseDouble(txtValor.getText().replace(".", "").replace(",", "."));
            inflacao = Double.parseDouble(txtInflacao.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor ou Inflação inválidos!");
            return;
        }

        String sql = "UPDATE tb_plano_diretor SET conta=?, ano_referencia=?, mes=?, valor_planejado=?, inflacao_premissa=? "
                + "WHERE conta=? AND ano_referencia=? AND mes=?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, contaNova);
            stmt.setInt(2, anoNovo);
            stmt.setInt(3, mesNovo);
            stmt.setDouble(4, valor);
            stmt.setDouble(5, inflacao);
            stmt.setString(6, contaAntiga);
            stmt.setInt(7, anoAntigo);
            stmt.setInt(8, mesAntigo);
            stmt.execute();
            JOptionPane.showMessageDialog(this, "Registro atualizado com sucesso!");
            carregarPlanejamento();
            limparCampos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + e.getMessage());
        }
    }

    private void excluirPlanejamento() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um registro na tabela!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir o registro?", "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String conta = modelo.getValueAt(row, 0).toString();
            int ano = (int) modelo.getValueAt(row, 1);
            int mes = (int) modelo.getValueAt(row, 2);

            String sql = "DELETE FROM tb_plano_diretor WHERE conta=? AND ano_referencia=? AND mes=?";
            try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
                stmt.setString(1, conta);
                stmt.setInt(2, ano);
                stmt.setInt(3, mes);
                stmt.execute();
                JOptionPane.showMessageDialog(this, "Registro excluído com sucesso!");
                carregarPlanejamento();
                limparCampos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage());
            }
        }
    }

    private void limparCampos() {
        txtConta.setText("");
        txtValor.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Form_Planeja_Auditoria().setVisible(true));
    }
}
