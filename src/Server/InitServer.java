package Server;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InitServer {

    public static void main(String[] args) {
        new InitServer();
    }

    static Map<String, XulyClient> clients;

    InitServer() {

        clients = new HashMap<>();

        try {
            ServerSocket server = new ServerSocket(34567);
            ServerSocket connectionServer = new ServerSocket(34568);

            while (true) {

                // cấp thông tin id và pass cho client
                Socket soc = server.accept();
                Socket connectSoc = connectionServer.accept();


                String id = randomNumberStrings(8);
                while (clients.containsKey(id)) {
                    id = randomNumberStrings(8);
                }

                // tạo một socketport cho user;
                DatagramSocket dgSocket = null;
                int serverUDPSoc = getRandomNumberUsingInts(10000, 65000);
                boolean not_ok = true;
                while (not_ok){
                    try {
                        dgSocket = new DatagramSocket(serverUDPSoc);
                        not_ok = false;
                    } catch (SocketException e){
                        not_ok = true;
                    }
                }

                XulyClient client = new XulyClient(soc,connectSoc, dgSocket, id, randomStrings(8), soc.getInetAddress(), soc.getPort(), serverUDPSoc);
                clients.put(id, client);

                String res = client.dis.readUTF();
                String[] token = res.trim().split(":");

                if (token[0].equals("REQUEST")) {
                    client.dos.writeUTF(id + "~~" + client.pass);
                    client.setScreen(Integer.parseInt(token[1]), Integer.parseInt(token[2]));
                }

                client.start();
            }

        } catch (Exception e) {

        }
    }

    public int getRandomNumberUsingInts(int min, int max) {
        Random random = new Random();
        return random.ints(min, max)
                .findFirst()
                .getAsInt();
    }

    public static String randomStrings(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

//        System.out.println(generatedString);
        return generatedString;
    }

    public static String randomNumberStrings(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 57; // letter 'z'
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

//        System.out.println(generatedString);
        return generatedString;
    }
}

class XulyClient extends Thread {
    // cổng mà server xài để giao tiếp với client
    DatagramSocket dgSocket;
    Socket soc;
    Socket conSoc;
    DataInputStream dis;
    DataOutputStream dos;
    DataInputStream conDis;
    DataOutputStream conDos;
    Dimension screen;
    String id;
    String pass;
    InetAddress publicUDPAddress;
    int publicUDPPort;
    int serverUDPport;
    Boolean is_connected;
    Boolean is_busy;

    public XulyClient(Socket soc, Socket conSoc,DatagramSocket dgSocket, String id, String pass, InetAddress publicUDPAddress, int publicUDPPort, int serverUDPport) {
        this.soc = soc;
        this.conSoc = conSoc;
        this.id = id;
        this.pass = pass;
        this.dgSocket = dgSocket;
        this.publicUDPAddress = publicUDPAddress;
        this.publicUDPPort = publicUDPPort;
        this.is_busy = false;
        this.serverUDPport = serverUDPport;

        try {
            this.dis = new DataInputStream(soc.getInputStream());
            this.dos = new DataOutputStream(soc.getOutputStream());
            this.conDis = new DataInputStream(conSoc.getInputStream());
            this.conDos = new DataOutputStream(conSoc.getOutputStream());

            is_connected = true;
        } catch (Exception e) {

        }
    }

    public void setScreen(int w, int h){
        this.screen = new Dimension(w, h);
    }

    public void acceptIncomingConnection(String id, String pass) {


        try {
            if (InitServer.clients.containsKey(id)) {
                XulyClient partner = InitServer.clients.get(id);

                if (partner.pass.equals(pass)) {
                    if (! partner.is_busy) {

                        // trao đổi UDP
                        this.dos.writeUTF("SEND USING UDP IN-SOCKET to:"+ this.serverUDPport);


                        // NHẬN UDP-in của bên master
                        byte[] buf = new byte[1000];
                        DatagramPacket reP = new DatagramPacket(buf, buf.length);
                        boolean accepted = false;
                        while (! accepted) {
                            this.dgSocket.receive(reP);
                            String udpMsg = new String(reP.getData());
                            this.publicUDPAddress = this.soc.getInetAddress();
                            this.publicUDPPort = reP.getPort();
                            if (udpMsg.trim().equals("OKE")) {
                                this.dos.writeUTF("EXCHANGE-OKE");
                                System.out.println("NHÂN UDP thành công");
                                accepted = true;
                            }
                        }
                        partner.is_busy = true;
                        this.is_busy = true;
                        System.out.println(this.publicUDPAddress+":"+this.publicUDPPort);
                        // chuẩn bị màn hình nhận

                        this.dos.writeUTF("PREPARERECEIVE:"+partner.screen.width+":"+partner.screen.height);

                        // thông báo incoming connection cho partner

                        try {
                            partner.conDos.writeUTF("SEND-SCREEN-TO:"+this.publicUDPAddress.getHostAddress()+":"+this.publicUDPPort);
                            System.out.println("Thông báo partner thành công");
                        } catch (IOException e){
                            e.printStackTrace();
                            System.out.println("Partner disconnect");
                        }

                    } else {
                        this.dos.writeUTF("PARTNER BUSY!");
                    }
                } else {
                    this.dos.writeUTF("WRONG PASSWORD");
                }


            }
        } catch (IOException e) {
            System.out.println("User " + this.id + " disconnected!");
            is_connected = false;
            InitServer.clients.remove(this.id);
            System.out.println(InitServer.clients.size());

        }

        // kiểm tra đối tác rảnh bận


    }

    @Override
    public void run() {
        try {
            while (true) {

                String command = this.dis.readUTF();
                if (command.trim().contains("CONNECT TO:")){
                    String[] token = command.split(":");
                    String id = token[1];
                    String pass = token[2];
                    System.out.println("ok:"+id+pass);


                    acceptIncomingConnection(id, pass);
                } else if (command.trim().equals("ACK")){

                }
            }
        } catch (IOException e) {
            System.out.println("User " + id + " disconnected!");
            is_connected = false;
            InitServer.clients.remove(id);
            System.out.println(InitServer.clients.size());
        }
    }


}