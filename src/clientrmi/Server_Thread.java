/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientrmi;

import common.RMI_Interface;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;

/**
 *
 * @author Pedro
 */
public class Server_Thread extends Thread {

    private Socket theClient;
    private DataOutputStream toClient;
    private DataInputStream fromClient;
    private RMI_Interface stub;
    private Connection conn;
    private String serverIP;
    private Server_Socket PreviusClass;

    public Server_Thread(Socket _theClient, RMI_Interface _stub, Connection _conn, String _address, Server_Socket a) {
        this.theClient = _theClient;
        this.stub = _stub;
        this.conn = _conn;
        this.serverIP = _address;
        this.PreviusClass = a;

        try {
            toClient = new DataOutputStream(_theClient.getOutputStream());
            fromClient = new DataInputStream(_theClient.getInputStream());
        } catch (IOException ex) {
            System.out.println("Constructor Server thread");
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void reconectRMI() {
        try {
            //Reconnect RMI.
            Registry registry = LocateRegistry.getRegistry(serverIP, 1099);
            stub = (RMI_Interface) registry.lookup("rmi://" + serverIP + ":1099/RMI_Interface");

            System.out.println("RMI Connection established...[OK]");
            PreviusClass.stub = stub;

            if (stub.isEmpty()) {
                Statement stmt2 = PreviusClass.conn.createStatement();
                ResultSet rs = stmt2.executeQuery("select * from devices");

                while (rs.next()) {
                    stub.recoveryBD(rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
                }
                stub.giveMeYourBD();
            }

        } catch (RemoteException ex) {
            Logger.getLogger(Server_Socket.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("RMI Connecting...[FAILED]");
        } catch (NotBoundException ex) {
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void disconnectClient() {
        try {
            System.out.println("closed socket: " + theClient.toString());
            theClient.close();
        } catch (IOException ex) {
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String msg = "";

        try {
            //Receive msg from the client
            msg = fromClient.readUTF();
            System.out.println("Message Recived: " + msg);
            String datetime = "2016-04-01 23:55:20";
            String ibt = "bt123456789";
            //Create the query to the local database.
            if (PreviusClass.conn != null) {
                Statement stmt = PreviusClass.conn.createStatement(); //stmt is the object to create statements.
                stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + msg + "',`datetime`='" + datetime + "' WHERE id_bluetooth='" + ibt + "'");
                System.out.println("Local query performed...[OK].");
                toClient.writeUTF("Acknowledge");
            } else {
                Connection conni = PreviusClass.cbd.connectBD();
                PreviusClass.conn = conni;
                toClient.writeUTF("noDB");
            }

            if (stub != null) {
                stub.updateRow(ibt, msg, datetime);
                System.out.println("External query performed...[OK]");
            } else {
                reconectRMI();
            }

        } catch (IOException ex) {
            //Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("External query performed...[FAILED]");

            try {
                stub.sayHello(); // Function to test RMI connection.
            } catch (Exception ex1) {
                reconectRMI();
            }

        } catch (SQLException ex) {
            //no DB connection
            try {
                System.out.println("DB connection ... [FAILED]");
                toClient.writeUTF("noBD");

                Connection conn = PreviusClass.cbd.connectBD();
                PreviusClass.conn = conn;

            } catch (IOException ex1) {
                System.out.println("No contestavion al cliente ");
                Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }
        disconnectClient();
    }
}
