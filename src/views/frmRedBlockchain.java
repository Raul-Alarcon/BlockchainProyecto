package views;

import models.BlockChain;
import models.NodeData;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class frmRedBlockchain extends JFrame {
    
    private ArrayList<NodeData> aServers;
    private ArrayList<NodeData> aClients;
    private ArrayList<JFrame> aFrmServers;
    private ArrayList<JFrame> aFrmWallets;
    
    private JTextField txtServerName;
    private JTextField txtServerIP;
    private JTextField txtServerPort;
    private JTextField txtWalletName;
    private JComboBox<String> cmbServers;
    private JButton btnCrearServidor;
    private JButton btnCrearWallet;
    
    public frmRedBlockchain() {
        this.aServers = new ArrayList<>();
        this.aClients = new ArrayList<>();
        this.aFrmServers = new ArrayList<>();
        this.aFrmWallets = new ArrayList<>();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Red Blockchain - Gestión de Servidores y Wallets");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLayout(new BorderLayout(10, 10));
        
        // Panel título
        JLabel lblTitulo = new JLabel("Red Blockchain de Contratos Inteligentes", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(2, 36, 159));
        add(lblTitulo, BorderLayout.NORTH);
        
        // Panel central con dos secciones
        JPanel panelCentral = new JPanel(new GridLayout(2, 1, 10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel Servidor
        JPanel panelServidor = new JPanel();
        panelServidor.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(178, 8, 8), 2),
            " Crear Servidor ",
            0, 0,
            new Font("Arial", Font.BOLD, 12),
            new Color(178, 8, 8)
        ));
        panelServidor.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        panelServidor.add(new JLabel("Nombre:"));
        txtServerName = new JTextField(10);
        panelServidor.add(txtServerName);
        
        panelServidor.add(new JLabel("IP:"));
        txtServerIP = new JTextField("127.0.0.1", 10);
        panelServidor.add(txtServerIP);
        
        panelServidor.add(new JLabel("Puerto:"));
        txtServerPort = new JTextField("7000", 6);
        panelServidor.add(txtServerPort);
        
        btnCrearServidor = new JButton("Crear Servidor");
        btnCrearServidor.addActionListener(e -> crearServidor());
        panelServidor.add(btnCrearServidor);
        
        // Panel Wallet
        JPanel panelWallet = new JPanel();
        panelWallet.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(32, 139, 2), 2),
            " Crear Wallet ",
            0, 0,
            new Font("Arial", Font.BOLD, 12),
            new Color(32, 139, 2)
        ));
        panelWallet.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        panelWallet.add(new JLabel("Nombre Wallet:"));
        txtWalletName = new JTextField(10);
        panelWallet.add(txtWalletName);
        
        panelWallet.add(new JLabel("Servidor:"));
        cmbServers = new JComboBox<>();
        cmbServers.setPreferredSize(new Dimension(150, 25));
        panelWallet.add(cmbServers);
        
        btnCrearWallet = new JButton("Crear Wallet");
        btnCrearWallet.addActionListener(e -> crearWallet());
        panelWallet.add(btnCrearWallet);
        
        panelCentral.add(panelServidor);
        panelCentral.add(panelWallet);
        
        add(panelCentral, BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
    }
    
    private void crearServidor() {
        String nombre = txtServerName.getText().trim().toUpperCase();
        String ip = txtServerIP.getText().trim();
        int puerto = Integer.parseInt(txtServerPort.getText().trim()) + aServers.size();
        
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un nombre para el servidor");
            return;
        }
        
        NodeData nodeServer = new NodeData(nombre, ip, puerto);
        
        // Crear GUI del servidor
        frmServidorGUI frmServer = new frmServidorGUI(nodeServer);
        
        // Si hay servidores previos, sincronizar blockchain
        if (aFrmServers.size() > 0) {
            // Obtener la blockchain más actualizada
            BlockChain bcActualizada = null;
            int maxSize = 0;
            
            for (JFrame frm : aFrmServers) {
                BlockChain bc = ((frmServidorGUI) frm).getBlockchain();
                if (bc.size() > maxSize) {
                    maxSize = bc.size();
                    bcActualizada = bc;
                }
            }
            
            if (bcActualizada != null) {
                frmServer.setBlockchain(bcActualizada);
                System.out.println("✅ Blockchain sincronizada: " + bcActualizada.size() + " bloques");
            }
        }
        
        aServers.add(nodeServer);
        cmbServers.addItem(nombre);
        frmServer.setVisible(true);
        aFrmServers.add(frmServer);
        
        // Broadcast a todos los servidores
        broadcastServers();
        
        txtServerName.setText("");
        JOptionPane.showMessageDialog(this, "Servidor creado: " + nombre + " en puerto " + puerto);
    }
    
    private void crearWallet() {
        String nombre = txtWalletName.getText().trim().toUpperCase();
        int serverIndex = cmbServers.getSelectedIndex();
        
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un nombre para el wallet");
            return;
        }
        
        if (serverIndex < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un servidor");
            return;
        }
        
        NodeData serverNode = aServers.get(serverIndex);
        int walletPort = 8000 + aClients.size();
        NodeData walletNode = new NodeData(nombre, serverNode.getIPAddress(), walletPort);
        aClients.add(walletNode);
        
        // Crear GUI del wallet
        frmWalletGUI frmWallet = new frmWalletGUI(walletNode, serverNode);
        frmWallet.setVisible(true);
        aFrmWallets.add(frmWallet);
        
        // Registrar clientes en servidores
        broadcastClients();
        
        txtWalletName.setText("");
        JOptionPane.showMessageDialog(this, "Wallet creado: " + nombre);
    }
    
    private void broadcastServers() {
        for (JFrame frm : aFrmServers) {
            ((frmServidorGUI) frm).registerServers(aServers);
        }
        for (JFrame frm : aFrmWallets) {
            ((frmWalletGUI) frm).updateServers(aServers);
        }
    }
    
    private void broadcastClients() {
        for (JFrame frm : aFrmServers) {
            ((frmServidorGUI) frm).registerClients(aClients);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new frmRedBlockchain().setVisible(true);
        });
    }
}
