package server;

import lombok.extern.log4j.Log4j2;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class Server {
    public static void main(String[] args)
    {
        List<ServerWorker> serverWorkers = new ArrayList<>();

        try
        {
            ServerSocket serverSocket = new ServerSocket(1234);
            Socket socket;

            while (true) {
                socket = serverSocket.accept();
                serverWorkers.add(new ServerWorker(socket));
                serverWorkers.removeIf(sw -> sw.getState() == Thread.State.TERMINATED);
            }
        }
        catch (Exception exp)
        {
            log.error(exp.getMessage());
        }

    }
}
