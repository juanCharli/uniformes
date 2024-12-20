/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mauri
 */
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class TicketPrinter {

    public static void printTicket(List<String> products, List<Integer> quantities, List<Double> productTotalPrices, double globalTotalPrice) {
        // Define the content of the ticket
            java.util.Date fechaHoy = new java.util.Date();       
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fechaHoyStr = sdf.format(fechaHoy);
        StringBuilder ticket = new StringBuilder();
        ticket.append("******* TICKET DE VENTA ********\n");
         ticket.append("Fecha: ").append(fechaHoyStr ).append("\n");
        
        for (int i = 0; i < products.size(); i++) {
            ticket.append("Producto: ").append(products.get(i)).append("\n");
            ticket.append("Cantidad: ").append(quantities.get(i)).append("\n");
            ticket.append("Precio Total del Producto: $").append(String.format("%.2f", productTotalPrices.get(i))).append("\n");
            ticket.append("--------------------------------\n");
        }

        ticket.append("Precio Total Global: $").append(String.format("%.2f", globalTotalPrice)).append("\n");
        ticket.append("********************************\n");
        ticket.append("Gracias por su compra.\n");
        ticket.append("********************************\n");
        ticket.append("\n");
        ticket.append("\n");
        ticket.append("\n");
        ticket.append("\n");

        // Convert the ticket content to a byte array
        byte[] bytes = ticket.toString().getBytes();

        // Specify the printer name (replace "POS58 Printer" with your printer's name)
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService posPrinter = null;
        for (PrintService printer : printServices) {
            if (printer.getName().equalsIgnoreCase("POS58 Printer(2)")) { // Replace "POS58 Printer" with your printer's name
                posPrinter = printer;
                break;
            }
        }

        if (posPrinter != null) {
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(bytes, flavor, null);
            DocPrintJob job = posPrinter.createPrintJob();
            try {
                job.print(doc, null);
                System.out.println("Ticket impreso correctamente.");
            } catch (PrintException e) {
                e.printStackTrace();
                System.out.println("Error al imprimir el ticket.");
            }
        } else {
            System.out.println("Impresora POS no encontrada.");
        }
    }
    
    
   public void imprimirhoy(Double venta){
       StringBuilder ticket = new StringBuilder();
       ticket.append("\n");
       ticket.append("\n");
       ticket.append("******* Corte del dia de Hoy********\n");
       ticket.append("Fecha:" + new java.util.Date(System.currentTimeMillis()));
       ticket.append("Monto Total: " + venta);
       ticket.append("\n");
       ticket.append("\n");
       ticket.append("\n");
       ticket.append("\n");
       
        byte[] bytes = ticket.toString().getBytes();
        
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService posPrinter = null;
        for (PrintService printer : printServices) {
            if (printer.getName().equalsIgnoreCase("POS58 Printer(2)")) { // Replace "POS58 Printer" with your printer's name
                posPrinter = printer;
                break;
            }
        }

        if (posPrinter != null) {
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(bytes, flavor, null);
            DocPrintJob job = posPrinter.createPrintJob();
            try {
                job.print(doc, null);
                System.out.println("Ticket impreso correctamente.");
            } catch (PrintException e) {
                e.printStackTrace();
                System.out.println("Error al imprimir el ticket.");
            }
        } else {
            System.out.println("Impresora POS no encontrada.");
        }
        
 
   } 
}

