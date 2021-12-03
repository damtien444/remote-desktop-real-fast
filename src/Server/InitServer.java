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

    static Map<String, XulyClient> clients;

    InitServer() {

        clients = new HashMap<>();

        try {
            ServerSocket server = new ServerSocket(34567);
            ServerSocket connectionServer = new ServerSocket(34568);
//            ServerSocket punchTCPServer = new ServerSocket(34569);
            DatagramSocket dgSocket = new DatagramSocket(40000);

            while (true) {

                // cấp thông tin id và pass cho client
                Socket soc = server.accept();
                Socket connectSoc = connectionServer.accept();
//                Socket tcpPunchSoc = punchTCPServer.accept();


                String id = randomNumberStrings(8);
                while (clients.containsKey(id)) {
                    id = randomNumberStrings(8);
                }

                // tạo một socketport cho user;

//                int serverUDPSoc = getRandomNumberUsingInts(10000, 65000);
//                boolean not_ok = true;
//                while (not_ok) {
//                    try {
//                        dgSocket = new DatagramSocket(serverUDPSoc);
//                        not_ok = false;
//                    } catch (SocketException e) {
//                        not_ok = true;
//                    }
//                }

                XulyClient
                        client =
                        new XulyClient(soc, connectSoc, dgSocket, id, randomStrings(8), soc.getInetAddress(), soc.getPort(), 40000);

                clients.put(id, client);



                client.start();
            }

        } catch (Exception e) {

        }
    }

    public static void main(String[] args) {
        new InitServer();
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

    public int getRandomNumberUsingInts(int min, int max) {
        Random random = new Random();
        return random.ints(min, max)
                .findFirst()
                .getAsInt();
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

    int[] skInSenderFilePort = new int[2];
    int[] skInReceiverFilePort = new int[2];
    int[] skInChatPort  = new int[2];
    int[] skInSlaveKeyAndMousePort = new int[2];

    String partnerID;

//    ServerSocket punchTCPServer;
    Socket tcpPunchSoc;
    DataInputStream disTCPpunch;
    DataOutputStream dosTCPpunch;

    String IP;
    int port_local;
    int port;

    String local_address;
    int local_port;



    public XulyClient(Socket soc, Socket conSoc, DatagramSocket dgSocket, String id, String pass, InetAddress publicUDPAddress, int publicUDPPort, int serverUDPport) {
        this.soc = soc;
        this.conSoc = conSoc;
        this.id = id;
        this.pass = pass;
        this.dgSocket = dgSocket;
        this.publicUDPAddress = publicUDPAddress;
        this.publicUDPPort = publicUDPPort;
        this.is_busy = false;
        this.serverUDPport = serverUDPport;
//        this.punchTCPServer = punchTCPServer;

//        this.tcpPunchSoc = tcpPunchSoc;
//


        try {
//            this.tcpPunchSoc = punchTCPServer.accept();
//            this.IP = ((InetSocketAddress) tcpPunchSoc.getRemoteSocketAddress()).getAddress().getHostAddress().trim();
//            this.port_local = tcpPunchSoc.getPort();
//            this.port = tcpPunchSoc.getLocalPort();


            this.dis = new DataInputStream(soc.getInputStream());
            this.dos = new DataOutputStream(soc.getOutputStream());
            this.conDis = new DataInputStream(conSoc.getInputStream());
            this.conDos = new DataOutputStream(conSoc.getOutputStream());


//            this.disTCPpunch = new DataInputStream(tcpPunchSoc.getInputStream());
//            this.dosTCPpunch = new DataOutputStream(tcpPunchSoc.getOutputStream());

//            initTCPpunch();

            is_connected = true;

            String res = this.dis.readUTF();
            String[] token = res.trim().split(":");

            System.out.println(res);
            if (token[0].equals("REQUEST")) {
                this.dos.writeUTF(id + "~~" + this.pass);
                this.setScreen(Integer.parseInt(token[1]), Integer.parseInt(token[2]));
            }

            this.skInSenderFilePort = this.getUDPPort("EXCHANGE-UDP-PORT-SENDER-FILE","EXCHANGE-UDP-PORT-SENDER-FILE-OKE");
            Thread.sleep(10);
            this.skInReceiverFilePort = this.getUDPPort("EXCHANGE-UDP-PORT-RECEIVER-FILE","EXCHANGE-UDP-PORT-RECEIVER-FILE-OKE");
            Thread.sleep(10);
            this.skInChatPort = this.getUDPPort("EXCHANGE-UDP-PORT-RECEIVE-CHAT", "EXCHANGE-UDP-PORT-RECEIVER-CHAT-OKE");
            Thread.sleep(10);
            this.skInSlaveKeyAndMousePort = this.getUDPPort("EXCHANGE-UDP-PORT-SLAVE-CMD-MOUSE-IN", "EXCHANGE-UDP-PORT-SLAVE-CMD-MOUSE-IN-OKE");

        } catch (Exception e) {

        }
    }

    public boolean initTCPpunch() throws IOException {
        String msg = this.disTCPpunch.readUTF();
        String[] token = msg.trim().split(":");
        return token[0].trim().equals("CONFIRM-TCP-PUNCH");
    }

    public int[] getUDPPort(String first_token, String confirm_msg) throws IOException {
        this.dos.writeUTF(first_token+":" + this.serverUDPport);
        int[] ports = new int[2];
        byte[] buf = new byte[1000];
        DatagramPacket reP = new DatagramPacket(buf, buf.length);
        boolean accepted = false;
        while (! accepted) {
            this.dgSocket.receive(reP);
            ports[0] = reP.getPort();
            String udpMsg = new String(reP.getData());
            this.publicUDPAddress = this.soc.getInetAddress();
            this.publicUDPPort = reP.getPort();
            String[] token = udpMsg.trim().split(":");
            if (token[0].trim().equals("OKE")) {
                this.dos.writeUTF(confirm_msg);
                System.out.println(confirm_msg+":"+token[1].trim()+":"+token[2].trim());
                this.local_address = token[1].trim();
                this.local_port = Integer.parseInt(token[2].trim());
                ports[1] = local_port;
                accepted = true;
            }
        }

        return ports;
    }

    public void setScreen(int w, int h) {
        this.screen = new Dimension(w, h);
    }

    public void acceptIncomingConnection(String id, String pass) {

        try {
            if (InitServer.clients.containsKey(id)) {
                XulyClient partner = InitServer.clients.get(id);

                if (partner.pass.equals(pass)) {
                    if (! partner.is_busy) {

//                         todo: trao đổi tcp punch
                        // trao đổi UDP và tcp punch
                        this.dos.writeUTF("ACCEPT-EXCHANGE:" + this.serverUDPport+":"
                                +this.IP+":"
                                +this.port+":"
                                +this.port_local+":"
                                +partner.IP+":"
                                +partner.port+":"
                                +partner.port_local);

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
                                this.dos.writeUTF("EXCHANGE-OKE-1");
                                System.out.println("NHÂN UDP thành công");
                                accepted = true;
                            }
                        }


                        partner.is_busy = true;
                        this.is_busy = true;
                        System.out.println(this.publicUDPAddress + ":" + this.publicUDPPort);
                        // chuẩn bị màn hình nhận

                        System.out.println(partner.IP);
                        this.dos.writeUTF("PREPARERECEIVE:" + partner.screen.width + ":" + partner.screen.height+":"+partner.skInSlaveKeyAndMousePort[0]+":"+partner.publicUDPAddress.getHostAddress());


                        // thông báo incoming connection cho partner



                        try {
                            partner.conDos.writeUTF("MASTER-IN-PORT:"+this.skInSenderFilePort[0]+":"+this.skInReceiverFilePort[0]+":"+this.publicUDPAddress.getHostAddress()+":"+this.skInChatPort[0]);
                            partner.conDos.writeUTF("SEND-SCREEN-TO:" + this.publicUDPAddress.getHostAddress() + ":" + this.publicUDPPort);
//                            partner.conDos.writeUTF("CHAT-BIND-TO:"+this.skInChatPort);
                            this.partnerID = partner.id;
                            partner.partnerID = this.id;
                            System.out.println("Thông báo partner thành công");
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("Partner disconnect");
                        }

                        this.dos.writeUTF("PARTNER-IN-PORTS:" + partner.skInSenderFilePort[0]+":"+partner.skInReceiverFilePort[0]+":"+partner.publicUDPAddress.getHostAddress()+
                                ":"+partner.skInChatPort[0]);



                    } else {
                        this.dos.writeUTF("PARTNER:BUSY");
                    }
                } else {
                    this.dos.writeUTF("PARTNER:WRONG-PASSWORD");
                }


            } else {
                // partner khong ton tai
                this.dos.writeUTF("PARTNER:NOT-AVAILABLE");
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
                if (command.trim().contains("CONNECT TO:")) {
                    String[] token = command.split(":");
                    String id = token[1];
                    String pass = token[2];
                    System.out.println("ok:" + id + pass);
                    acceptIncomingConnection(id, pass);
                } else if (command.trim().equals("ACK")) {

                }

            }
        } catch (IOException e) {
            System.out.println("User " + id + " disconnected!");
            is_connected = false;

            if (this.partnerID != null) {
                try {
                    InitServer.clients.get(this.partnerID).conDos.writeUTF("PARTNERDISCONNECT");
                    InitServer.clients.get(this.partnerID).is_busy = false;
                } catch (Exception ignored) {
                }
            }

            InitServer.clients.remove(id);
            System.out.println(InitServer.clients.size());
        }
    }

//    private void initPunConnection(ServerSocket socketserver){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    tcpPunchSoc = socketserver.accept();
//                    disTCPpunch = new DataInputStream(tcpPunchSoc.getInputStream());
//                    dosTCPpunch = new DataOutputStream(tcpPunchSoc.getOutputStream());
//                } catch (IOException e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }

}