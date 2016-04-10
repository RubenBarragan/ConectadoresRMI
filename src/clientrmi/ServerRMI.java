/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientrmi;

import static clientrmi.ClientRMI.serverIP;
import common.RMI_Interface;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author EndUser
 */
public class ServerRMI implements RMI_Interface {

    static String externalIP = "10.0.5.232";
    static String localIP = "10.0.5.215";
    static int localPort = 1099;

    /**
     * @param args the command line arguments
     */
    public ServerRMI() {
        super();
    }

    public String sayHello() {
        return "Hello, a wii!";
    }

    public String selectAll() {

        String returnedQuery = "";

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();

            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices");

            while (rs.next()) {
                returnedQuery += rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5);
                //System.out.println(rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3) + " " + rs.getInt(4));
            }

            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnedQuery;
    }

    //Only local computing.
    public String selectRow(String id) {

        String returnedQuery = "";

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();

            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices where id_bluetooth = '" + id + "'");

            while (rs.next()) {
                returnedQuery += rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5);
            }

            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnedQuery;
    }

    public String insertRow(String ibt, String name, String lugar, String datetime) {

        String returnedQuery = "Cosa";

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();

            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            stmt.executeUpdate("INSERT INTO `locator`.`devices` (`id_bluetooth`, `name`, `lugar`, `datetime`) VALUES ('" + ibt + "', '" + name + "', '" + lugar + "', '" + datetime + "')");

            System.out.println("All right");

            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnedQuery;
    }

    public String updateRow(String ibt, String lugar, String datetime) {

        String returnedQuery = "";

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();

            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + lugar + "',`datetime`='" + datetime + "' WHERE id_bluetooth='" + ibt + "'");

            System.out.println("All right");

//            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnedQuery;
    }

    public void recoveryBD(String ibt, String name, String lugar, String datetime) {
        if (exists_idBT(ibt)) {
            updateRow(ibt, lugar, datetime);
        } else {
            insertRow(ibt, name, lugar, datetime);
        }
    }

    //Only local computing.
    public boolean exists_idBT(String id) {
        String s = selectRow(id);

        if (s.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    //Only local computing.
    public static void testBDConnection() {
        ConnectBD cbd = new ConnectBD();
        Connection con = null;

        con = cbd.connectBD();
        while (con == null) {
            con = cbd.connectBD();
        }

    }

    public boolean isEmpty() {
        boolean _isEmpty = false;

        ConnectBD cbd = new ConnectBD();
        Connection con = null;
        try {
            con = cbd.connectBD();

            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("SELECT * FROM `devices`");

            if (!rs.isBeforeFirst()) {
                System.out.println("No data");
                _isEmpty = true;
            }

            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);

        }

        return _isEmpty;
    }

    public void giveMeYourBD() {
        ResultSet rs = null;

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();

            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            rs = stmt.executeQuery("select * from devices");

            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(externalIP, 1099);
                RMI_Interface stub = (RMI_Interface) registry.lookup("rmi://" + externalIP + ":1099/RMI_Interface");

                while (rs.next()) {
                    stub.recoveryBD(rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
                }

            } catch (RemoteException ex) {
                Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NotBoundException ex) {
                Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
            }

            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void startServer() {
        // TODO code application logic here

        try {
            testBDConnection();

            //ServerRMI obj = new ServerRMI();
            RMI_Interface stub = (RMI_Interface) UnicastRemoteObject.exportObject(this, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("rmi://" + localIP + ":1099/RMI_Interface", stub);

            System.err.println("Server ready");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
