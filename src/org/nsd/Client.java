package org.nsd;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.nsd.requests.*;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client{
    private String userName;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private String currentChannel;

    public Client(Socket socket, String userName){
        try {
            this.socket = socket;
            this.userName = userName;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.currentChannel = "general";
        }catch (IOException e){
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    public void sendMessage(){
        try{
            OpenRequest openReq = new OpenRequest(userName);
            bufferedWriter.write(userName);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.write(openReq.toJSONString());
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String message = scanner.nextLine();
                bufferedWriter.write(handleInput(message));
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }catch (IOException e){
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    public String handleInput(String message){
        if(message.charAt(0) == '/'){
            return handleCommands(message);
        }
        else{
            Message request = new Message(userName,message);
            PublishRequest pubReq = new PublishRequest(currentChannel, request);
            return pubReq.toJSONString();
        }

    }

    public String handleCommands(String message){
        int i = message.indexOf(' ');
        String command = message;
        String instruction = "";
        if(i != -1) {
            command = message.substring(0, i);
            instruction = message.substring(i + 1);
        }
        switch (command){
            case "/help":
                break;
            case "/join":
                SubscribeRequest subReq = new SubscribeRequest(userName, instruction);
                message = subReq.toJSONString();
                break;
            case "/unsubscribe":
                UnsubscribeRequest unsubReq = new UnsubscribeRequest(userName, instruction);
                message = unsubReq.toJSONString();
                break;
            case "/create":
                OpenRequest openReq = new OpenRequest(userName);
                message = openReq.toJSONString();
            case "/get":
                GetRequest getReq = new GetRequest(userName, Integer.parseInt(instruction));
                message = getReq.toJSONString();
                break;
            default:
                System.out.println("invalid command...");
        }
        return message;
    }

    public void recieveMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = null;

                while(socket.isConnected()){
                    try{
                        response = bufferedReader.readLine();

                        JSONParser parser = new JSONParser();
                        JSONObject obj = (JSONObject)parser.parse(response);
                        handleResponse(obj);
                    }catch(ParseException e){
                        System.out.println(response);
                    }catch (IOException i){
                        closeAll(socket, bufferedWriter, bufferedReader);
                    }
                }
            }
        }).start();

    }

    public void closeAll(Socket socket, BufferedWriter toClient, BufferedReader fromClient){
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

    public void handleResponse(JSONObject response) throws ParseException {
        switch(response.get("_class").toString()){
            case "SuccessResponse":
                //could print out messages here, would make more sense
                break;
            default:

        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String userName = scanner.nextLine();

        Socket socket = new Socket("localhost", 2123);
        Client client = new Client(socket, userName);

        client.recieveMessage();

        client.sendMessage();
    }
}