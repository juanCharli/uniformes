/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Juan Carlos
 */
import dao.*;
import conexion.Conexion;
import modelo.Usuarios;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import java.sql.SQLException;

public class Panel_Inventario extends PanelBase {

    private JButton historyButton;
    private DefaultTableModel inventoryTableModel;
    private ModeloInventario mi;
    private ModeloProducto mp;
    private Usuarios user;

    public Panel_Inventario() throws SQLException {
        super();
        this.mi = new ModeloInventario();
        this.mp = new ModeloProducto();
        this.user = new Usuarios();
        setupInventoryUI();
    }

    private void setupInventoryUI() {
        // Configuración específica del panel de inventario
        inventoryTableModel = new DefaultTableModel(
                new Object[]{"Categoría", "Producto", "Talla", "Cantidad"}, 0);
        inventoryTable = new JTable(inventoryTableModel);
        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        ventasboton = new JButton("Historial de ventas");
        registerButton = new JButton("Registrar Ingresos");
        registerButton.setBackground(Color.GREEN);
        cancelButton = new JButton("Cancelar Ingreso");
        cancelButton.setBackground(Color.RED);
        historyButton = new JButton("Historial de Ingresos");
        addButton.setText("Añadir al registro");
        bottomPanel.add(registerButton);
        bottomPanel.add(cancelButton);
        bottomPanel.add(historyButton);
        bottomPanel.add(ventasboton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Acciones específicas
        addButton.addActionListener(e -> addToRegister());
        registerButton.addActionListener(e -> registerAll());
        cancelButton.addActionListener(e -> cancelSelectedRow());
        historyButton.addActionListener(e -> showIncomeHistory());
        ventasboton.addActionListener(e -> PanelHistorial());
    }

    private void addToRegister() {
        String category = (String) categoryComboBox.getSelectedItem();
        String type = (String) typeComboBox.getSelectedItem();
        String product = (String) productComboBox.getSelectedItem();
        String quantityStr = quantityField.getText();

        // Validar campos
        if (category == null || type == null || product == null || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Añadir registro a la tabla de inventario
        Object[] newRow = {category, type, product, quantity};
        inventoryTableModel.addRow(newRow);

        // Limpiar campos
        quantityField.setText("");

    }

    protected void registerAll() {
        // Lógica de registro de inventario
        int rows = inventoryTableModel.getRowCount();
        if (rows == 0) {
            JOptionPane.showMessageDialog(this, "No hay registros para ingresar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int i = 0; i < rows; i++) {
            String categoria = (String) inventoryTableModel.getValueAt(i, 0);
            String tipo = (String) inventoryTableModel.getValueAt(i, 1);
            String producto = (String) inventoryTableModel.getValueAt(i, 2);
            int cantidad = (int) inventoryTableModel.getValueAt(i, 3);

            int productId = mp.getProductID(categoria, tipo, producto);
            if (productId != 0) {
                boolean success = mi.registrarIngreso(productId, user.getIdUsuario(), cantidad);
                if (!success) {
                    JOptionPane.showMessageDialog(this, "Error al registrar ingreso.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        inventoryTableModel.setRowCount(0);
        JOptionPane.showMessageDialog(this, "Ingresos registrados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelSelectedRow() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow != -1) {
            inventoryTableModel.removeRow(selectedRow);
        }
    }

    // Métodos específicos de inventario (ej: historial)
    private void showIncomeHistory() {
        JFrame historyFrame = new JFrame("HISTORIAL DE INGRESOS---STOCK ACTUAL");
        historyFrame.setSize(800, 600);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        historyFrame.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridLayout(2, 1));

        // Panel del historial de ingresos
        JPanel incomeHistoryPanel = new JPanel(new BorderLayout());
        DefaultTableModel historyTableModel = new DefaultTableModel(new Object[]{"Producto", "Descripción", "Cantidad", "Fecha y Hora"}, 0);
        JTable historyTable = new JTable(historyTableModel);
        incomeHistoryPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        contentPanel.add(incomeHistoryPanel);

        // Panel del stock actual
        JPanel stockPanel = new JPanel(new BorderLayout());
        DefaultTableModel stockTableModel = new DefaultTableModel(new Object[]{"ID Producto", "Nombre", "Descripción", "Cantidad Total", "Precio del Producto"}, 0);
        JTable stockTable = new JTable(stockTableModel);
        stockPanel.add(new JScrollPane(stockTable), BorderLayout.CENTER);
        contentPanel.add(stockPanel);

        historyFrame.add(contentPanel, BorderLayout.CENTER);

        // Botón para exportar a PDF
        JPanel buttonPanel = new JPanel();
        JButton exportButton = new JButton("Exportar a PDF");
        buttonPanel.add(exportButton);
        historyFrame.add(buttonPanel, BorderLayout.SOUTH);

        // Acción del botón de exportación
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    exportToPDF(historyTableModel, stockTableModel);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(historyFrame, "Error al exportar a PDF.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Cargar el historial de ingresos
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT p.nombre, i.cantidad, i.fecha_hora, p.descripcion "
                    + "FROM Ingresos i "
                    + "JOIN Productos p ON i.id_producto = p.id_producto "
                    + "WHERE MONTH(i.fecha_hora) = MONTH(CURDATE()) AND YEAR(i.fecha_hora) = YEAR(CURDATE())";
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    String productName = resultSet.getString("nombre");
                    int quantity = resultSet.getInt("cantidad");
                    Timestamp timestamp = resultSet.getTimestamp("fecha_hora");
                    String description = resultSet.getString("descripcion");

                    Object[] row = {productName, description, quantity, timestamp.toString()};
                    historyTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el historial de ingresos.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Cargar el stock actual
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT p.id_producto, p.nombre, p.descripcion, inv.cantidad, p.precio "
                    + "FROM Inventario inv "
                    + "JOIN Productos p ON inv.id_producto = p.id_producto";
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    int productId = resultSet.getInt("id_producto");
                    String productName = resultSet.getString("nombre");
                    String description = resultSet.getString("descripcion");
                    int totalQuantity = resultSet.getInt("cantidad");
                    double price = resultSet.getDouble("precio");

                    Object[] row = {productId, productName, description, totalQuantity, price};
                    stockTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el stock actual.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        historyFrame.setVisible(true);
    }

    private void exportToPDF(DefaultTableModel historyTableModel, DefaultTableModel stockTableModel) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar PDF");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            PdfWriter writer = new PdfWriter(fileToSave.getAbsolutePath() + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Historial de Ingresos del Mes Actual").setBold().setFontSize(18));
            addTableToDocument(historyTableModel, document);

            document.add(new Paragraph("\n\nStock Actual").setBold().setFontSize(18));
            addTableToDocument(stockTableModel, document);

            document.close();
        }
    }

    private void addTableToDocument(DefaultTableModel tableModel, Document document) {
        int numCols = tableModel.getColumnCount();
        Table table = new Table(UnitValue.createPercentArray(numCols)).useAllAvailableWidth();

        // Agregar encabezados de columna
        for (int col = 0; col < numCols; col++) {
            table.addHeaderCell(new Paragraph(tableModel.getColumnName(col)).setBold());
        }

        // Agregar filas de datos
        int numRows = tableModel.getRowCount();
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                table.addCell(new Paragraph(tableModel.getValueAt(row, col).toString()));
            }
        }

        document.add(table);
    }

    private void PanelHistorial() {
        JFrame historyFrame = new JFrame("Historial de Ventas");
        historyFrame.setSize(800, 600);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        historyFrame.setLayout(new BorderLayout());

        // Panel de filtros
        JPanel panelFiltros = new JPanel();
        panelFiltros.setLayout(new GridLayout(2, 2));
        Properties p = new Properties();
        p.put("text.today", "Hoy");
        p.put("text.month", "Mes");
        p.put("text.year", "Año");

        // Crear el modelo de fecha
        UtilDateModel modelInicio = new UtilDateModel();
        UtilDateModel modelFin = new UtilDateModel();
        JDatePanelImpl datePanelInicio = new JDatePanelImpl(modelInicio, new Properties());
        JDatePanelImpl datePanelFin = new JDatePanelImpl(modelFin, new Properties());
        JDatePickerImpl datePickerInicio = new JDatePickerImpl(datePanelInicio, new DateLabelFormatter());
        JDatePickerImpl datePickerFin = new JDatePickerImpl(datePanelFin, new DateLabelFormatter());

        panelFiltros.add(new JLabel("Fecha Inicio:"));
        panelFiltros.add(datePickerInicio);
        panelFiltros.add(new JLabel("Fecha Fin:"));
        panelFiltros.add(datePickerFin);

        historyFrame.add(panelFiltros, BorderLayout.NORTH);

        // Panel del historial de ingresos
        JPanel incomeHistoryPanel = new JPanel(new BorderLayout());
        DefaultTableModel historyTableModel = new DefaultTableModel(new Object[]{"Producto", "Talla", "Cantidad", "Fecha y Hora", "Precio Total", "ID Orden"}, 0);
        JTable historyTable = new JTable(historyTableModel);
        incomeHistoryPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        // Panel del stock actual
        JPanel stockPanel = new JPanel(new BorderLayout());
        DefaultTableModel stockTableModel = new DefaultTableModel(new Object[]{"ID Producto", "Nombre", "Descripción", "Cantidad Total", "Precio del Producto"}, 0);
        JTable stockTable = new JTable(stockTableModel);
        stockPanel.add(new JScrollPane(stockTable), BorderLayout.CENTER);

        // Split pane to divide the income history panel and stock panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, incomeHistoryPanel, stockPanel);
        splitPane.setDividerLocation(300); // Set initial divider location
        historyFrame.add(splitPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        JButton exportButton = new JButton("Exportar a PDF");
        JButton filterButton = new JButton("Buscar");
        JButton clearFiltersButton = new JButton("Limpiar Filtros");

        JLabel precios = new JLabel("<hrml><b>El total vendido es: 0.0</hmtl></b>");
        buttonPanel.add(filterButton);
        buttonPanel.add(clearFiltersButton);
        buttonPanel.add(exportButton);

        buttonPanel.add(precios); // Añadir el JLabel al panel de botones
        historyFrame.add(buttonPanel, BorderLayout.SOUTH);

        // Acción para cargar las ventas del día al iniciar
        loadIncomeHistory(historyTableModel, new java.sql.Date(System.currentTimeMillis()), new java.sql.Date(System.currentTimeMillis()));
        updateTotalPriceLabel(historyTableModel, precios); // Actualizar el JLabel con la suma inicial

        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener las fechas seleccionadas
                java.util.Date startValue = (java.util.Date) datePickerInicio.getModel().getValue();
                java.util.Date endValue = (java.util.Date) datePickerFin.getModel().getValue();

                java.sql.Date sqlStartDate = null;
                java.sql.Date sqlEndDate = null;

                if (startValue != null) {
                    sqlStartDate = new java.sql.Date(startValue.getTime());
                }

                if (endValue != null) {
                    sqlEndDate = new java.sql.Date(endValue.getTime());
                }

                // Imprimir para depuración
                // Cargar el historial de ingresos
                loadIncomeHistory(historyTableModel, sqlStartDate, sqlEndDate);
                updateTotalPriceLabel(historyTableModel, precios); // Actualizar el JLabel después de filtrar
            }
        });

        clearFiltersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                datePickerInicio.getModel().setValue(null);
                datePickerFin.getModel().setValue(null);
                loadIncomeHistory(historyTableModel, new java.sql.Date(System.currentTimeMillis()), new java.sql.Date(System.currentTimeMillis()));
                updateTotalPriceLabel(historyTableModel, precios); // Actualizar el JLabel después de limpiar filtros
                total = 0;
            }
        });

        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    exportToPDF(historyTableModel);
                } catch (IOException ex) {
                    Logger.getLogger(Panel_Ventas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        // Cargar el stock actual
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT p.id_producto, p.nombre, p.descripcion, inv.cantidad, p.precio "
                    + "FROM Inventario inv "
                    + "JOIN Productos p ON inv.id_producto = p.id_producto";
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    int productId = resultSet.getInt("id_producto");
                    String productName = resultSet.getString("nombre");
                    String description = resultSet.getString("descripcion");
                    int totalQuantity = resultSet.getInt("cantidad");
                    double price = resultSet.getDouble("precio");

                    Object[] row = {productId, productName, description, totalQuantity, price};
                    stockTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el stock actual.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        historyFrame.setVisible(true);
    }

    private void exportToPDF(DefaultTableModel historyTableModel) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar PDF");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            PdfWriter writer = new PdfWriter(fileToSave.getAbsolutePath() + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Historial de ventas del Mes Actual").setBold().setFontSize(18));
            addTableToDocument(historyTableModel, document);

            document.close();
        }
    }

    private void loadIncomeHistory(DefaultTableModel historyTableModel, Date startDate, Date endDate) {
        historyTableModel.setRowCount(0); // Limpiar la tabla
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT p.nombre AS producto, V.cantidad, V.fecha_hora, V.precio_total, V.id_orden, p.talla "
                    + "FROM Ventas V "
                    + "JOIN Productos p ON V.id_producto = p.id_producto "
                    + "WHERE V.fecha_hora BETWEEN ? AND ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Convertir java.util.Date a java.sql.Date
                java.sql.Date sqlStartDate = null;
                java.sql.Date sqlEndDate = null;

                if (startDate != null) {
                    sqlStartDate = new java.sql.Date(startDate.getTime());
                }
                if (endDate != null) {
                    sqlEndDate = new java.sql.Date(endDate.getTime());
                }

                statement.setDate(1, sqlStartDate);
                statement.setDate(2, sqlEndDate);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String productName = resultSet.getString("producto");
                        String talla = resultSet.getString("talla");
                        int quantity = resultSet.getInt("cantidad");
                        Date date = resultSet.getDate("fecha_hora");
                        double totalPrice = resultSet.getDouble("precio_total");
                        int orderId = resultSet.getInt("id_orden");

                        Object[] row = {productName, talla, quantity, date.toString(), totalPrice, orderId};
                        historyTableModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar el historial de ventas.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTotalPriceLabel(DefaultTableModel tableModel, JLabel totalLabel) {
        total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object value = tableModel.getValueAt(i, 4); // Asegúrate de que el índice de la columna "Precio Total" es correcto
            if (value instanceof Double) {
                total += (Double) value;
            } else if (value instanceof String) {
                try {
                    total += Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    // Manejar el caso en que la cadena no se puede convertir a un número
                    e.printStackTrace();
                }
            }
        }
        totalLabel.setText("<html><b> El total vendido es: " + total + "</html></b>");
    }

}
