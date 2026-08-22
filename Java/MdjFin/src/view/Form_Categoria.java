package view;

import dao.CategoriaDAO;
import model.Categoria;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class Form_Categoria extends JFrame {

    private JTextField txtNome;
    private JTextField txtMultiplicador;
    private JTable tabela;
    private DefaultTableModel modelo;
    private JButton btnAdicionar, btnAtualizar, btnExcluir;
    private CategoriaDAO dao;

    public Form_Categoria() {
        dao = new CategoriaDAO();
        setTitle("Gerenciar Categorias");
        setSize(800, 500);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initComponents();
        carregarCategorias();
        setLocationRelativeTo(null);  // Centraliza a janela
    }

    private void initComponents() {
        txtNome = new JTextField(20);
        txtMultiplicador = new JTextField(10);
        txtMultiplicador.setText("1.0");
        btnAdicionar = new JButton("Adicionar");
        btnAtualizar = new JButton("Atualizar");
        btnExcluir = new JButton("Excluir");

        modelo = new DefaultTableModel(new String[]{"ID", "Nome", "Multiplicador"}, 0);
        tabela = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tabela);

        // Definindo o layout do painel para centralizar os botões e campos
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());  // Usando FlowLayout para os botões e campos
        panel.add(new JLabel("Nome:"));
        panel.add(txtNome);
        panel.add(new JLabel("Multiplicador:"));
        panel.add(txtMultiplicador);
        panel.add(btnAdicionar);
        panel.add(btnAtualizar);
        panel.add(btnExcluir);

        add(panel, "North");
        add(scrollPane, "Center");

        btnAdicionar.addActionListener(e -> adicionarCategoria());
        btnAtualizar.addActionListener(e -> atualizarCategoria());
        btnExcluir.addActionListener(e -> excluirCategoria());

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tabela.getSelectedRow();
                if (row >= 0) {
                    txtNome.setText(modelo.getValueAt(row, 1).toString());
                    txtMultiplicador.setText(modelo.getValueAt(row, 2).toString());
                }
            }
        });
    }

    private void carregarCategorias() {
        try {
            modelo.setRowCount(0); // Limpa a tabela antes de adicionar novos dados
            List<Categoria> lista = dao.listar();
            for (Categoria c : lista) {
                modelo.addRow(new Object[]{c.getId(), c.getNome(), c.getMultiplicador()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar categorias: " + e.getMessage());
        }
    }

    private void adicionarCategoria() {
        try {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Digite o nome da categoria!", "Atenção", JOptionPane.WARNING_MESSAGE);
                txtNome.requestFocus();
                return;
            }

            double multiplicador;
            try {
                multiplicador = Double.parseDouble(txtMultiplicador.getText().replace(",", "."));
                if (multiplicador < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Multiplicador inválido! Use apenas números.", "Erro", JOptionPane.ERROR_MESSAGE);
                txtMultiplicador.requestFocus();
                txtMultiplicador.selectAll();
                return;
            }

            Categoria categoria = new Categoria();
            categoria.setNome(nome);
            categoria.setMultiplicador(multiplicador);
            // ID não precisa setar ? o banco gera automático

            dao.salvar(categoria);

            JOptionPane.showMessageDialog(this, "Categoria adicionada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            txtNome.setText("");
            txtMultiplicador.setText("1.0");
            txtNome.requestFocus();
            carregarCategorias();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar categoria:\n" + e.getMessage(), "Erro no Banco", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void atualizarCategoria() {
        int row = tabela.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para atualizar!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = (Integer) modelo.getValueAt(row, 0);
            String nome = txtNome.getText().trim();
            double multiplicador = Double.parseDouble(txtMultiplicador.getText().replace(",", "."));

            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome não pode ficar em branco!");
                return;
            }

            Categoria c = new Categoria();
            c.setId(id);
            c.setNome(nome);
            c.setMultiplicador(multiplicador);

            dao.atualizar(c);
            JOptionPane.showMessageDialog(this, "Categoria atualizada com sucesso!");
            carregarCategorias();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + e.getMessage());
        }
    }

    private void excluirCategoria() {
        int row = tabela.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para excluir!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir esta categoria?\nIsso pode afetar produtos vinculados!",
                "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = (Integer) modelo.getValueAt(row, 0);
                dao.excluir(id);
                JOptionPane.showMessageDialog(this, "Categoria excluída com sucesso!");
                carregarCategorias();
                txtNome.setText("");
                txtMultiplicador.setText("");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir:\n" + e.getMessage());
            }
        }
    }
    
    
}
