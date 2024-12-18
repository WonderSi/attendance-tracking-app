package production.IterationServer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public Main() {
    }

    public static void main(String[] args) throws IOException {
        ServerSocket s = new ServerSocket(2048);
        System.out.println("Server is started");

        while(true) {
            Socket clientSocket = s.accept();
            PrintStream var10000 = System.out;
            InetAddress var10001 = clientSocket.getInetAddress();
            var10000.println("Client is accepted: " + var10001 + ":" + clientSocket.getPort());
            Thread t = new Thread(new RequestProcessor(clientSocket));
            System.out.println("Thread is starting...");
            t.start();
        }
    }
}
