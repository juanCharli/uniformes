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

public class ModeloInventario {
    private ModeloProducto productoModel;
    private Connection connection;

    public ModeloInventario() throws SQLException {
        this.productoModel = new ModeloProducto();
        this.connection = Conexion.getConnection();
    }

    public boolean registrarIngreso(int productoId, int usuarioId, int cantidad) {
        try (Connection connection = Conexion.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "INSERT INTO Ingresos (id_producto, id_usuario, cantidad, fecha_hora) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, productoId);
            stmt.setInt(2, usuarioId);
            stmt.setInt(3, cantidad);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}