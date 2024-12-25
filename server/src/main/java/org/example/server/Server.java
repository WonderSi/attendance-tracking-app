package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.*;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private static final int PORT = 2048;


    public static void main(String[] args) {
        setupLogger();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Сервер запущен на порту " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Новый клиент подключен: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка в работе сервера", e);
        }

    }
    private static void setupLogger() {
        try {
            //удаление стандартных обработчиков
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            //создание обработчика для консоли
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new SimpleFormatter());

            //создание обработчика для файла
            FileHandler fileHandler = new FileHandler("server.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

            //добавление обработчиков к логгеру
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
