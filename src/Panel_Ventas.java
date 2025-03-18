/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mauri
 */
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class Panel_Ventas extends PanelBase {

    private JButton historyButton;
    private DefaultTableModel inventoryTableModel;
    private JLabel totalPriceLabel;
    private double total = 0.0;

    public Panel_Ventas() {
        super();
        setupSalesUI();
    }

    private void setupSalesUI() {
        // Configuración específica del panel de ventas
        inventoryTableModel = new DefaultTableModel(
                new Object[]{"Categoría", "Producto", "Talla", "Cantidad", "Precio unitario", "Precio total"}, 0);
        inventoryTable = new JTable(inventoryTableModel);
        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        registerButton = new JButton("Realizar Venta");
        cancelButton = new JButton("Cancelar Venta");
        historyButton = new JButton("Historial de Ventas");
        totalPriceLabel = new JLabel("<html><b>Precio total: $0.00</b></html>");
        addButton.setText("Añadir al carrito");
        bottomPanel.add(registerButton);
        bottomPanel.add(cancelButton);
        bottomPanel.add(historyButton);
        bottomPanel.add(totalPriceLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        // Acciones específicas
        addButton.addActionListener(e -> addToRegister());
        registerButton.addActionListener(e -> registerAll());
        cancelButton.addActionListener(e -> cancelSelectedRow());
        historyButton.addActionListener(e -> showIncomeHistory());
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
        int productId = getProductID(category, type, product);
        String id = String.valueOf(productId);
        double unitPrice = getUnitPrice(id);
        double totalPriceForRow = Double.parseDouble(quantityStr) * unitPrice;

        // Añadir registro a la tabla de inventario
        Object[] newRow = {category, type, product, quantity, unitPrice, totalPriceForRow};
        inventoryTableModel.addRow(newRow);

        // Actualizar el precio total
        updateTotalPrice();

        // Limpiar campos
        quantityField.setText("");
        categoryComboBox.setSelectedIndex(0);
        typeComboBox.setSelectedIndex(0);
        productComboBox.setSelectedIndex(0);
    }

    private void updateTotalPrice() {
        double totalPrice = 0.0;
        int rowCount = inventoryTableModel.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            double rowPrice = (double) inventoryTableModel.getValueAt(i, 5); // Índice de la columna de precio total
            totalPrice += rowPrice;
        }
        totalPriceLabel.setText("<html><b>Precio total: $" + String.format("%.2f", totalPrice) + "</b></html>");
    }
    
    @Override
    protected void registerAll() {
        int rows = inventoryTableModel.getRowCount();
        if (rows == 0) {
            JOptionPane.showMessageDialog(this, "No hay registros para ingresar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = Conexion.getConnection()) {
            connection.setAutoCommit(false); // Para manejo de transacciones

            boolean hayInventarioSuficiente = true;
            String sqlVentas = "INSERT INTO Ventas (id_producto, id_usuario, cantidad, fecha_hora, id_orden, precio_total) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statementVentas = connection.prepareStatement(sqlVentas)) {

                // Paso 1: Insertar una nueva orden en la tabla Ordenes
                String sqlOrden = "INSERT INTO Ordenes (id_usuario, fecha_hora) VALUES (?, ?)";
                int idOrden;
                try (PreparedStatement statementOrden = connection.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                    statementOrden.setInt(1, getUserID()); // Implementar getUserID para obtener el ID del usuario actual
                    statementOrden.setTimestamp(2, new Timestamp(new java.util.Date().getTime()));
                    statementOrden.executeUpdate();

                    try (ResultSet generatedKeys = statementOrden.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            idOrden = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Error al obtener el ID de la nueva orden.");
                        }
                    }
                }

                // Variable para almacenar el tipo que falta
                String tipoFaltante = "";

                // Listas para almacenar los datos del ticket
                List<String> products = new ArrayList<>();
                List<Integer> quantities = new ArrayList<>();
                List<Double> productTotalPrices = new ArrayList<>();
                double globalTotalPrice = 0.0;

                // Paso 2: Procesar cada registro en inventoryTableModel
                for (int i = 0; i < rows; i++) {
                    String category = (String) inventoryTableModel.getValueAt(i, 0);
                    String type = (String) inventoryTableModel.getValueAt(i, 1);
                    String product = (String) inventoryTableModel.getValueAt(i, 2);
                    int quantity = (int) inventoryTableModel.getValueAt(i, 3);
                    Double price = (Double) inventoryTableModel.getValueAt(i, 4);
                    Double totalPriceForRow = (Double) inventoryTableModel.getValueAt(i, 5);
                    int productID = getProductID(category, type, product); // Implementar getProductID para obtener el ID del producto
                    System.out.println(productID);
                    System.out.println(quantity);
                    // Verificar si hay suficiente inventario
                    if (!checkStock(productID, quantity)) {
                        hayInventarioSuficiente = false;
                        tipoFaltante = type; // Almacenar el tipo que falta
                        break;
                    }

                    System.out.println(quantity);
                    // Registrar la venta
                    statementVentas.setInt(1, productID);
                    statementVentas.setInt(2, getUserID()); // Implementar getUserID para obtener el ID del usuario actual
                    statementVentas.setInt(3, quantity);
                    statementVentas.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
                    statementVentas.setInt(5, idOrden); // Asociar con la orden
                    statementVentas.setDouble(6, totalPriceForRow);
                    statementVentas.addBatch();

                    // Agregar a las listas del ticket
                    products.add(type);
                    quantities.add(quantity);
                    productTotalPrices.add(totalPriceForRow);
                    globalTotalPrice += totalPriceForRow;
                }

                // Ejecutar todas las ventas en batch
                statementVentas.executeBatch();

                if (hayInventarioSuficiente) {
                    connection.commit(); // Confirmar transacción

                    // Limpiar la tabla después de registrar los ingresos
                    inventoryTableModel.setRowCount(0);
                    updateTotalPrice();

                    // Imprimir el ticket
                    TicketPrinter.printTicket(products, quantities, productTotalPrices, globalTotalPrice);

                    JOptionPane.showMessageDialog(this, "Venta realizada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    connection.rollback(); // Revertir transacción si no hay inventario suficiente
                    JOptionPane.showMessageDialog(this, "No hay suficiente inventario disponible para registrar. Falta el tipo: " + tipoFaltante, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al registrar los ingresos en la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean checkStock(int productID, int quantity) {
        boolean hayStock = false;
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT cantidad FROM Inventario WHERE id_producto = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, productID);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int stock = resultSet.getInt("cantidad");
                        hayStock = stock >= quantity;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hayStock;
    }


    @Override
    protected int getUserID() {
        // Implementa la lógica para obtener el ID del usuario actual
        // Podrías almacenar el ID del usuario en una variable de sesión al iniciar sesión
        // Aquí se asume un valor fijo para simplificación
        return 2;
    }

    private double getUnitPrice(String id) {
        double price = -1;
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT precio FROM Productos WHERE id_producto = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        price = resultSet.getDouble("precio");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return price;
    }

    private void cancelSelectedRow() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow != -1) {
            inventoryTableModel.removeRow(selectedRow);
            // Actualizar el precio total después de eliminar la fila
            updateTotalPrice();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para cancelar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showIncomeHistory() {
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
        JButton Ventas = new JButton("imprimir ventas de hoy");
        JLabel precios = new JLabel("<hrml><b>El total vendido es: 0.0</hmtl></b>");
        buttonPanel.add(filterButton);
        buttonPanel.add(clearFiltersButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(Ventas);
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

        Ventas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TicketPrinter obj = new TicketPrinter();
                obj.imprimirhoy(total);
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

    private void exportToPDF(DefaultTableModel historyTableModel) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar PDF");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            PdfWriter writer = new PdfWriter(fileToSave.getAbsolutePath() + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Historial de ventas").setBold().setFontSize(18));

            addTableToDocument(historyTableModel, document);

            // Calcular el monto total
            double total = 0.0;
            for (int i = 0; i < historyTableModel.getRowCount(); i++) {
                Object value = historyTableModel.getValueAt(i, 4); // Suponiendo que la columna "Precio Total" está en la cuarta posición (índice 4)
                if (value instanceof Double) {
                    total += (Double) value;
                } else if (value instanceof String) {
                    try {
                        total += Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Añadir el monto total al documento
            document.add(new Paragraph("El Monto total es: " + total).setBold().setFontSize(14));

            document.close();
        }
    }

    private void addTableToDocument(DefaultTableModel tableModel, Document document) {
        Table table = new Table(tableModel.getColumnCount());
        // Añadir encabezados de columnas
        for (int col = 0; col < tableModel.getColumnCount(); col++) {
            table.addCell(new Cell().add(new Paragraph(tableModel.getColumnName(col)).setBold()));
        }
        // Añadir filas de la tabla
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                table.addCell(new Cell().add(new Paragraph(tableModel.getValueAt(row, col).toString())));
            }
        }
        document.add(table);
    }
}
