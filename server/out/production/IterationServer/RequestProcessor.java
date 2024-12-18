package production.IterationServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class RequestProcessor implements Runnable {
    Socket socket;
    IModel model;

    public RequestProcessor(Socket s) {
        this.socket = s;
        this.model = new Model();
    }

    public void run() {
        try {
            InputStream input = this.socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = this.socket.getOutputStream();
            final OutputStreamWriter writer = new OutputStreamWriter(output);
            String line = "";

            do {
                System.out.println("Waiting line...");
                line = reader.readLine();
                System.out.println("Line: " + line);
                if (line.equals("start")) {
                    this.model.startProcess(new Updatable(this) {
                        public void update(double value) {
                            try {
                                writer.write(String.valueOf(value) + "\n");
                                writer.flush();
                            } catch (IOException var4) {
                                IOException e = var4;
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            } while(!line.equals("exit"));

            writer.close();
            System.out.println("Processor is closed");
        } catch (IOException var6) {
            IOException e = var6;
            throw new RuntimeException(e);
        }
    }
}
