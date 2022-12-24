package org.nsd;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread{

    public static ArrayList<ServerThread> serverThreads = new ArrayList<>();
    private String userName;
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    private String channel;

    @Override
    public void run() {
        String message;

        while(socket.isConnected()){
            try{
                message = bufferedReader.readLine();
                handleInput(message);
            }catch (IOException e){
                closeAll(socket, bufferedWriter, bufferedReader);
                break;
            }
        }
    }

    public void handleInput(String message){
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(message);
        }catch (ParseException e){
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }
    /*
    public void handleCommands(String message){
        int i = message.indexOf(' ');
        String command = message;
        String instruction = "";
        if(i != -1) {
            command = message.substring(0, i);
            instruction = message.substring(i + 1);
        }
        switch (command){
            case "/help":
                help();
                break;
            case "/subscribe":
                subscribe(instruction);
                break;
            case "/unsubscribe":
                unSubscribe(instruction);
                 break;
            case "/get":
                get(Integer.parseInt(instruction));
                break;
            default:
                System.out.println("invalid command...");
        }
    }

     */
    public void help() {
        try {
            bufferedWriter.write("\nHeres a list of commands:\n" +
                    "- /help (Displays all commands)\n" +
                    "- /subscribe <channel> (subscribes you to a channel/Joins a channel)\n" +
                    "- /unsubscribe <channel> (unsubscribes you from a channel)\n" +
                    "- /get <timestamp> (returns all messages since the timestamp which is in seconds.)\n");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch(IOException e){
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    public void subscribe(String channel){
        this.channel = channel;
        sendMessage("SERVER: " + userName + " Has joined!" );
    }

    public void unSubscribe(String channel){

    }

    public void get(int time){

    }

    public void sendMessage(String message){
        for(ServerThread serverThread : serverThreads){
            try{
                if(!serverThread.userName.equals(userName) && serverThread.channel.equals(channel)){
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
        sendMessage("SERVER: " + userName + " has left!");
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
            this.channel = userName;
            sendMessage("SERVER: " + userName + " Has joined!" );
        }catch(IOException ie)
        {
            closeAll(socket, bufferedWriter, bufferedReader);
        }


    }
}