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

public class ModeloProducto {

    public List<String> getCategorias() {
        List<String> categorias = new ArrayList<>();
        try (Connection connection = Conexion.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT nombre FROM Categoria")) {
            while (rs.next()) {
                categorias.add(rs.getString("nombre"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return categorias;
    }

    public List<String> getTiposPorCategoria(String categoria) {
        List<String> tipos = new ArrayList<>();
        try (Connection connection = Conexion.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT nombre FROM Tipos WHERE id_categoria = (SELECT id_categoria FROM Categoria WHERE nombre = ?)")) {
            stmt.setString(1, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tipos.add(rs.getString("nombre"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tipos;
    }

    public List<String> getProductosPorTipo(String categoria, String tipo) {
        List<String> productos = new ArrayList<>();
        try (Connection connection = Conexion.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT talla FROM Productos WHERE id_tipo = (SELECT id_tipo FROM Tipos WHERE nombre = ? AND id_categoria = (SELECT id_categoria FROM Categoria WHERE nombre = ?)) ORDER BY CAST(talla AS UNSIGNED) ASC, LENGTH(talla) ASC, talla ASC")) {
            stmt.setString(1, tipo);
            stmt.setString(2, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(rs.getString("talla"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }

    public int getProductID(String categoria, String tipo, String producto) {
        int productId = 0;
        try (Connection connection = Conexion.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT id_producto FROM Productos WHERE talla = ? AND id_tipo = (SELECT id_tipo FROM Tipos WHERE nombre = ? AND id_categoria = (SELECT id_categoria FROM Categoria WHERE nombre = ?))")) {
            stmt.setString(1, producto);
            stmt.setString(2, tipo);
            stmt.setString(3, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    productId = rs.getInt("id_producto");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productId;
    }
}
