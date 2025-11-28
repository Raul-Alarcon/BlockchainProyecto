/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author carlos
 */
public class NodeData {
    private String nodeName;
    private String IPAddress;
    private int    socketNum;
    
    public NodeData(String pnodeName, String pIPAddress, int psocketNum)
    {
        this.nodeName= pnodeName;
        this.IPAddress= pIPAddress;
        this.socketNum= psocketNum;
    }

    /**
     * @return the nodeName
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @return the IPAddress
     */
    public String getIPAddress() {
        return IPAddress;
    }

    /**
     * @return the socketNum
     */
    public int getSocketNum() {
        return socketNum;
    }
    
}
