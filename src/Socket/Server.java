package Socket;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] zero) {
        ServerSocket socket;
        try {
            socket = new ServerSocket(2024);
            Thread t = new Thread(new Accepter_clients(socket));
            t.start();
            System.out.println("Le serveur est prêt !");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}