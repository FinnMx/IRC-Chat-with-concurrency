package org.nsd;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.nsd.responses.ErrorResponse;
import org.nsd.responses.SuccessResponse;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread{

    public static ArrayList<ServerThread> serverThreads = new ArrayList<>();
    public static ArrayList<String> channelList = new ArrayList<>();
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
            JSONObject obj = (JSONObject)parser.parse(message);

            handleRequest(obj);
        }catch (ParseException e){
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    public void handleRequest(JSONObject obj) throws ParseException {
        try {
            JSONObject response = new JSONObject();
            switch (obj.get("_class").toString()) {
                case "PublishRequest":
                    response = sendMessage(obj);
                    break;
                case "OpenRequest":
                    response = openRequest(obj);
                    break;
                case "SubscribeRequest":
                    response = subscribeRequest(obj);
                default:
                    break;
            }
            //this is where logging can be done for all requests/exchanges
            System.out.println(obj.toJSONString());
            System.out.println(response.toJSONString());

            bufferedWriter.write(response.toJSONString());
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException e){
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    public JSONObject subscribeRequest(JSONObject obj) throws IOException {
        SuccessResponse success = new SuccessResponse();
        String requestedChannel = obj.get("channel").toString();
        if(requestedChannel == channel){
            bufferedWriter.write("You are already in this channel!");
            return success.toJSON();
        }
        if(searchList(channelList, requestedChannel)){
            channel = requestedChannel;
            return success.toJSON();
        }
        ErrorResponse error = new ErrorResponse("This channel doesn't exist");
        return error.toJSON();

    }

    public boolean searchList(ArrayList<String> set, String item){
        for (String current :set){
            if(current.equals(item)){
                return true;
            }
        }
        return false;
    }

    public JSONObject sendMessage(JSONObject obj) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject message = (JSONObject)parser.parse(obj.get("message").toString());
        obj.put("identity", channel);
        String userName = message.get("from").toString();
        String channel = obj.get("identity").toString();
        String body = message.get("body").toString();
        for(ServerThread serverThread : serverThreads){
            try{
                if(!serverThread.userName.equals(userName) && serverThread.channel.equals(channel)){
                    serverThread.bufferedWriter.write(userName + ": " + body);
                    serverThread.bufferedWriter.newLine();
                    serverThread.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeAll(socket, bufferedWriter, bufferedReader);
            }
        }
        SuccessResponse success = new SuccessResponse();
        return success.toJSON();
    }

    public void serverMessage(String userName, String message) throws IOException {
        for(ServerThread serverThread : serverThreads){
            try{
                if(!serverThread.userName.equals(userName) && serverThread.channel.equals(channel)){
                    serverThread.bufferedWriter.write(userName + " " + message);
                    serverThread.bufferedWriter.newLine();
                    serverThread.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeAll(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    public JSONObject openRequest(JSONObject obj){
        for(String channel: channelList){
            if(channel == obj.get("identity")) {
                ErrorResponse error = new ErrorResponse("Channel already exists/user already exists");
                return error.toJSON();
            }
        }
        channelList.add(obj.get("identity").toString());
        SuccessResponse success = new SuccessResponse();
        return success.toJSON();
    }

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

    public void unSubscribe(String channel){

    }

    public void get(int time){

    }

    public void removeServerThread() {
        try {
            serverMessage(userName, "has left!");
            serverThreads.remove(this);

        }catch(IOException e){
            closeAll(socket, bufferedWriter, bufferedReader);
        }
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
            serverMessage(userName, "has joined!");
        }catch(IOException ie)
        {
            closeAll(socket, bufferedWriter, bufferedReader);
        }

    }
}