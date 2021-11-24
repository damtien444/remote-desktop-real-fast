package Client;

import Client.Utilities.CONFIG;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class Slave {
    //    static Screen bufferedScreen;

    SendScreen sendScreen;
    public Slave(InetAddress masterAddress) {
        try {

            DatagramSocket skOutScreen = new DatagramSocket();
            sendScreen = new SendScreen(skOutScreen, masterAddress, CONFIG.PORT_UDP_SOCKET_IN_RECEIVE_SCREEN);
            sendScreen.start();

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Không thể tạo socket");
        }

    }

    public Slave(InetAddress masterAddress, int masterPort) {
        try {
            DatagramSocket skOutScreen = new DatagramSocket();
            sendScreen = new SendScreen(skOutScreen, masterAddress, masterPort);

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Không thể tạo socket");
        }

    }


    public static void main(String[] args) throws UnknownHostException {
        InetAddress masterAddress = InetAddress.getByName("localhost");
        Slave slave = new Slave(masterAddress);
    }

}
