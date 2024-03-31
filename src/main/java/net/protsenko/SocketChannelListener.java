package net.protsenko;

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

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (".".equals(inputLine)) {
                    out.println("bye");
                    try {
                        in.close();
                        out.close();
                        clientSocket.close();
                    } catch (IOException e) {
                    }
                    break;
                }
                out.println(inputLine);
            }
        } catch (IOException e) {
            Logger.getLogger("ListenerLogger").log(Level.WARNING, e.getMessage(), e);
        }

    }
}
