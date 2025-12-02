package controller;

import models.NodeData;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class WalletContratos {
    
    private NodeData walletNode;
    private NodeData serverNode;
    
    public WalletContratos(NodeData wallet, NodeData server) {
        this.walletNode = wallet;
        this.serverNode = server;
    }
    
    public boolean enviarContrato(models.contratos contrato) {
        try {
            Socket socket = new Socket(serverNode.getIPAddress(), serverNode.getSocketNum());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(contrato);
            
            // MEJORA 3: Esperar confirmación del servidor
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Object respuesta = ois.readObject();
                
                if (respuesta instanceof String) {
                    String confirmacion = (String) respuesta;
                    if (confirmacion.startsWith("CONTRACT_RECEIVED:")) {
                        String[] partes = confirmacion.split(":");
                        String contratoId = partes[1];
                        String pendientes = partes[2];
                        System.out.println("✅ [Wallet] Contrato recibido por el servidor");
                        System.out.println("📝 [Wallet] Contratos pendientes en servidor: " + pendientes);
                    } else if (confirmacion.startsWith("BLOCK_MINED:")) {
                        String[] partes = confirmacion.split(":");
                        String blockId = partes[1];
                        String blockHash = partes[2];
                        System.out.println("✅ [Wallet] Contrato minado en Bloque #" + blockId);
                        System.out.println("🔒 [Wallet] Hash: " + blockHash.substring(0, 16) + "...");
                    }
                }
            } catch (Exception e) {
                // Si no hay confirmación, no es crítico
                System.out.println("⚠️ [Wallet] No se recibió confirmación del servidor");
            }
            
            socket.close();
            System.out.println("[Wallet] Contrato enviado al servidor");
            return true;
        } catch (Exception e) {
            System.err.println("[Wallet] Error al enviar contrato: " + e.getMessage());
            return false;
        }
    }
    
    // Método para recibir notificaciones de bloques minados
    public String esperarConfirmacion(Socket socket) {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Object obj = ois.readObject();
            if (obj instanceof String) {
                return (String) obj;
            }
        } catch (Exception e) {
            System.err.println("[Wallet] Error al recibir confirmación: " + e.getMessage());
        }
        return null;
    }
    
    public boolean enviarMultiplesContratos(java.util.ArrayList<models.contratos> contratos) {
        try {
            Socket socket = new Socket(serverNode.getIPAddress(), serverNode.getSocketNum());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            
            for (models.contratos contrato : contratos) {
                oos.writeObject(contrato);
                oos.flush();
                Thread.sleep(100); // Pequeño delay entre contratos
            }
            
            // Esperar confirmación del servidor
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Object respuesta = ois.readObject();
                
                if (respuesta instanceof String) {
                    String confirmacion = (String) respuesta;
                    if (confirmacion.startsWith("BLOCK_MINED:")) {
                        String[] partes = confirmacion.split(":");
                        String blockId = partes[1];
                        String cantidadContratos = partes.length > 3 ? partes[3] : "?"; 
                        System.out.println("✅ [Wallet] Bloque #" + blockId + " minado con " + cantidadContratos + " contrato(s)");
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ [Wallet] No se recibió confirmación");
            }
            
            socket.close();
            System.out.println("[Wallet] " + contratos.size() + " contratos enviados al servidor");
            return true;
        } catch (Exception e) {
            System.err.println("[Wallet] Error al enviar contratos: " + e.getMessage());
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
