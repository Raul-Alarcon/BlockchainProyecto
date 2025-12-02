package controller;

import models.NodeData;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class WalletContratos {
    
    private NodeData walletNode;
    private NodeData serverNode;
    
    public WalletContratos(NodeData wallet, NodeData server) {
        this.walletNode = wallet;
        this.serverNode = server;
    }
    
    public boolean enviarContrato(String jsonContrato) {
        try {
            Socket socket = new Socket(serverNode.getIPAddress(), serverNode.getSocketNum());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(jsonContrato);
            socket.close();
            System.out.println("[Wallet] Contrato enviado al servidor");
            return true;
        } catch (Exception e) {
            System.err.println("[Wallet] Error al enviar contrato: " + e.getMessage());
            return false;
        }
    }
    
    public NodeData getWalletNode() {
        return walletNode;
    }
    
    public void setServerNode(NodeData server) {
        this.serverNode = server;
    }
}
