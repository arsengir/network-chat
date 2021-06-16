package com.github.arsengir;

import java.io.*;
import java.net.Socket;

public class InMessageRunnable implements Runnable {

    private final Client client;
    private BufferedReader inMessage;

    public InMessageRunnable(Client client, Socket socket) {
        this.client = client;
        try {
            inMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String line;
            while (true) {
                while ((line = inMessage.readLine()) != null) {
                    client.inMessage(line);
                }
            }
        } catch (IOException ignored) {

        }

    }
}
