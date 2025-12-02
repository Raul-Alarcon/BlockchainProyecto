/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.io.Serializable;

/**
 *
 * @author Raul
 */
public class servicio implements Serializable{
    private String idServicio;
    private String descripcion;
    private String estado;
    private double montoServicio;

    public servicio(String idServicio, String descripcion, String estado, double montoServicio) {
        this.idServicio = idServicio;
        this.descripcion = descripcion;
        this.estado = estado;
        this.montoServicio = montoServicio;
    }

    public String getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(String idServicio) {
        this.idServicio = idServicio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getMontoServicio() {
        return montoServicio;
    }

    public void setMontoServicio(double montoServicio) {
        this.montoServicio = montoServicio;
    }
}
