import java.io.*;
import java.net.Socket;

public class RequestProcessor implements Runnable{
    Socket socket;
    IModel model;

    public RequestProcessor(Socket s) {
        socket = s;
        model = new Model();
    }


    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(output);

            String line = "";

            while (true) {
                System.out.println("Waiting line...");
                line = reader.readLine();
                System.out.println("Line: " + line);

                if(line.equals("start")) {
                    model.startProcess(new Updatable() {
                        @Override
                        public void update(double value) {
                            try {
                                writer.write(String.valueOf(value) + "\n");
                                writer.flush();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
                if(line.equals("exit")) break;


            }
            writer.close();
            System.out.println("Processor is closed");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
