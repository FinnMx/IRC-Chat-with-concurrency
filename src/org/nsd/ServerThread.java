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
    private static Logger logger;

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

    public void writeMessage(String message){
        try{
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException e){
            closeAll(socket, bufferedWriter, bufferedReader);
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
                    break;
                case "Help":
                    response = help();
                    break;
                case "ViewChannels":
                    response = viewChannels();
                    break;
                default:
                    response = invalid();
                    break;
            }
            //this is where logging can be done for all requests/exchanges
            System.out.println(obj.toJSONString());
            System.out.println(response.toJSONString());

            writeMessage(response.toJSONString());
        }catch (IOException e){
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    public JSONObject viewChannels(){
        for (String x: channelList){
            writeMessage("- " + x);
        }
        SuccessResponse success = new SuccessResponse();
        return success.toJSON();
    }

    public JSONObject invalid(){
        ErrorResponse error = new ErrorResponse("Invalid command, type /help to view a list of commands");
        return error.toJSON();
    }

    public JSONObject help() {
            writeMessage("""
                    Here's a list of commands:
                    - /help (Displays all commands)
                    - /create <channel> (create a new channel)
                    - /join <channel> (subscribes you to a channel/Joins a channel)
                    - /leave (disconnects you, sends you back to general)
                    - /viewchannels (displays a list of all channels)
                    - /get <timestamp> (returns all messages since the timestamp which is in seconds.)""");
        SuccessResponse success = new SuccessResponse();
        return success.toJSON();
    }

    public void logMessageToDB(String channel, String message){
        Logger logger = new Logger("log.db");
        logger.write(channel, message);
        logger.close();
    }

    public JSONObject subscribeRequest(JSONObject obj) throws IOException {
        SuccessResponse success = new SuccessResponse();
        String requestedChannel = obj.get("channel").toString();
        if(requestedChannel.equals(channel)){
            writeMessage("You are already in this channel!");
            return success.toJSON();
        }
        if(searchList(channelList, requestedChannel)){
            channel = requestedChannel;
            serverMessage(obj.get("identity").toString(), "has joined!");
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
                if(!serverThread.userName.equals(userName) && serverThread.channel.equals(channel)){
                    serverThread.writeMessage(userName + ": " + body);
                }
        }
        logMessageToDB(channel, userName + ": " + body);
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
        writeMessage("Channel " + obj.get("identity") + " has been created!");
        SuccessResponse success = new SuccessResponse();
        if(!(obj.get("identity") == userName)) {
            Logger logger = new Logger("log.db");
            logger.writeChannel(obj.get("identity").toString());
            logger.close();
        }
        return success.toJSON();
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
                channelList.remove(userName);
                fromClient.close();
                toClient.close();
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void reloadChannels(){
        Logger logger = new Logger("log.db");
        channelList = logger.loadAllChannels();
        logger.close();
    }

    public void reloadMessages(){
        Logger logger = new Logger("log.db");
        String chat = logger.load(channel);
        if(chat != null)
            writeMessage(chat);

        logger.close();
    }

    public ServerThread(Socket socket)
    {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userName = bufferedReader.readLine();
            serverThreads.add(this);
            logger = new Logger("log.db");
            reloadChannels();
            this.channel = "general";
            reloadMessages();
            serverMessage(userName, "has joined!");
        }catch(IOException ie)
        {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }
}