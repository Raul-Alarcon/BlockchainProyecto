package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import models.contratos;

public class Block implements Serializable {

    private int id;
    private int nonce;
    private long timeStamp;
    private String hash; // según Nakamoto (2008) es el Root Hash
    private String previousHash;
    private ArrayList<contratos> listaContratos;

    public Block(int pId, String pPrevHash, ArrayList<contratos> pListaContratos) { // <-- ¡IMPORTANTE!
        this.id = pId;
        this.timeStamp = new Date().getTime();
        this.previousHash = pPrevHash;
        this.listaContratos = pListaContratos;
        this.nonce = -1;
        this.hash = null;
    }

//    public Block() {
//        this.timeStamp = new Date().getTime();
//        this.listaContratos= new ArrayList<>();
//        this.nonce=-1;
//        this.hash=null;
//        this.id = -1;
//    }
    public boolean register(int pNonce, String pHash) {
        if ((this.id > -1) && (this.nonce < 0) && (this.hash == null)) {
            this.nonce = pNonce;
            this.hash = pHash;
            return true;
        } else {
            return false;
        }
    }

    public int countContratos() {
        return this.listaContratos.size();
    }

    public int getId() {
        return id;
    }

    public int getNonce() {
        return nonce;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

//    public void setContrato(contratos pContrato)
//    {
//        this.listaContratos.add(new contratos(
//                String.valueOf(this.listaContratos.size()),pContrato.getParteA(), pContrato.getParteB()
//        ));
//    }
    public contratos getContrato(int pId) {
        return this.listaContratos.get(pId);
    }

    public ArrayList<contratos> getListaContratos() {
        return listaContratos;
    }

    @Override
    public String toString() {
        String sCad = Integer.toString(id) + Long.toString(timeStamp) + this.previousHash;
        for (int i = 0; i < this.listaContratos.size(); i++) {
            sCad = sCad + this.listaContratos.get(i).toString();
        }
        return sCad;
    }
}
