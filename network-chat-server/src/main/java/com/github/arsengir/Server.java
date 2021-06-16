package com.github.arsengir;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Properties;

public class Server {
    static final int PORT = getSetting("port");
    private final ArrayList<ClientRunnable> CLIENTS = new ArrayList<>();

    public Server() {
        Socket clientSocket = null;
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Сервер запущен!");
            while (true) {
                clientSocket = serverSocket.accept();
                ClientRunnable client = new ClientRunnable(this, clientSocket);
                CLIENTS.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                log("Сервер остановлен.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void log(String msg) {
        String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        System.out.printf("[%s] %s\n", dateNow, msg);

        File log = new File("serverFile.log");
        try (FileWriter fw = new FileWriter(log, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(String.format("[%s] %s\n", dateNow, msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getSetting(String setting) {
        String rootPath;
        try {
            rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        } catch (Exception e) {
            rootPath = "";
        }
        String settingsPath = rootPath + "settings.properties";

        Properties settingProps = new Properties();
        try {
            settingProps.load(new FileInputStream(settingsPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(settingProps.getProperty(setting));
    }


    public synchronized void sendMessageToAllClients(String msg, ClientRunnable thisClient) {
        log(msg);
        for (ClientRunnable client : CLIENTS) {
            if (client != thisClient) {
                client.sendMessage(msg);
            }
        }
    }

    public void removeClient(ClientRunnable client) {
        CLIENTS.remove(client);
    }
}
