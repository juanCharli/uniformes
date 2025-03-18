/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Juan Carlos
 */
import dao.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;


public abstract class PanelBase extends JPanel {

    protected JComboBox<String> categoryComboBox;
    protected JComboBox<String> typeComboBox;
    protected JComboBox<String> productComboBox;
    protected JTextField quantityField;
    protected JButton addButton;
    protected JTable inventoryTable;
    protected DefaultTableModel inventoryTableModel;
    protected JButton registerButton;
    protected JButton ventasboton;
    protected JButton cancelButton;
    protected JLabel categoria;
    protected JLabel tipo;
    protected JLabel producto;
    protected JLabel cantidad;
    protected double total;
    private ModeloInventario mi;
    private ModeloProducto mp;

    public PanelBase() throws SQLException {
        setupUI();
        setupComboBoxListeners();
        this.mi = new ModeloInventario();
        this.mp = new ModeloProducto();
        getCategorias();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        JPanel registerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Etiquetas comunes
        categoria = new JLabel("Categoría:");
        tipo = new JLabel("Tipo:");
        producto = new JLabel("Talla-Producto:");
        cantidad = new JLabel("Cantidad:");

        // Componentes comunes
        categoryComboBox = new JComboBox<>();
        typeComboBox = new JComboBox<>();
        productComboBox = new JComboBox<>();
        quantityField = new JTextField();
        addButton = new JButton();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        categoria = new JLabel("Categoría:");
        gbc.gridx = 1;
        gbc.gridy = 0;
        registerPanel.add(categoria, gbc);
        categoryComboBox = new JComboBox<>();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        categoryComboBox.setPreferredSize(new Dimension(190, 25));
        registerPanel.add(categoryComboBox, gbc);

        tipo = new JLabel("Tipo:");
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        registerPanel.add(tipo, gbc);
        typeComboBox = new JComboBox<>();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        typeComboBox.setPreferredSize(new Dimension(190, 25));
        registerPanel.add(typeComboBox, gbc);

        producto = new JLabel("Talla-Producto:");
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        registerPanel.add(producto, gbc);
        productComboBox = new JComboBox<>();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        productComboBox.setPreferredSize(new Dimension(190, 25));
        registerPanel.add(productComboBox, gbc);

        cantidad = new JLabel("Cantidad:");
        gbc.gridx = 7;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        registerPanel.add(cantidad, gbc);
        quantityField = new JTextField();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        quantityField.setPreferredSize(new Dimension(190, 25));
        registerPanel.add(quantityField, gbc);

        addButton = new JButton("Añadir al Registro");
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        registerPanel.add(addButton, gbc);

        add(registerPanel, BorderLayout.NORTH);
    }

    private void setupComboBoxListeners() {
        categoryComboBox.addActionListener(e -> getTiposPorCategoria());
        typeComboBox.addActionListener(e -> getProductosPorTipo());
    }

    protected void getCategorias() {
        categoryComboBox.setModel(new DefaultComboBoxModel<>(mp.getCategorias().toArray(new String[0])));
    }

    protected void getTiposPorCategoria() {
        String categoria = (String) categoryComboBox.getSelectedItem();
        if (categoria != null) {
            typeComboBox.setModel(new DefaultComboBoxModel<>(mp.getTiposPorCategoria(categoria).toArray(new String[0])));
        }
    }

    protected void getProductosPorTipo() {
        String categoria = (String) categoryComboBox.getSelectedItem();
        String tipo = (String) typeComboBox.getSelectedItem();
        productComboBox.setModel(new DefaultComboBoxModel<>(mp.getProductosPorTipo(categoria, tipo).toArray(new String[0])));
    }

}
