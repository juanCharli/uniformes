/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Juan Carlos
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public PanelBase() {
        setupUI();
        setupComboBoxListeners();
        loadCategories();
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
        categoryComboBox.addActionListener(e -> updateTypeComboBox());
        typeComboBox.addActionListener(e -> updateProductComboBox());
    }

    protected void loadCategories() {
        List<String> categories = new ArrayList<>();
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT nombre FROM Categoria";
            try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
                while (rs.next()) {
                    categories.add(rs.getString("nombre"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        categoryComboBox.setModel(new DefaultComboBoxModel<>(categories.toArray(new String[0])));
    }

    protected void updateTypeComboBox() {
        String category = (String) categoryComboBox.getSelectedItem();
        if (category == null) {
            return;
        }
        List<String> types = new ArrayList<>();
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT nombre FROM Tipos WHERE id_categoria = (SELECT id_categoria FROM Categoria WHERE nombre = ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, category);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        types.add(rs.getString("nombre"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        typeComboBox.setModel(new DefaultComboBoxModel<>(types.toArray(new String[0])));
    }

    protected void updateProductComboBox() {
        String category = (String) categoryComboBox.getSelectedItem();
        String type = (String) typeComboBox.getSelectedItem();
        if (category == null || type == null) {
            return;
        }
        List<String> products = new ArrayList<>();
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT talla FROM Productos WHERE id_tipo = (SELECT id_tipo FROM Tipos WHERE nombre = ? AND id_categoria = (SELECT id_categoria FROM Categoria WHERE nombre = ?))ORDER BY CAST(talla AS UNSIGNED) ASC, LENGTH(talla) ASC, talla ASC";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, type);
                stmt.setString(2, category);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        products.add(rs.getString("talla"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        productComboBox.setModel(new DefaultComboBoxModel<>(products.toArray(new String[0])));
    }

    protected int getProductID(String category, String type, String product) {
        int productId = 0;
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT id_producto FROM Productos WHERE talla = ? AND id_tipo = (SELECT id_tipo FROM Tipos WHERE nombre = ? AND id_categoria = (SELECT id_categoria FROM Categoria WHERE nombre = ?))";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, product);
                stmt.setString(2, type);
                stmt.setString(3, category);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        productId = rs.getInt("id_producto");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productId;
    }

    protected abstract void registerAll(); // Implementación específica en cada panel

    protected int getUserID() {
        // Implementación según tu login (ej: almacenar ID en sesión)
        return 1; // Valor temporal
    }
}
