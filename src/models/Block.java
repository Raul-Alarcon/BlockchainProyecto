/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

// smartcontracts/Bloque.java
package models;

import utils.UtilBlockchain;
import java.time.Instant;
import java.util.Objects;

public class Block {
    private int indice;
    private long timestamp; // en segundos
    private String hashAnterior;
    private String datos; // JSON de uno o varios contratos
    private int nonce;
    private String hash;

    // Constructor para el bloque génesis
    public Block(String datos) {
        this.indice = 0;
        this.timestamp = Instant.now().getEpochSecond();
        this.hashAnterior = "0";
        this.datos = datos;
        this.nonce = 0;
        this.hash = calcularHash();
    }

    // Constructor para bloques posteriores
    public Block(int indice, String hashAnterior, String datos) {
        this.indice = indice;
        this.timestamp = Instant.now().getEpochSecond();
        this.hashAnterior = hashAnterior;
        this.datos = datos;
        this.nonce = 0;
        this.hash = calcularHash();
    }

    public String calcularHash() {
        String entrada = indice + timestamp + hashAnterior + datos + nonce;
        return UtilBlockchain.sha256(entrada);
    }

    // Getters
    public int getIndice() { return indice; }
    public long getTimestamp() { return timestamp; }
    public String getHashAnterior() { return hashAnterior; }
    public String getDatos() { return datos; }
    public int getNonce() { return nonce; }
    public String getHash() { return hash; }

    // Setter para nonce (usado en minería)
    public void setNonce(int nonce) {
        this.nonce = nonce;
        this.hash = calcularHash();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block bloque = (Block) o;
        return indice == bloque.indice &&
               timestamp == bloque.timestamp &&
               nonce == bloque.nonce &&
               Objects.equals(hashAnterior, bloque.hashAnterior) &&
               Objects.equals(datos, bloque.datos) &&
               Objects.equals(hash, bloque.hash);
    }

    @Override
    public String toString() {
        return "Bloque{" +
                "indice=" + indice +
                ", timestamp=" + timestamp +
                ", hashAnterior='" + hashAnterior + '\'' +
                ", datos='" + datos + '\'' +
                ", nonce=" + nonce +
                ", hash='" + hash + '\'' +
                '}';
    }
}