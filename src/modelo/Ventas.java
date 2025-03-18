/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.Date;

/**
 *
 * @author Juan Carlos
 */
public class Ventas {
    private int idVenta;
    private int idProducto;
    private int idUsuario;
    private int idOrden;
    private int cantidad;
    private Date fecha;
    private double precioTotal;

    public Ventas(int idVenta, int idproducto, int idUsuario, int idOrden, int cantidad, Date fecha, double precioTotal) {
        this.idVenta = idVenta;
        this.idProducto = idproducto;
        this.idUsuario = idUsuario;
        this.idOrden = idOrden;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.precioTotal = precioTotal;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdproducto() {
        return idProducto;
    }

    public void setIdproducto(int idproducto) {
        this.idProducto = idproducto;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(double precioTotal) {
        this.precioTotal = precioTotal;
    }
    
    
}
