/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Juan Carlos
 */
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.UnitValue;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;

public class ExportarPDF {

    public static void exportarTabla(DefaultTableModel... tablas) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar PDF");
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            PdfWriter writer = new PdfWriter(fileToSave.getAbsolutePath() + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            for (int i = 0; i < tablas.length; i++) {
                if (i > 0) {
                    document.add(new Paragraph("\n\n"));
                }
                document.add(new Paragraph("Tabla " + (i + 1)).setBold().setFontSize(18));
                agregarTablaADocumento(tablas[i], document);
            }

            document.close();
            JOptionPane.showMessageDialog(null, "PDF guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void agregarTablaADocumento(DefaultTableModel tableModel, Document document) {
        int numCols = tableModel.getColumnCount();
        Table table = new Table(UnitValue.createPercentArray(numCols)).useAllAvailableWidth();

        // Agregar encabezados de columna
        for (int col = 0; col < numCols; col++) {
            table.addHeaderCell(new Cell().add(new Paragraph(tableModel.getColumnName(col)).setBold()));
        }

        // Agregar filas de datos
        int numRows = tableModel.getRowCount();
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                table.addCell(new Cell().add(new Paragraph(tableModel.getValueAt(row, col).toString())));
            }
        }

        document.add(table);
    }
}
