package Sockets;

import java.net.Socket;

public interface MultiClientServer {
    void assignClient(Socket connection);
    void closeClient(int connectionId);
}
