package org.nsd;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread{

    public static ArrayList<ServerThread> serverThreads = new ArrayList<>();
    private String userName;
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    @Override
    public void run() {
        String message;

        while(socket.isConnected()){
            try{
                message = bufferedReader.readLine();
                globalMessage(message);
            }catch (IOException e){
                closeAll(socket, bufferedWriter, bufferedReader);
                break;
            }
        }
    }

    public void globalMessage(String message){
        for(ServerThread serverThread : serverThreads){
            try{
                if(!serverThread.userName.equals(userName)){
                    serverThread.bufferedWriter.write(message);
                    serverThread.bufferedWriter.newLine();
                    serverThread.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeAll(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    public void removeServerThread(){
        serverThreads.remove(this);
        globalMessage("SERVER: " + userName + " has left!");
    }

    public void closeAll(Socket socket, BufferedWriter toClient, BufferedReader fromClient){
        removeServerThread();
        try{
            if(fromClient != null && toClient != null && socket != null){
                fromClient.close();
                toClient.close();
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public ServerThread(Socket socket)
    {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userName = bufferedReader.readLine();
            serverThreads.add(this);
            globalMessage("SERVER: " + userName + " Has joined!" );
        }catch(IOException ie)
        {
            closeAll(socket, bufferedWriter, bufferedReader);
        }


    }
}