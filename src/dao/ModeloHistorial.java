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
import java.util.ArrayList;
import java.util.List;

public class ModeloHistorial {

    public List<Object[]> obtenerHistorialVentas(Date startDate, Date endDate) {
        List<Object[]> historial = new ArrayList<>();
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT p.nombre AS producto, V.cantidad, V.fecha_hora, V.precio_total, V.id_orden, p.talla "
                    + "FROM Ventas V "
                    + "JOIN Productos p ON V.id_producto = p.id_producto "
                    + "WHERE V.fecha_hora BETWEEN ? AND ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDate(1, startDate);
                statement.setDate(2, endDate);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        historial.add(new Object[]{
                            resultSet.getString("producto"),
                            resultSet.getString("talla"),
                            resultSet.getInt("cantidad"),
                            resultSet.getTimestamp("fecha_hora"),
                            resultSet.getDouble("precio_total"),
                            resultSet.getInt("id_orden")
                        });
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historial;
    }

    public List<Object[]> obtenerHistorialIngresos(Date startDate, Date endDate) {
        List<Object[]> historial = new ArrayList<>();
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT p.nombre, i.cantidad, i.fecha_hora, p.descripcion "
                    + "FROM Ingresos i "
                    + "JOIN Productos p ON i.id_producto = p.id_producto "
                    + "WHERE i.fecha_hora BETWEEN ? AND ?"; //permite filtrar por fechas
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    historial.add(new Object[]{
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion"),
                        resultSet.getInt("cantidad"),
                        resultSet.getTimestamp("fecha_hora")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historial;
    }

    public List<Object[]> obtenerStockActual() {
        List<Object[]> stock = new ArrayList<>();
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT p.id_producto, p.nombre, p.descripcion, inv.cantidad, p.precio "
                    + "FROM Inventario inv "
                    + "JOIN Productos p ON inv.id_producto = p.id_producto";
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    stock.add(new Object[]{
                        resultSet.getInt("id_producto"),
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion"),
                        resultSet.getInt("cantidad"),
                        resultSet.getDouble("precio")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stock;
    }
}
