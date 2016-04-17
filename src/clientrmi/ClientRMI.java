/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientrmi;

import static clientrmi.ServerRMI.testBDConnection;
import common.RMI_Interface;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pedro
 */
public class ClientRMI{

    static String serverIP = "10.0.5.215";
    
    /**
     * @param args the command line arguments
     */
    private ClientRMI() {
    }

    public static void main(String[] args) {
        
        String host = (args.length < 1) ? null : args[0];
        RMI_Interface stub = null;
        
        
        ServerRMI theServer = new ServerRMI();
        theServer.startServer();
        
        
        //To connect with the RMI Server.
        try {
            Registry registry = LocateRegistry.getRegistry(serverIP, 1099);
            stub = (RMI_Interface) registry.lookup("rmi://"+serverIP+":1099/RMI_Interface");
            
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Client exception: " + e.toString());
            System.out.println("");
        }
        
        //Instance to connect with clients.
        Server_Socket s = new Server_Socket(4050, stub);
    }
}