package views;

import controller.WalletContratos;
import models.NodeData;
import models.contratos;
import models.servicio;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class frmWalletGUI extends JFrame {
    
    private NodeData walletNode;
    private NodeData serverNode;
    private WalletContratos wallet;
    private ArrayList<NodeData> servidores;
    
    private JLabel lblWalletName;
    private JLabel lblServerInfo;
    private JTextField txtIdContrato;
    private JTextField txtParteA;
    private JTextField txtParteB;
    private JTextField txtIdServicio;
    private JTextField txtDescripcion;
    private JTextField txtMonto;
    private JComboBox<String> cmbEstado;
    private JComboBox<String> cmbServidores;
    private JButton btnAgregarServicio;
    private JButton btnEnviarContrato;
    private JTextArea txtContratoActual;
    
    private contratos contratoActual;
    
    public frmWalletGUI(NodeData wallet, NodeData server) {
        this.walletNode = wallet;
        this.serverNode = server;
        this.wallet = new WalletContratos(wallet, server);
        this.servidores = new ArrayList<>();
        this.contratoActual = null;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Wallet - " + walletNode.getNodeName());
        setSize(700, 600);
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior - Info
        JPanel panelInfo = new JPanel(new GridLayout(2, 1));
        panelInfo.setBackground(new Color(38, 48, 58));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        lblWalletName = new JLabel("Wallet: " + walletNode.getNodeName());
        lblWalletName.setFont(new Font("Arial", Font.BOLD, 18));
        lblWalletName.setForeground(new Color(128, 0, 0));
        lblWalletName.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblServerInfo = new JLabel("Servidor: " + serverNode.getNodeName() + " (" + serverNode.getIPAddress() + ":" + serverNode.getSocketNum() + ")");
        lblServerInfo.setForeground(Color.WHITE);
        lblServerInfo.setHorizontalAlignment(SwingConstants.CENTER);
        
        panelInfo.add(lblWalletName);
        panelInfo.add(lblServerInfo);
        
        // Panel central - Formulario
        JPanel panelFormulario = new JPanel();
        panelFormulario.setLayout(new BoxLayout(panelFormulario, BoxLayout.Y_AXIS));
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Sección Contrato
        JPanel panelContrato = new JPanel(new GridLayout(4, 2, 5, 5));
        panelContrato.setBorder(BorderFactory.createTitledBorder("Datos del Contrato"));
        
        panelContrato.add(new JLabel("ID Contrato:"));
        txtIdContrato = new JTextField();
        panelContrato.add(txtIdContrato);
        
        panelContrato.add(new JLabel("Parte A (Cliente):"));
        txtParteA = new JTextField();
        panelContrato.add(txtParteA);
        
        panelContrato.add(new JLabel("Parte B (Proveedor):"));
        txtParteB = new JTextField();
        panelContrato.add(txtParteB);
        
        JButton btnCrearContrato = new JButton("Crear Contrato");
        btnCrearContrato.addActionListener(e -> crearContrato());
        panelContrato.add(new JLabel());
        panelContrato.add(btnCrearContrato);
        
        // Sección Servicio
        JPanel panelServicio = new JPanel(new GridLayout(5, 2, 5, 5));
        panelServicio.setBorder(BorderFactory.createTitledBorder("Agregar Servicio"));
        
        panelServicio.add(new JLabel("ID Servicio:"));
        txtIdServicio = new JTextField();
        panelServicio.add(txtIdServicio);
        
        panelServicio.add(new JLabel("Descripción:"));
        txtDescripcion = new JTextField();
        panelServicio.add(txtDescripcion);
        
        panelServicio.add(new JLabel("Estado:"));
        cmbEstado = new JComboBox<>(new String[]{"Pendiente", "En Progreso", "Completado"});
        panelServicio.add(cmbEstado);
        
        panelServicio.add(new JLabel("Monto:"));
        txtMonto = new JTextField();
        panelServicio.add(txtMonto);
        
        btnAgregarServicio = new JButton("Agregar Servicio");
        btnAgregarServicio.setEnabled(false);
        btnAgregarServicio.addActionListener(e -> agregarServicio());
        panelServicio.add(new JLabel());
        panelServicio.add(btnAgregarServicio);
        
        // Área de vista previa
        JPanel panelVista = new JPanel(new BorderLayout());
        panelVista.setBorder(BorderFactory.createTitledBorder("Contrato Actual"));
        txtContratoActual = new JTextArea(5, 40);
        txtContratoActual.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtContratoActual);
        panelVista.add(scroll, BorderLayout.CENTER);
        
        // Panel envío
        JPanel panelEnvio = new JPanel(new FlowLayout());
        panelEnvio.add(new JLabel("Servidor:"));
        cmbServidores = new JComboBox<>();
        cmbServidores.addItem(serverNode.getNodeName());
        panelEnvio.add(cmbServidores);
        
        btnEnviarContrato = new JButton("Enviar Contrato al Servidor");
        btnEnviarContrato.setEnabled(false);
        btnEnviarContrato.addActionListener(e -> enviarContrato());
        panelEnvio.add(btnEnviarContrato);
        
        panelFormulario.add(panelContrato);
        panelFormulario.add(Box.createVerticalStrut(10));
        panelFormulario.add(panelServicio);
        panelFormulario.add(Box.createVerticalStrut(10));
        panelFormulario.add(panelVista);
        panelFormulario.add(panelEnvio);
        
        add(panelInfo, BorderLayout.NORTH);
        add(new JScrollPane(panelFormulario), BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
    }
    
    private void crearContrato() {
        String id = txtIdContrato.getText().trim();
        String parteA = txtParteA.getText().trim();
        String parteB = txtParteB.getText().trim();
        
        if (id.isEmpty() || parteA.isEmpty() || parteB.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos del contrato");
            return;
        }
        
        contratoActual = new contratos(id, parteA, parteB);
        btnAgregarServicio.setEnabled(true);
        actualizarVistaContrato();
        JOptionPane.showMessageDialog(this, "Contrato creado. Ahora puede agregar servicios.");
    }
    
    private void agregarServicio() {
        if (contratoActual == null) return;
        
        String id = txtIdServicio.getText().trim();
        String desc = txtDescripcion.getText().trim();
        String estado = (String) cmbEstado.getSelectedItem();
        String montoStr = txtMonto.getText().trim();
        
        if (id.isEmpty() || desc.isEmpty() || montoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos del servicio");
            return;
        }
        
        try {
            double monto = Double.parseDouble(montoStr);
            contratoActual.agregarServicio(id, desc, estado, monto);
            
            txtIdServicio.setText("");
            txtDescripcion.setText("");
            txtMonto.setText("");
            
            actualizarVistaContrato();
            btnEnviarContrato.setEnabled(true);
            JOptionPane.showMessageDialog(this, "Servicio agregado al contrato");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un número válido");
        }
    }
    
    private void actualizarVistaContrato() {
        if (contratoActual == null) return;
        
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(contratoActual.getIdContrato()).append("\n");
        sb.append("Cliente: ").append(contratoActual.getParteA()).append("\n");
        sb.append("Proveedor: ").append(contratoActual.getParteB()).append("\n");
        sb.append("Valor Total: $").append(contratoActual.getValorTotal()).append("\n\n");
        sb.append("Servicios:\n");
        
        for (servicio s : contratoActual.getListaServicios()) {
            sb.append("  - ").append(s.getIdServicio()).append(": ");
            sb.append(s.getDescripcion()).append(" ($").append(s.getMontoServicio()).append(") - ");
            sb.append(s.getEstado()).append("\n");
        }
        
        txtContratoActual.setText(sb.toString());
    }
    
    private void enviarContrato() {
        if (contratoActual == null) {
            JOptionPane.showMessageDialog(this, "No hay contrato para enviar");
            return;
        }
        
        // Convertir contrato a JSON
        String jsonContrato = contratoToJSON(contratoActual);
        
        // Enviar al servidor
        boolean enviado = wallet.enviarContrato(jsonContrato);
        
        if (enviado) {
            JOptionPane.showMessageDialog(this, "Contrato enviado al servidor para minado");
            contratoActual = null;
            limpiarFormulario();
        } else {
            JOptionPane.showMessageDialog(this, "Error al enviar el contrato", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String contratoToJSON(contratos c) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"idContrato\": \"").append(c.getIdContrato()).append("\",\n");
        json.append("  \"cliente\": \"").append(c.getParteA()).append("\",\n");
        json.append("  \"proveedor\": \"").append(c.getParteB()).append("\",\n");
        json.append("  \"valorTotal\": ").append(c.getValorTotal()).append(",\n");
        json.append("  \"servicios\": [\n");
        
        for (int i = 0; i < c.getListaServicios().size(); i++) {
            servicio s = c.getListaServicios().get(i);
            json.append("    {\n");
            json.append("      \"idServicio\": \"").append(s.getIdServicio()).append("\",\n");
            json.append("      \"descripcion\": \"").append(s.getDescripcion()).append("\",\n");
            json.append("      \"estado\": \"").append(s.getEstado()).append("\",\n");
            json.append("      \"monto\": ").append(s.getMontoServicio()).append("\n");
            json.append("    }");
            if (i < c.getListaServicios().size() - 1) json.append(",");
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}");
        return json.toString();
    }
    
    private void limpiarFormulario() {
        txtIdContrato.setText("");
        txtParteA.setText("");
        txtParteB.setText("");
        txtIdServicio.setText("");
        txtDescripcion.setText("");
        txtMonto.setText("");
        txtContratoActual.setText("");
        btnAgregarServicio.setEnabled(false);
        btnEnviarContrato.setEnabled(false);
    }
    
    public void updateServers(ArrayList<NodeData> servers) {
        this.servidores = servers;
        cmbServidores.removeAllItems();
        for (NodeData server : servers) {
            cmbServidores.addItem(server.getNodeName());
        }
    }
}
