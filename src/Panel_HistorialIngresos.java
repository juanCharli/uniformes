
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import javax.swing.JOptionPane;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Juan Carlos
 */
public class Panel_HistorialIngresos extends PanelBaseHistorial {
    private ExportarPDF exportar;

    public Panel_HistorialIngresos() {
        super("Historial de Ingresos", new String[]{"Producto", "Descripción", "Cantidad", "Fecha y Hora"});
        exportar= new ExportarPDF();
    }

    @Override
    protected void cargarHistorial(Date startDate, Date endDate) {
        historyTableModel.setRowCount(0);
        List<Object[]> historial = modeloHistorial.obtenerHistorialIngresos(startDate, endDate);
        for (Object[] row : historial) {
            historyTableModel.addRow(row);
        }
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
