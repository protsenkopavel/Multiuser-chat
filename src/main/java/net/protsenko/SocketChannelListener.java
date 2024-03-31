package net.protsenko;

import net.protsenko.model.Request;
import net.protsenko.service.InputParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketChannelListener extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public SocketChannelListener(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            String firstInput = in.readLine();
            Request request;
            String credentials = "";

            if (firstInput != null) {
                try {
                    request = InputParser.parseInputString(firstInput);
                    // обработка первого вызова
                    credentials = request.getCredentials();
                } catch (Exception e) {
                    logWarning(e);
                    close();
                    return;
                }
            }

            String inputLine;

            while ((inputLine = in.readLine()) != null ) {
                try {
                    request = InputParser.parseInputString(firstInput);
                    if (!request.getCredentials().equals(credentials)) {
                        // неправильные кред.
                    }
                } catch (Exception e) {
                    logWarning(e);
                }
            }

            String inputLine;


            while ((inputLine = in.readLine()) != null) {
                if (".".equals(inputLine)) {
                    out.println("bye");

                    // метод, преобразующий входящий поток байтов в джейсон (реквест)
                    // если это первое обращение к серверу, необходимо либо зарегать либо проверить в БД наличие пользователя и пароля
                    // в случае если пользака нет в БД, регистрируем
                    // в случае успеха возвращаем список подключенных пользователей (написать метод для возврата списка пользователей)
                    // если во входящем реквесте статус "сообщение", записываем его в райтер
                    // если пользак хочет закончить работу приложения, закрываем сокет

                    close();
                    break;
                }
                out.println(inputLine);
            }
        } catch (IOException e) {
            logWarning(e);
        }
    }


    private void close() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {

        }
    }

    private void logWarning(Exception e) {
        Logger.getLogger("ListenerLogger").log(Level.WARNING, e.getMessage(), e);
    }
}