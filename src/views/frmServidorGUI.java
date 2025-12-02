package views;

import controller.ServidorContratos;
import models.BlockChain;
import models.NodeData;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

public class frmServidorGUI extends JFrame {
    
    private NodeData currentNode;
    private ServidorContratos servidor;
    private BlockChain blockchain;
    private JTextArea txtMessages;
    private JLabel lblNombre;
    private JLabel lblIP;
    private JLabel lblPuerto;
    private JLabel lblServidores;
    private JButton btnVerBlockchain;
    
    public frmServidorGUI(NodeData nodeData) {
        this.currentNode = nodeData;
        this.blockchain = new BlockChain();
        initComponents();
        this.servidor = new ServidorContratos(nodeData, blockchain, txtMessages);
    }
    
    private void initComponents() {
        setTitle("Servidor Blockchain - " + currentNode.getNodeName());
        setSize(600, 500);
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior - Info del servidor
        JPanel panelInfo = new JPanel();
        panelInfo.setBackground(new Color(38, 48, 58));
        panelInfo.setLayout(new GridLayout(4, 1, 5, 5));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        lblNombre = new JLabel("Servidor: " + currentNode.getNodeName());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 18));
        lblNombre.setForeground(new Color(95, 254, 243));
        
        lblIP = new JLabel("IP: " + currentNode.getIPAddress());
        lblIP.setForeground(Color.WHITE);
        
        lblPuerto = new JLabel("Puerto: " + currentNode.getSocketNum());
        lblPuerto.setForeground(Color.WHITE);
        
        lblServidores = new JLabel("Servidores conectados: 0");
        lblServidores.setForeground(new Color(255, 255, 5));
        
        panelInfo.add(lblNombre);
        panelInfo.add(lblIP);
        panelInfo.add(lblPuerto);
        panelInfo.add(lblServidores);
        
        // Panel central - Mensajes
        JPanel panelMensajes = new JPanel(new BorderLayout());
        panelMensajes.setBorder(BorderFactory.createTitledBorder("Actividad del Servidor"));
        
        txtMessages = new JTextArea();
        txtMessages.setEditable(false);
        txtMessages.setBackground(new Color(243, 233, 233));
        txtMessages.setForeground(new Color(86, 13, 13));
        JScrollPane scroll = new JScrollPane(txtMessages);
        panelMensajes.add(scroll, BorderLayout.CENTER);
        
        // Panel inferior - Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        btnVerBlockchain = new JButton("Ver Blockchain");
        btnVerBlockchain.addActionListener(e -> verBlockchain());
        panelBotones.add(btnVerBlockchain);
        
        add(panelInfo, BorderLayout.NORTH);
        add(panelMensajes, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
    }
    
    public void registerServers(ArrayList<NodeData> servers) {
        servidor.registerServers(servers);
        int count = 0;
        for (NodeData node : servers) {
            if (!node.getNodeName().equals(currentNode.getNodeName())) {
                count++;
            }
        }
        lblServidores.setText("Servidores conectados: " + count);
    }
    
    public void registerClients(ArrayList<NodeData> clients) {
        servidor.registerClients(clients);
    }
    
    public BlockChain getBlockchain() {
        return blockchain;
    }
    
    public void setBlockchain(BlockChain bc) {
        this.blockchain = bc;
        this.servidor = new ServidorContratos(currentNode, blockchain, txtMessages);
    }
    
    private void verBlockchain() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== BLOCKCHAIN ==========\n");
        sb.append("Total de bloques: ").append(blockchain.size()).append("\n\n");
        
        for (int i = 0; i < blockchain.getCadena().size(); i++) {
            var bloque = blockchain.getCadena().get(i);
            sb.append("Bloque #").append(bloque.getIndice()).append("\n");
            sb.append("Hash: ").append(bloque.getHash()).append("\n");
            sb.append("Hash Anterior: ").append(bloque.getHashAnterior()).append("\n");
            sb.append("Nonce: ").append(bloque.getNonce()).append("\n");
            sb.append("Datos: ").append(bloque.getDatos().substring(0, Math.min(50, bloque.getDatos().length()))).append("...\n");
            sb.append("--------------------------------\n");
        }
        
        JTextArea txtBlockchain = new JTextArea(sb.toString());
        txtBlockchain.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtBlockchain);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Blockchain", JOptionPane.INFORMATION_MESSAGE);
    }
}
