/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Raul
 */
public class contratos implements Serializable, Cloneable{
    private String idContrato;
    private String parteA;
    private String parteB;
    private double valorTotal;
    private ArrayList<servicio> listaServicios;

    public contratos(String idContrato, String parteA, String parteB) {
        this.idContrato = idContrato;
        this.parteA = parteA;
        this.parteB = parteB;
        this.valorTotal = 0;
        this.listaServicios = new ArrayList<>();
    }

    public String getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(String idContrato) {
        this.idContrato = idContrato;
    }

    public String getParteA() {
        return parteA;
    }

    public void setParteA(String parteA) {
        this.parteA = parteA;
    }

    public String getParteB() {
        return parteB;
    }

    public void setParteB(String parteB) {
        this.parteB = parteB;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public ArrayList<servicio> getListaServicios() {
        return listaServicios;
    }

    // Este método recibe la información de un servicio y lo agrega a la lista.
    public void agregarServicio(String idServicio, String descripcion, String estado, double montoServicio) {
        servicio nuevoServicio = new servicio(idServicio, descripcion, estado, montoServicio);
        this.listaServicios.add(nuevoServicio);
        this.valorTotal += montoServicio;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(idContrato).append(parteA).append(parteB);
        sb.append(String.format("%.2f", valorTotal));
        for (servicio s : listaServicios) {
            sb.append(s.toString());
        }
        return sb.toString();
    }
    
    @Override
    public contratos clone() {
        contratos copia = new contratos(this.idContrato, this.parteA, this.parteB);
        copia.valorTotal = this.valorTotal;
        for (servicio s : this.listaServicios) {
            copia.listaServicios.add(s.clone());
        }
        return copia;
    }
}
