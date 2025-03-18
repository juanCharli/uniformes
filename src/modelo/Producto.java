/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Juan Carlos
 */
public class Producto {
    private int idProducto;
    private String nombre;
    private String talla;
    private double precio;
    private int idTipo;

    public Producto(int idProducto, String nombre, String talla, double precio, int idTipo) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.talla = talla;
        this.precio = precio;
        this.idTipo = idTipo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTalla() {
        return talla;
    }

    public double getPrecio() {
        return precio;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public void setPrecio(double precio) {
        if (precio <= 0){
            throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        }
        this.precio = precio;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }
    
    
}
