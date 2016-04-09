/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientrmi;

import java.sql.*;

/**
 *
 * @author Ruben
 */
public class ConnectBD {

    Connection connectBD(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/locator", "root", "");
            System.out.println("se conecto a DB");
            return con;

        } catch (ClassNotFoundException | SQLException e) {
            //System.out.println(e);
            System.out.println("DBs connecting...[FAILED]");
            
            return null;
        }
    }
}
