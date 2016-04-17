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
import java.text.SimpleDateFormat;
import java.util.Calendar;

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

            if (!PreviusClass.sendBD) {
                Statement stmt2 = PreviusClass.conn.createStatement();
                ResultSet rs = stmt2.executeQuery("select * from devices");

                while (rs.next()) {
                    stub.recoveryBD(rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
                }
                PreviusClass.sendBD = true;
                stub.giveMeYourBD();
            }

        } catch (RemoteException ex) {
            //Logger.getLogger(Server_Socket.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("RMI Connecting...[FAILED]");
        } catch (NotBoundException ex) {
            System.out.println("RMI NotBoundException");
            //Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            System.out.println("RMI SQLException");
            //Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
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

    public String insertPerson(String ibt, String name, String password) {

        String returnedQuery = "Cosa";

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();

            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            stmt.executeUpdate("INSERT INTO `locator`.`devices` (`id_bluetooth`, `name`, `password`) VALUES ('" + ibt + "', '" + name + "', '" + password + "')");

            System.out.println("All right");

            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnedQuery;
    }

    public boolean checkID(String idBT, String name, String password) {
        boolean trueID = false;

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices where id_bluetooth = '" + idBT + "'");

            while (rs.next()) {
                if (rs.getString(2).equals(idBT) && rs.getString(3).equals(name) && rs.getString(6).equals(password)) {
                    trueID = true;
                }
            }
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return trueID;
    }

    public boolean checkIDexist(String idBT, String name, String password) {
        boolean trueID = false;

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices where id_bluetooth = '" + idBT + "'");

            if (rs.next()) {
                trueID = true;
            }
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return trueID;
    }

    public String buscarPersona(String nombre) {
        String datosPersona = "";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices where name = '" + nombre + "'");

            while (rs.next()) {
                datosPersona = rs.getString(3) + "#" + rs.getString(4) + "#" + rs.getString(5);
            }
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }

        return datosPersona;
    }

    public String buscarTodos() {
        String datosPersonas = "";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices");
            int i = 0;
            while (rs.next()) {
                datosPersonas += rs.getString(3) + "#" + rs.getString(4) + "#" + rs.getString(5) + "#";
                i++;
            }
            datosPersonas = deleteLastChar(datosPersonas);
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }

        return datosPersonas;
    }

    public String getDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //System.out.println(sdf.format(cal.getTime()));
        return sdf.format(cal.getTime());
    }

    public String deleteLastChar(String s) {
        if (!s.isEmpty()) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }

    public String searchArea(String area) {
        String data = "";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices where lugar = '" + area + "'");

            while (rs.next()) {
                data += rs.getString(2) + "#" + rs.getString(3) + "#" + rs.getString(4) + "#" + rs.getString(5) + "#";
            }
            data = deleteLastChar(data);
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data; //id_bluetooth#name#lugar#date#id_bluetooth#name#lugar#date...
    }

    @Override
    public void run() {
        String msg = "";

        try {

            //Receive msg from the client
            msg = fromClient.readUTF();

            String[] dataSet = msg.split("#"); //ingresar#id_bluetooth#nombre#password

            if (dataSet[0].equals("ingresar")) {
                //Check if the user and password match.
                if (checkID(dataSet[1], dataSet[2], dataSet[3])) {
                    toClient.writeUTF("ingresa");
                } else {
                    toClient.writeUTF("noIngresa");
                }
            } else if (dataSet[0].equals("registrar")) { //registras#id_bluetooth#nombre#password
                //Check if the person already exists.
                if (!checkIDexist(dataSet[1], dataSet[2], dataSet[3])) {
                    insertPerson(dataSet[1], dataSet[2], dataSet[3]);
                    toClient.writeUTF("signUp");
                } else {
                    toClient.writeUTF("userExists");
                }
            } else if (dataSet[0].equals("searchPerson")) {  //searchPerson#name
                String persona = buscarPersona(dataSet[1]);
                toClient.writeUTF(persona); //notFound if user doesn't exist and id_bluetooth#name#lugar#date
            } else if (dataSet[0].equals("searchAll")) { //searchAll
                String Todos = buscarTodos();
                toClient.writeUTF(Todos); //id_bluetooth#name#lugar#date#id_bluetooth#name#lugar#date...
            } else if (dataSet[0].equals("searchArea")) { //searchArea#location
                String userInArea = searchArea(dataSet[1]);
                toClient.writeUTF(userInArea); //id_bluetooth#name#lugar#date#id_bluetooth#name#lugar#date...
            } else if (dataSet[0].equals("updateLocation")) { //updateLocation#id_bluetooth#location
//                System.out.println("Message Recived: " + msg);
                String ibt = dataSet[1];
                String lugar = dataSet[2];
                String datetime = getDate();
                //Create the query to the local database.
                if (PreviusClass.conn != null) {
                    Statement stmt = PreviusClass.conn.createStatement(); //stmt is the object to create statements.
                    stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + lugar + "',`datetime`='" + datetime + "' WHERE id_bluetooth='" + ibt + "'");
                    System.out.println("Local query performed...[OK].");
                    toClient.writeUTF("Acknowledge");
                } else {
                    Connection conni = PreviusClass.cbd.connectBD();
                    PreviusClass.conn = conni;
                    toClient.writeUTF("noDB");
                }
                if (stub != null) {
                    stub.updateRow(ibt, lugar, datetime, "pass");
                    System.out.println("External query performed...[OK]");
                } else {
                    PreviusClass.sendBD = false;
                    reconectRMI();
                }
            }

        } catch (IOException ex) {
            //Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("External query performed...[FAILED]");
            try {
                stub.sayHello(); // Function to test RMI connection.
            } catch (Exception ex1) {
                PreviusClass.sendBD = false;
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
