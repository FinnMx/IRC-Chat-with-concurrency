package org.nsd;

import java.io.*;
import java.net.*;
import java.io.*;
import java.net.Socket;
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
            bufferedWriter.write(userName);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String message = scanner.nextLine();
                bufferedWriter.write(userName + ": " + message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }catch (IOException e){
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    public void recieveMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;

                while(socket.isConnected()){
                    try{
                        message = bufferedReader.readLine();
                        System.out.println(message);
                    }catch(IOException e){
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