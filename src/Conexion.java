/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mauri
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexion {
    private static final String URL = "jdbc:mysql://db-casahogar.cpskw68mqfah.us-east-2.rds.amazonaws.com/uniformes";
    private static  String USER;
    private static  String PASSWORD;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    

    
    // El método ObtenerUsuario puede permanecer igual
    public String ObtenerUsuario() throws SQLException {
        String rol = null;
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement("SELECT rol FROM Usuarios WHERE usuario = ? AND contraseña = ?")) {
            statement.setString(1, USER);
            statement.setString(2, PASSWORD);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    rol = resultSet.getString("rol");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al conectar a la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
        return rol;
    }
    
    public void Declarar(String usuario,String contra){
        Conexion.USER=usuario;
        Conexion.PASSWORD=contra;
    }
}
