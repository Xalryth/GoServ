package Sockets;

import java.util.Random;

public interface ClientIdGenerator {
    Random random = new Random();
    Integer generateClientId();
}
