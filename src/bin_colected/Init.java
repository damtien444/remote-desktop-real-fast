package bin_colected;


import javax.swing.*;
import java.net.Socket;

public class Init {
    static String port = "10000";

    public static void main(String[] args) {
        String ip = JOptionPane.showInputDialog("Please enter the server ip");
        new Init().initialize(ip, Integer.parseInt(port));
    }

    public void initialize(String ip, int port){
        try {
            Socket socket = new Socket(ip, port);
            System.out.println("connecting to server");
            Authentication frame_au = new Authentication(socket);
            frame_au.setSize(300,80);
            frame_au.setLocation(500,300);
            frame_au.setVisible(true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
