//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Main {
    public static void main(String[] args) throws IOException {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        ServerSocket s = new ServerSocket(2048);
        System.out.println("Server is started");
        while(true) {
            Socket clientSocket = s.accept();
            System.out.println("Client is accepted: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            Thread t = new Thread(new RequestProcessor(clientSocket));
            System.out.println("Thread is starting...");
            t.start();
        }

    }
}