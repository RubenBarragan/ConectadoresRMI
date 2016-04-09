/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientrmi;

import common.RMI_Interface;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pedro
 */
public class RunnableListener implements Runnable {

    private Thread t;
    private String threadName;

    RunnableListener(String name) {
        threadName = name;
        System.out.println("Creating " + threadName);
    }

    public void run() {
        System.out.println("Running " + threadName);
        //try {
            //conectarme al otro servidor por rmi

            //crear socketserver para comunicacion con los demas celulares
            //ServerSocket serverSocket = new ServerSocket(1515);
            //Socket clientSocket = serverSocket.accept();
            //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            //DataOutputStream outt = new DataOutputStream(clientSocket.getOutputStream());
            //DataInputStream inn = new DataInputStream(clientSocket.getInputStream());
            //BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //System.out.println("client conected");

            String inputLine = "";
            while (true) {
                //while ((inputLine = inn.readUTF()) != null) {
                System.out.println("entre");
                //if (inn.available() > 0) {
                //    inputLine = inn.readUTF();
                //    System.out.println("recibed message: " + inputLine);
                //    outt.writeUTF(inputLine);
                //}

                //stub.insertRow("bt123456789", "pedro", "biblio", "2016-04-12 00:00:00");
                // aqui mando el update ala base de datos y/o devuelvo la ubicacion de alguien
                //out.println(inputLine);
                //outt.writeUTF(inputLine);
                //}
            //}
        //} catch (IOException ex) {
        //    Logger.getLogger(RunnableListener.class.getName()).log(Level.SEVERE, null, ex);
        //} catch (NotBoundException ex) {
        //    Logger.getLogger(RunnableListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("Thread " + threadName + " exiting.");
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

}
