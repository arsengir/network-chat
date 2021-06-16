package com.github.arsengir;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;

public class Client {
    static final int PORT = Integer.parseInt(getSetting("port"));
    static final String HOST = getSetting("host");

    public Client() throws IOException {
        String name;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите имя: ");
        name = scanner.nextLine();

        Socket clientSocket = new Socket(HOST, PORT);

        // Запустим отдельный поток который будет смотреть входящие сообщения
        new Thread(new InMessageRunnable(this, clientSocket)).start();

        try (PrintWriter outMessage = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true)) {
            String line;
            while (true) {
                line = scanner.nextLine();
                if ("/exit".equals(line)) {
                    outMessage.println(line);
                    break;
                }
                outMessage.printf("%s: %s\n", name, line);
                log(String.format("%s: %s", name, line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
            clientSocket.close();
        }
    }

    public void inMessage(String msg) {
        System.out.println(msg);
        log(msg);
    }

    private static String getSetting(String setting) {
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
        return settingProps.getProperty(setting);
    }

    private void log(String msg) {
        String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

        File log = new File("clientFile.log");
        try (FileWriter fw = new FileWriter(log, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(String.format("[%s] %s\n", dateNow, msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
