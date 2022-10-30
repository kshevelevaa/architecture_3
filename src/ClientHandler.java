import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;
    public static ArrayList<String> messages = new ArrayList<>();
    public static int messagesSize = 0;
    private final Timer timer = new Timer();

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userName = bufferedReader.readLine();
            clientHandlers.add(this);
            allMessages();
            broadcastMessage("SERVER: " + userName + " has entered the chat");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String message;
        try{
        while(socket.isConnected()){
            message = bufferedReader.readLine();
            messages.add(message);
//            broadcastMessage( message);
        }}catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    public void allMessages() {
        timer.schedule(new TimerTask() {
            public void run() {
                if (messagesSize < messages.size()) {
                    for (int i = messagesSize; i < messages.size(); i++) {
                        broadcastMessage(messages.get(i));
                    }
                    messagesSize = messages.size();
                }

            }
        }, 0, 5000);
    }

    public void broadcastMessage(String messageToSent){
        for (ClientHandler clientHandler: clientHandlers){
            try{
                if (!clientHandler.userName.equals(userName)){
                    clientHandler.bufferedWriter.write(messageToSent);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + userName + " has left the chat");
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter!= null){
                bufferedWriter.close();
            }
            if (socket!=null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
