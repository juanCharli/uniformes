/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Juan Carlos
 */
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import javax.swing.JOptionPane;

public class Panel_HistorialVentas extends PanelBaseHistorial {
    private ExportarPDF exportar;
    
    public Panel_HistorialVentas() {
        super("Historial de Ventas", new String[]{"Producto", "Talla", "Cantidad", "Fecha y Hora", "Precio Total", "ID Orden"});
        exportar = new ExportarPDF();
    }

    @Override
    protected void cargarHistorial(Date startDate, Date endDate) {
        historyTableModel.setRowCount(0);
        List<Object[]> historial = modeloHistorial.obtenerHistorialVentas(startDate, endDate);
        for (Object[] row : historial) {
            historyTableModel.addRow(row);
        }
        actualizarTotal();
    }

    @Override
    protected void exportarAPDF() {
        try {
            exportar.exportarTabla(historyTableModel, stockTableModel);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al exportar a PDF.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
