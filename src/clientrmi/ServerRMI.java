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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/**
 *
 * @author EndUser
 */
public class ServerRMI implements RMI_Interface {

    static String externalIP = "10.0.5.190";
    static String localIP = "10.0.5.215";
    static int localPort = 1099;

    /**
     * @param args the command line arguments
     */
    public ServerRMI() {
        super();
    }

    public String sayHello() {
        return "Connection RMI established...[OK]";
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
    public int selectRow(String id, String otherDate) {
        // 0 = no existe 1 = si existe y hacer cambio 2= si existe y no hacer cambio
        int returnedQuery = 0;
        String theDate = "";

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices where id_bluetooth = '" + id + "'");

            while (rs.next()) {
                theDate = rs.getString(5);
                returnedQuery = compareDates(theDate, otherDate);
            }

            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnedQuery;
    }

    public int compareDates(String theDate, String otherDate) {
        int result = -1;
        Timestamp timestamp2 = null;
        Timestamp timestamp = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date parsedDate = dateFormat.parse(theDate);
            timestamp = new java.sql.Timestamp(parsedDate.getTime());

            Date parsedDate2 = dateFormat.parse(otherDate);
            timestamp2 = new java.sql.Timestamp(parsedDate2.getTime());

            if (timestamp.before(timestamp2)) {
                result = 1;
            } else {
                result = 2;
            }

        } catch (Exception e) {//this generic but you can control another types of exception
            System.out.println(e.toString());
        }
        return result;
    }

    public String insertRow(String ibt, String name, String lugar, String datetime, String pass) {

        String returnedQuery = "Cosa";

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();

            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            stmt.executeUpdate("INSERT INTO `locator`.`devices` (`id_bluetooth`, `name`, `lugar`, `datetime`, `password`) VALUES ('" + ibt + "', '" + name + "', '" + lugar + "', '" + datetime + "','" + pass + "')");

            System.out.println("All right");

            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnedQuery;
    }

    public String updateRow(String ibt, String lugar, String datetime, String pass) {

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

    public void recoveryBD(String ibt, String name, String lugar, String datetime, String pass) {
        int result = exists_idBT(ibt, datetime);
        if (result == 1) {
            updateRow(ibt, lugar, datetime, pass);
        } else if (result == 0) {
            insertRow(ibt, name, lugar, datetime, pass);
        }
    }

    //Only local computing.
    public int exists_idBT(String id, String otherDate) {
        int s = selectRow(id, otherDate);
        return s;
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

    public RMI_Interface connectToServer(String externalip, int port) {
        RMI_Interface stub = null;
        try {
            Registry registry;
            registry = LocateRegistry.getRegistry(externalip, 1099);
            stub = (RMI_Interface) registry.lookup("rmi://" + externalip + ":1099/RMI_Interface");

        } catch (RemoteException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stub;
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
            
            RMI_Interface stub = connectToServer(externalIP, 1099);
            while (rs.next()) {
                stub.recoveryBD(rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
            }
            
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void startServer() {
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
