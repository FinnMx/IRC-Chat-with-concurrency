package org.nsd;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.nsd.requests.*;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Client{
    private String userName;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Client(Socket socket, String userName){
        try {
            this.socket = socket;
            this.userName = userName;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
            while(!socket.isClosed()){
                String message = scanner.nextLine();
                bufferedWriter.write(handleInput(message));
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String handleInput(String message){
        if(message.charAt(0) == '/'){
            return handleCommands(message);
        }
        else{
            Message request = new Message(userName,message);
            PublishRequest pubReq = new PublishRequest("", request);
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
        //Using a switch case instead of a hashmap as we only have a few commands...
        switch (command){
            case "/help":
                DefaultRequest help = new DefaultRequest("Help");
                message = help.toJSONString();
                break;
            case "/join":
                SubscribeRequest subReq = new SubscribeRequest(userName, instruction);
                message = subReq.toJSONString();
                break;
            case "/leave":
                UnsubscribeRequest unsubReq = new UnsubscribeRequest(userName, "general");
                message = unsubReq.toJSONString();
                break;
            case "/quit":
                DefaultRequest quit = new DefaultRequest("Quit");
                message = quit.toJSONString();
                break;
            case "/create":
                OpenRequest openReq = new OpenRequest(instruction);
                message = openReq.toJSONString();
                break;
            case "/get":
                GetRequest getReq = new GetRequest(userName, getRequestedTime(Integer.parseInt(instruction)));
                message = getReq.toJSONString();
                break;
            case "/viewchannels":
                DefaultRequest viewChannels = new DefaultRequest("ViewChannels");
                message = viewChannels.toJSONString();
                break;
            default:
                DefaultRequest invalid = new DefaultRequest("Invalid");
                message = invalid.toJSONString();
                break;
        }
        return message;
    }

    public String getRequestedTime(int mins){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime time = LocalDateTime.now().minusMinutes(mins);
        return dtf.format(time);
    }


    public void recieveMessage(){
        new Thread(() -> {
            String response = null;

            while(!socket.isClosed()){
                try{
                    response = bufferedReader.readLine();
                    JSONParser parser = new JSONParser();
                    JSONObject obj = (JSONObject)parser.parse(response);
                    handleResponse(obj);
                }catch(ParseException e){
                    System.out.println(response);
                }catch (IOException | NullPointerException i ){
                    closeAll(socket, bufferedWriter, bufferedReader);
                }
            }
            System.exit(0); //for some reason the program terminates but lingers here for no reason
                                   //I can't figure out why this is for the life of me.
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
        switch (response.get("_class").toString()) {
            case "ErrorResponse" -> System.out.println(response.get("error"));
            case "Quit" -> closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String userName;
        System.out.println("--------------------------------------------------\n"+
                           "         NSD IRC-Chat by finn moorhouse\n" +
                           "Type /help once your in to view a list of commands\n"+
                            "--------------------------------------------------");
        do {
            System.out.println("Enter your username (must be >3 characters): ");
            userName = scanner.nextLine();
        }while (userName.length() <= 3);
        Socket socket = new Socket("localhost", 2123);
        Client client = new Client(socket, userName);

        client.recieveMessage();
        client.sendMessage();
    }
}