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
import java.net.SocketTimeoutException;
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
    private Thread conflictResolverThread;
    private ArrayList<Socket> connectedWallets;
    private ArrayList<models.contratos> contratosPendientes; // Pool de contratos
    
    public ServidorContratos(NodeData nodeData, BlockChain bc, JTextArea txtArea) {
        this.currentNode = nodeData;
        this.blockchain = bc;
        this.txtMessages = txtArea;
        this.aOtherServers = new ArrayList<>();
        this.aClients = new ArrayList<>();
        this.connectedWallets = new ArrayList<>();
        this.contratosPendientes = new ArrayList<>(); // Inicializar pool
        this.startServer();
        
        // MEJORA 1: Sincronizar al iniciar (esperar 2 segundos para que otros servidores estén listos)
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                syncWithAllServers();
            } catch (Exception e) {
                log("⚠️ Error en sincronización inicial: " + e.getMessage());
            }
        }).start();
        
        // MEJORA 2: Iniciar resolución de conflictos periódica
        startConflictResolver();
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
        int exitosos = 0;
        int intentos = 0;
        
        log("📡 Iniciando broadcast del Bloque #" + block.getId() + " a " + aOtherServers.size() + " servidor(es)...");
        
        for (NodeData server : aOtherServers) {
            if (!server.getNodeName().equals(currentNode.getNodeName())) {
                intentos++;
                try {
                    Socket socket = new Socket(server.getIPAddress(), server.getSocketNum());
                    socket.setSoTimeout(5000); // Timeout de 5 segundos
                    
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(block);
                    oos.flush();
                    
                    log("✅ Bloque enviado a " + server.getNodeName());
                    exitosos++;
                    
                    socket.close();
                } catch (Exception e) {
                    log("❌ Error enviando a " + server.getNodeName() + ": " + e.getMessage());
                }
            }
        }
        
        log("📡 Broadcast completado: " + exitosos + "/" + intentos + " servidores");
        return exitosos > 0;
    }
    
    // Sincronización de blockchain completa con otros servidores
    public void syncBlockchain(NodeData server) {
        try {
            Socket socket = new Socket(server.getIPAddress(), server.getSocketNum());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject("REQUEST_BLOCKCHAIN");
            
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Object obj = ois.readObject();
            
            if (obj instanceof BlockChain) {
                BlockChain receivedChain = (BlockChain) obj;
                
                // Consenso: adoptar cadena más larga si es válida
                if (receivedChain.size() > blockchain.size() && receivedChain.isChainValid()) {
                    blockchain = receivedChain;
                    log("✅ Blockchain sincronizada desde " + server.getNodeName() + 
                        " (" + receivedChain.size() + " bloques)");
                } else {
                    log("⚠️ Blockchain recibida no es válida o más corta");
                }
            }
            socket.close();
        } catch (Exception e) {
            log("Error en sincronización: " + e.getMessage());
        }
    }
    
    // Sincronizar con todos los servidores registrados
    public void syncWithAllServers() {
        log("🔄 Iniciando sincronización con todos los servidores...");
        for (NodeData server : aOtherServers) {
            if (!server.getNodeName().equals(currentNode.getNodeName())) {
                syncBlockchain(server);
            }
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
                    String mensaje = (String) obj;
                    
                    if (mensaje.equals("REQUEST_BLOCKCHAIN")) {
                        // Enviar blockchain completa
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(blockchain);
                        log("📤 Blockchain enviada a cliente");
                    }
                } else if (obj instanceof models.contratos) {
                    // Recibir contrato del wallet
                    models.contratos contrato = (models.contratos) obj;
                    contratosPendientes.add(contrato);
                    
                    log("📝 Contrato " + contrato.getIdContrato() + " agregado al pool (Total: " + contratosPendientes.size() + ")");
                    
                    // Esperar más contratos (timeout de 1 segundo)
                    socket.setSoTimeout(1000);
                    
                    while (true) {
                        try {
                            Object nextObj = ois.readObject();
                            if (nextObj instanceof models.contratos) {
                                models.contratos nextContrato = (models.contratos) nextObj;
                                contratosPendientes.add(nextContrato);
                                log("📝 Contrato " + nextContrato.getIdContrato() + " agregado al pool (Total: " + contratosPendientes.size() + ")");
                            } else {
                                break;
                            }
                        } catch (Exception e) {
                            // Timeout o fin de stream - proceder a minar
                            break;
                        }
                    }
                    
                    // Minar todos los contratos pendientes
                    ArrayList<models.contratos> contratosParaMinar = new ArrayList<>(contratosPendientes);
                    blockchain.createBlock(contratosParaMinar);
                    blockchain.mineBlock();
                    Block ultimoBloque = blockchain.getLastBlock();
                    broadcastBlock(ultimoBloque);
                    
                    contratosPendientes.clear();
                    
                    log("✅ Bloque #" + ultimoBloque.getId() + " minado con " + contratosParaMinar.size() + " contrato(s)");
                    
                    // Notificar al wallet
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        String confirmacion = "BLOCK_MINED:" + ultimoBloque.getId() + ":" + ultimoBloque.getHash() + ":" + contratosParaMinar.size();
                        oos.writeObject(confirmacion);
                        log("📧 Confirmación enviada: Bloque #" + ultimoBloque.getId() + " con " + contratosParaMinar.size() + " contrato(s)");
                    } catch (Exception e) {
                        log("⚠️ No se pudo notificar al wallet");
                    }
                } else if (obj instanceof Block) {
                    Block bloque = (Block) obj;
                    log("📥 Bloque #" + bloque.getId() + " recibido de otro servidor");
                    log("   Hash: " + bloque.getHash().substring(0, 16) + "...");
                    log("   Contratos: " + bloque.countContratos());
                    
                    // Verificar si el bloque ya existe
                    if (blockchain.blockExist(bloque)) {
                        log("⚠️ Bloque #" + bloque.getId() + " ya existe en la cadena");
                    } else if (blockchain.addProvedBlock(bloque)) {
                        log("✅ Bloque #" + bloque.getId() + " agregado exitosamente");
                        log("🔗 Blockchain actualizada: " + blockchain.size() + " bloques");
                    } else {
                        log("❌ Bloque #" + bloque.getId() + " rechazado (inválido o no cumple PoW)");
                        log("   Verificando PoW...");
                        if (!blockchain.getProofOfWork_overBlock(bloque)) {
                            log("❌ PoW no válido para este bloque");
                        }
                    }
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
    
    public void setBlockchain(BlockChain bc) {
        this.blockchain = bc;
        log("🔄 Blockchain actualizada: " + bc.size() + " bloques");
    }
    
    // Minar bloque con todos los contratos pendientes
    public boolean minarContratosPendientes() {
        if (contratosPendientes.isEmpty()) {
            log("⚠️ No hay contratos pendientes para minar");
            return false;
        }
        
        // Crear copia de los contratos pendientes
        ArrayList<models.contratos> contratosParaMinar = new ArrayList<>(contratosPendientes);
        
        // Crear y minar bloque
        blockchain.createBlock(contratosParaMinar);
        blockchain.mineBlock();
        Block ultimoBloque = blockchain.getLastBlock();
        
        // Broadcast a otros servidores
        broadcastBlock(ultimoBloque);
        
        log("✅ Bloque #" + ultimoBloque.getId() + " minado con " + contratosParaMinar.size() + " contrato(s)");
        
        // Limpiar contratos pendientes
        contratosPendientes.clear();
        
        return true;
    }
    
    // Obtener cantidad de contratos pendientes
    public int getContratosPendientesCount() {
        return contratosPendientes.size();
    }
    
    // Verificar conectividad con otros servidores (heartbeat)
    public void checkServerHealth() {
        log("💓 Verificando estado de servidores...");
        for (NodeData server : aOtherServers) {
            if (!server.getNodeName().equals(currentNode.getNodeName())) {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(server.getIPAddress(), server.getSocketNum()), 2000);
                    socket.close();
                    log("✅ " + server.getNodeName() + " está activo");
                } catch (Exception e) {
                    log("❌ " + server.getNodeName() + " no responde");
                }
            }
        }
    }
    
    // Resolver conflictos: adoptar cadena más larga válida
    public boolean resolveConflicts() {
        int maxLength = blockchain.size();
        BlockChain newChain = null;
        
        log("🔍 Resolviendo conflictos... Longitud actual: " + maxLength);
        
        for (NodeData server : aOtherServers) {
            if (!server.getNodeName().equals(currentNode.getNodeName())) {
                try {
                    Socket socket = new Socket(server.getIPAddress(), server.getSocketNum());
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject("REQUEST_BLOCKCHAIN");
                    
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    BlockChain receivedChain = (BlockChain) ois.readObject();
                    
                    if (receivedChain.size() > maxLength && receivedChain.isChainValid()) {
                        maxLength = receivedChain.size();
                        newChain = receivedChain;
                    }
                    socket.close();
                } catch (Exception e) {
                    // Servidor no disponible, continuar con el siguiente
                }
            }
        }
        
        if (newChain != null) {
            blockchain = newChain;
            log("✅ Cadena reemplazada. Nueva longitud: " + maxLength);
            return true;
        }
        
        log("✅ Nuestra cadena es la más larga");
        return false;
    }
    
    // MEJORA 2: Iniciar thread para resolver conflictos periódicamente
    private void startConflictResolver() {
        conflictResolverThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(300000); // 5 minutos
                    log("🔄 Ejecutando resolución de conflictos automática...");
                    resolveConflicts();
                } catch (InterruptedException e) {
                    log("⚠️ Thread de resolución de conflictos interrumpido");
                    break;
                } catch (Exception e) {
                    log("⚠️ Error en resolución de conflictos: " + e.getMessage());
                }
            }
        });
        conflictResolverThread.setDaemon(true);
        conflictResolverThread.start();
        log("✅ Resolución de conflictos automática iniciada (cada 5 minutos)");
    }
    
    // Detener el servidor y sus threads
    public void shutdown() {
        try {
            if (conflictResolverThread != null) {
                conflictResolverThread.interrupt();
            }
            if (svrSocket != null && !svrSocket.isClosed()) {
                svrSocket.close();
            }
            log("🛑 Servidor detenido correctamente");
        } catch (Exception e) {
            log("⚠️ Error al detener servidor: " + e.getMessage());
        }
    }
}
