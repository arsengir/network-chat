package com.github.arsengir;

import java.io.*;
import java.net.Socket;

public class ClientRunnable implements Runnable {

    private final Server server;
    private static int clientsCount = 0;

    // исходящее сообщение
    private PrintWriter outMessage;
    // входящее сообщение
    private BufferedReader inMessage;

    public ClientRunnable(Server server, Socket socket) {
        this.server = server;
        clientsCount++;
        try {
            outMessage = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            inMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        server.sendMessageToAllClients("Новый участник вошел в чат. Участников в чате " + clientsCount, null);
        try {
            String line;
            mainWhile:
            while (true) {
                while ((line = inMessage.readLine()) != null) {
                    if ("/exit".equals(line)) {
                        break mainWhile;
                    }
                    server.sendMessageToAllClients(line, this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.close();
        }
    }

    public void sendMessage(String msg) {
        outMessage.println(msg);
    }

    public void close() {
        server.removeClient(this);
        clientsCount--;
        server.sendMessageToAllClients("Участник вышел из чата. Участников в чате " + clientsCount, this);
    }
}
