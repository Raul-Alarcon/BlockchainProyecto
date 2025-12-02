package controller;

import models.Block;
import models.BlockChain;
import models.NodeData;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class ServidorContratos implements Runnable {
    
    private Thread tListener;
    private NodeData currentNode;
    private ArrayList<NodeData> aOtherServers;
    private ArrayList<NodeData> aClients;
    private ServerSocket svrSocket;
    private BlockChain blockchain;
    private JTextArea txtMessages;
    
    public ServidorContratos(NodeData nodeData, BlockChain bc, JTextArea txtArea) {
        this.currentNode = nodeData;
        this.blockchain = bc;
        this.txtMessages = txtArea;
        this.aOtherServers = new ArrayList<>();
        this.aClients = new ArrayList<>();
        this.startServer();
    }
    
    private void startServer() {
        try {
            InetAddress iAddress = InetAddress.getByName(this.currentNode.getIPAddress());
            InetSocketAddress sNetServer = new InetSocketAddress(iAddress, this.currentNode.getSocketNum());
            svrSocket = new ServerSocket();
            svrSocket.bind(sNetServer);
            tListener = new Thread(this);
            tListener.start();
            log("Servidor iniciado en " + currentNode.getIPAddress() + ":" + currentNode.getSocketNum());
        } catch (Exception e) {
            log("Error al iniciar servidor: " + e.getMessage());
        }
    }
    
    public void registerServers(ArrayList<NodeData> servers) {
        this.aOtherServers = servers;
        log("Servidores registrados: " + servers.size());
    }
    
    public void registerClients(ArrayList<NodeData> clients) {
        this.aClients = clients;
        log("Clientes registrados: " + clients.size());
    }
    
    public boolean broadcastBlock(Block block) {
        try {
            for (NodeData server : aOtherServers) {
                if (!server.getNodeName().equals(currentNode.getNodeName())) {
                    Socket socket = new Socket(server.getIPAddress(), server.getSocketNum());
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(block);
                    socket.close();
                }
            }
            return true;
        } catch (Exception e) {
            log("Error en broadcast: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = svrSocket.accept();
                InputStream is = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                
                Object obj = ois.readObject();
                
                if (obj instanceof String) {
                    String jsonContrato = (String) obj;
                    blockchain.minarBloque(jsonContrato);
                    Block ultimoBloque = blockchain.obtenerUltimoBloque();
                    broadcastBlock(ultimoBloque);
                    log("Bloque minado: #" + ultimoBloque.getIndice());
                } else if (obj instanceof Block) {
                    Block bloque = (Block) obj;
                    blockchain.getCadena().add(bloque);
                    log("Bloque recibido de otro servidor: #" + bloque.getIndice());
                }
                
                socket.close();
            } catch (Exception e) {
                log("Error en servidor: " + e.getMessage());
            }
        }
    }
    
    private void log(String mensaje) {
        if (txtMessages != null) {
            txtMessages.append(mensaje + "\n");
        }
        System.out.println("[Servidor] " + mensaje);
    }
    
    public BlockChain getBlockchain() {
        return blockchain;
    }
}
