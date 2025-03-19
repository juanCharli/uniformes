/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Juan Carlos
 */
import dao.ModeloHistorial;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.util.List;
import java.util.Properties;
import org.jdatepicker.impl.*;

public abstract class PanelBaseHistorial extends JFrame {

    protected DefaultTableModel historyTableModel, stockTableModel;
    protected JTable historyTable, stockTable;
    protected JLabel totalLabel;
    protected UtilDateModel modelInicio, modelFin;
    protected JDatePickerImpl datePickerInicio, datePickerFin;
    protected JButton filterButton, clearFiltersButton, exportButton;
    protected ModeloHistorial modeloHistorial;

    public PanelBaseHistorial(String title, String[] columnNames) {
        setTitle(title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        modeloHistorial = new ModeloHistorial(); // Modelo para historial y stock

        // Panel de filtros
        JPanel panelFiltros = new JPanel(new GridLayout(2, 2));
        Properties p = new Properties();
        p.put("text.today", "Hoy");
        p.put("text.month", "Mes");
        p.put("text.year", "Año");

        modelInicio = new UtilDateModel();
        modelFin = new UtilDateModel();
        JDatePanelImpl datePanelInicio = new JDatePanelImpl(modelInicio, p);
        JDatePanelImpl datePanelFin = new JDatePanelImpl(modelFin, p);
        datePickerInicio = new JDatePickerImpl(datePanelInicio, new DateLabelFormatter());
        datePickerFin = new JDatePickerImpl(datePanelFin, new DateLabelFormatter());

        panelFiltros.add(new JLabel("Fecha Inicio:"));
        panelFiltros.add(datePickerInicio);
        panelFiltros.add(new JLabel("Fecha Fin:"));
        panelFiltros.add(datePickerFin);

        add(panelFiltros, BorderLayout.NORTH);

        // Panel del historial
        historyTableModel = new DefaultTableModel(columnNames, 0);
        historyTable = new JTable(historyTableModel);
        JScrollPane historyScroll = new JScrollPane(historyTable);

        // Panel del stock
        stockTableModel = new DefaultTableModel(new Object[]{"ID Producto", "Nombre", "Descripción", "Cantidad Total", "Precio"}, 0);
        stockTable = new JTable(stockTableModel);
        JScrollPane stockScroll = new JScrollPane(stockTable);

        // Divisor para historial y stock
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, historyScroll, stockScroll);
        splitPane.setDividerLocation(300); // Ajuste inicial

        add(splitPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        filterButton = new JButton("Buscar");
        clearFiltersButton = new JButton("Limpiar Filtros");
        exportButton = new JButton("Exportar a PDF");
        totalLabel = new JLabel("<html><b>Total: 0.0</b></html>");

        buttonPanel.add(filterButton);
        buttonPanel.add(clearFiltersButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(totalLabel);

        add(buttonPanel, BorderLayout.SOUTH);

        // Eventos de botones
        filterButton.addActionListener(e -> filtrarHistorial());
        clearFiltersButton.addActionListener(e -> limpiarFiltros());
        exportButton.addActionListener(e -> exportarAPDF());

        // Cargar historial y stock inicial
        cargarHistorial(new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
        cargarStock();
    }

    protected void filtrarHistorial() {
        Date startDate = (modelInicio.getValue() != null) ? new Date(modelInicio.getValue().getTime()) : null;
        Date endDate = (modelFin.getValue() != null) ? new Date(modelFin.getValue().getTime()) : null;
        cargarHistorial(startDate, endDate);
    }

    protected void limpiarFiltros() {
        modelInicio.setValue(null);
        modelFin.setValue(null);
        cargarHistorial(new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
    }

    protected void actualizarTotal() {
        double total = 0;
        
        // Verificar si la tabla tiene al menos 5 columnas (para evitar errores en el historial de ingresos)
        if (historyTableModel.getColumnCount() < 5) {
            totalLabel.setText("<html><b>Total: N/A</b></html>");
            return;
        }
        for (int i = 0; i < historyTableModel.getRowCount(); i++) {
            Object value = historyTableModel.getValueAt(i, 4);
            if (value instanceof Double) {
                total += (Double) value;
            } else if (value instanceof String) {
                try {
                    total += Double.parseDouble((String) value);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        totalLabel.setText("<html><b>Total vendido es : " + total + "</b></html>");
    }

    protected void cargarStock() {
        stockTableModel.setRowCount(0);
        List<Object[]> stockData = modeloHistorial.obtenerStockActual();
        for (Object[] row : stockData) {
            stockTableModel.addRow(row);
        }
    }

    protected abstract void cargarHistorial(Date startDate, Date endDate);

    protected abstract void exportarAPDF();
}
