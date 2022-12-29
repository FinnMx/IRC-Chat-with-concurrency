package org.nsd;

import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {

    private ServerSocket serverSocket;
    private static ArrayList<String> loadedChanelList = new ArrayList<>();


    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void initServer(){
        try {
            //THIS SHIT DONT WORK COME UP WITH A NEW WAY!!!!!!!!!!!!!!!
            Socket tmpSocket = new Socket("localhost", 2123);
            ServerThread tmpServerThread = new ServerThread(tmpSocket);
            tmpServerThread.loadChannels(loadedChanelList);
            tmpServerThread.removeServerThread();
            while(!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Client Connected");
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
            }
        } catch(IOException e){

        }
    }

    public void closeSocket(){
        try{
            if(serverSocket != null)
                serverSocket.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(2123);
            Server server = new Server(serverSocket);
            server.initServer();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}