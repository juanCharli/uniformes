/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Juan Carlos
 */
import conexion.Conexion;
import java.sql.*;
import java.util.List;

public class ModeloVentas {
    private ModeloProducto productoModel;

    public ModeloVentas() {
        this.productoModel = new ModeloProducto();
    }

    public boolean registrarVenta(List<Object[]> ventas, int usuarioId) {
        try (Connection connection = Conexion.getConnection()) {
            connection.setAutoCommit(false); // Iniciar transacción

            // Insertar orden
            int idOrden = insertarOrden(connection, usuarioId);

            if (idOrden == -1) {
                connection.rollback();
                return false;
            }

            String sqlVentas = "INSERT INTO Ventas (id_producto, id_usuario, cantidad, fecha_hora, id_orden, precio_total) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statementVentas = connection.prepareStatement(sqlVentas)) {
                for (Object[] venta : ventas) {
                    int productID = productoModel.getProductID((String) venta[0], (String) venta[1], (String) venta[2]);
                    int quantity = (int) venta[3];
                    double totalPriceForRow = (double) venta[5];

                    if (!checkStock(productID, quantity)) {
                        connection.rollback();
                        return false;
                    }

                    // Registrar venta
                    statementVentas.setInt(1, productID);
                    statementVentas.setInt(2, usuarioId);
                    statementVentas.setInt(3, quantity);
                    statementVentas.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    statementVentas.setInt(5, idOrden);
                    statementVentas.setDouble(6, totalPriceForRow);
                    statementVentas.addBatch();
                }

                // Ejecutar todas las ventas
                statementVentas.executeBatch();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int insertarOrden(Connection connection, int usuarioId) throws SQLException {
        String sqlOrden = "INSERT INTO Ordenes (id_usuario, fecha_hora) VALUES (?, ?)";
        try (PreparedStatement statementOrden = connection.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
            statementOrden.setInt(1, usuarioId);
            statementOrden.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            statementOrden.executeUpdate();

            try (ResultSet generatedKeys = statementOrden.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean checkStock(int productID, int quantity) {
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

    public double getUnitPrice(int productID) {
        double price = -1;
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT precio FROM Productos WHERE id_producto = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, productID);
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
}

