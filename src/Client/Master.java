package Client;

import Client.Utilities.Screen;

import java.awt.*;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Master {


    public static void main(String[] args) throws SocketException, UnknownHostException {
        new MasterScreen(Toolkit.getDefaultToolkit().getScreenSize()).run();
    }
}
