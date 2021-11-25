package Client;

import Client.Utilities.Receiver;
import Client.Utilities.Sender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class Initiualize extends JFrame implements Runnable{

    InnitualizeScreen innitualizeScreen;
    Socket initSocToServer;
    DataOutputStream dos;
    DataInputStream dis;
    Socket incomingSoc;
    DataOutputStream incomingDos;
    DataInputStream incomingDis;
    DatagramSocket skInScreen;

    DatagramSocket skInSenderFile;
    DatagramSocket skInReceiverFile;

    DatagramSocket skOutSenderFile;
    DatagramSocket SkOutReceiverFile;

    DatagramSocket skInReceiveChat;

    DatagramSocket skInSlaveKeyAndMouse;

    private final Thread thread;
    Slave sendScreen;
    MasterScreen master;
    Thread masterThread;

    Receiver fileReceiver;
    Sender fileSender;

    int partner_sender_in_port;
    int partner_receiver_in_port;
    InetAddress partner_address;

    int partner_chat_in_port;

    Thread fileReceiverThread;
    Thread mouseAndKeyReceiver;

    Socket tcpPunch;
    DataOutputStream dosTCPpunch;
    DataInputStream disTCPpunch;

    String partner_ip;
    int partner_port;
    int partner_local_port;

    int partner_slave_mouse_key_port;

    String my_ip;
    int my_port;
    int my_local_port;
    ChatAndFileTransfer chatBox;



    Initiualize() throws InterruptedException {
        innitualizeScreen = new InnitualizeScreen();
        thread = new Thread(this);
        try {
            skInScreen = new DatagramSocket();
            byte[] data = new byte[1000];

//            initSocToServer = new  Socket("123.26.107.217", 34567);
//            incomingSoc = new Socket("123.26.107.217", 34568);
//            tcpPunch = new Socket("123.26.107.217", 34569);
            initSocToServer = new  Socket("localhost", 34567);
            incomingSoc = new Socket("localhost", 34568);
//            tcpPunch = new Socket("localhost", 34569);

            this.incomingDis = new DataInputStream(incomingSoc.getInputStream());
            this.incomingDos = new DataOutputStream(incomingSoc.getOutputStream());

//            initSocToServer.setSoTimeout(100000);
            this.dos = new DataOutputStream(initSocToServer.getOutputStream());
            this.dis = new DataInputStream(initSocToServer.getInputStream());

            this.skInSenderFile = new DatagramSocket();
            this.skInReceiverFile = new DatagramSocket();
            this.skInReceiveChat = new DatagramSocket();
            this.skInSlaveKeyAndMouse = new DatagramSocket();

            this.skOutSenderFile = new DatagramSocket();
            this.SkOutReceiverFile = new DatagramSocket();

//            this.disTCPpunch = new DataInputStream(tcpPunch.getInputStream());
//            this.dosTCPpunch = new DataOutputStream(tcpPunch.getOutputStream());

            // gửi cònfirm tcp punch
//            this.dosTCPpunch.writeUTF("CONFIRM-TCP-PUNCH:START");


            String[] idAndPass = requestIDandPass(dos, dis);
            innitualizeScreen.setId(idAndPass[0]);
            innitualizeScreen.setPass(idAndPass[1]);

            sendSkInToServer("EXCHANGE-UDP-PORT-SENDER-FILE", "EXCHANGE-UDP-PORT-SENDER-FILE-OKE", skInSenderFile);
            sendSkInToServer("EXCHANGE-UDP-PORT-RECEIVER-FILE", "EXCHANGE-UDP-PORT-RECEIVER-FILE-OKE", skInReceiverFile);
            sendSkInToServer("EXCHANGE-UDP-PORT-RECEIVE-CHAT", "EXCHANGE-UDP-PORT-RECEIVER-CHAT-OKE", skInReceiveChat);
            sendSkInToServer("EXCHANGE-UDP-PORT-SLAVE-CMD-MOUSE-IN", "EXCHANGE-UDP-PORT-SLAVE-CMD-MOUSE-IN-OKE", skInSlaveKeyAndMouse);

            // start listening incoming connection
            this.start();

            innitualizeScreen.connect.addActionListener(e -> {


                System.out.println("Connect to other peer");

                String otherID = innitualizeScreen.otherID.getText();
                String otherPass = innitualizeScreen.otherPass.getText();

                if (otherID.length() == 8 && otherPass.length() == 8){
                    try {
                        dos.writeUTF("CONNECT TO:"+otherID+":"+otherPass);


                        String msg = dis.readUTF();
                        String[] token = msg.trim().split(":");
                        InetAddress serverUDPaddress = initSocToServer.getInetAddress();

                        if (token[0].trim().equals("ACCEPT-EXCHANGE")) {
                            int serverUDPport = Integer.parseInt(token[1]);

                            System.out.println("Port:" + serverUDPaddress + ":" + serverUDPport);

                            this.my_ip = token[2].trim();
                            this.my_port = Integer.parseInt(token[3].trim());
                            this.my_local_port = Integer.parseInt(token[4].trim());

                            this.partner_ip = token[5].trim();
                            this.partner_port = Integer.parseInt(token[6].trim());
                            this.partner_local_port = Integer.parseInt(token[7].trim());

                            boolean accept = false;
                            while (! accept) {
                                DatagramPacket
                                        seP =
                                        new DatagramPacket("OKE".getBytes(), "OKE".length(), serverUDPaddress, serverUDPport);

                                // su dung them soc

                                this.skInScreen.send(seP);
                                System.out.println("send success");

                                try {
                                    msg = dis.readUTF();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                if (msg.trim().equals("EXCHANGE-OKE-1")) {
                                    accept = true;
                                }
                            }

                            System.out.println("OKE");

                            // nhận portIn của partner

                            String msg1 = this.dis.readUTF();
                            String[] token1 = msg1.trim().split(":");



                            if (token1[0].trim().equals("PREPARERECEIVE")) {
                                Dimension
                                        receiveScreen =
                                        new Dimension(Integer.parseInt(token1[1]), Integer.parseInt(token1[2]));
                                System.out.println("Nhận thông số màn hình");

                                this.partner_slave_mouse_key_port = Integer.parseInt(token1[3].trim());
                                this.partner_address = InetAddress.getByName(token1[4].trim());

                                this.master = new MasterScreen(skInScreen, receiveScreen, partner_address, partner_slave_mouse_key_port);
                                this.masterThread = new Thread(master);
                                this.masterThread.start();
//                            while (true) {
//                                DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
//                                try {
//                                    this.skInScreen.receive(receivePacket);    //receiver udp packet
//
//                                    String udpMsg = new String(receivePacket.getData());   //get data from the received udp packet
//
//                                    System.out.println("Received: " + udpMsg.trim() + ", From: IP " + receivePacket.getAddress().getHostAddress().trim() + " Port " + receivePacket.getPort());
//                                } catch (IOException ex) {
//                                    System.err.println("Error " + ex);
//                                }
//
//                            }

                                // thực hiện một vòng while kiểm tra nhậ
                            }


                            msg = this.dis.readUTF();
                            String[] token2 = msg.trim().split(":");
                            if (token2[0].trim().equals("PARTNER-IN-PORTS")){
                                this.partner_sender_in_port = Integer.parseInt(token2[1].trim());
                                this.partner_receiver_in_port = Integer.parseInt(token2[2].trim());
                                this.partner_address = InetAddress.getByName(token2[3].trim());
                                this.partner_chat_in_port = Integer.parseInt(token2[4].trim());
                                System.out.println("partner address: "+token2[3].trim()+":"+partner_sender_in_port+":"+partner_receiver_in_port);
                            }

                            // TODO: khởi động màn hình chat và file transfer của master
                            System.out.println(msg);

                            chatBox =  new ChatAndFileTransfer(this.skInSenderFile, this.skInReceiverFile, this.partner_address, this.partner_sender_in_port,
                                    this.partner_receiver_in_port, this.skInReceiveChat, this.partner_chat_in_port, "Master");
                            new Thread(chatBox).start();

//                            String path = "C:\\Users\\damti\\OneDrive - Danang University of Technology\\OneDrive - The University of Technology\\Desktop\\Study\\Doan Coso Nganh Mang\\RemoteDesktop\\src\\Client\\Utilities\\screen.png";
//                            this.fileSender = new Sender(this.partner_receiver_in_port, skInSenderFile, this.partner_address.getHostAddress(),path,"sended-through-UDP.png");


                            // TODO: kiểm tra gửi đến

                            // Master send by sender to slave receiver
//                            new Thread(new Runnable() {
//                                private String udpMsg = "";
//                                @Override
//                                public void run() {
//                                    int j = 0;
//                                    String msgs = "";
//                                    byte[] sendData = new byte[1000];
//                                    while (true) {
//                                        msgs = "I AM CLIENT Master send by sender to slave receiver " + j;
//                                        sendData = msgs.getBytes();
//                                        DatagramPacket
//                                                sp =
//                                                new DatagramPacket(sendData, sendData.length, partner_address, partner_receiver_in_port);
//                                        try {
//                                            skOutSenderFile.send(sp);
//                                        } catch (IOException ex) {
//                                            ex.printStackTrace();
//                                        }
//                                        j++;
//                                        try {
//                                            Thread.sleep(2000);
//                                        } catch (Exception ex) {
//                                            System.err.println("Exception in Thread sleep" + e);
//                                        }
//                                    }
//                                }
//                            }).start();
                            // Master send by receiver to slave sender


                        } else if (token[0].trim().equals("PARTNER")){
                            if (token[1].trim().equals("NOT-AVAILABLE")){
                                JOptionPane.showMessageDialog(null, "Partner is not available!");
                            } else if (token[1].trim().equals("WRONG-PASSWORD")){
                                JOptionPane.showMessageDialog(null, "Wrong partner password!");
                            } else if (token[1].trim().equals("BUSY")){
                                JOptionPane.showMessageDialog(null, "Partner is currently connect to other!");
                            }
                        }



                        // khởi động màn hình master truyền vào socin

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Mất kết nối máy chủ");
                        System.exit(-1);
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "ID hoặc pass sai");
                }
                // yêu cầu địa chỉ của máy peer
                // và kích hoạt udp punch

                // khởi động master
            });




        } catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public void createTcpHole(InetAddress addrToConnect, int portToConnect, int localPort){
        if (this.tcpPunch!=null){
            this.dosTCPpunch = null;
            this.disTCPpunch = null;

            String addr = addrToConnect.getHostAddress().trim();

            // chờ kết nối của đối phương
        }
    }

    public void sendSkInToServer(String first_token, String confirm_msg, DatagramSocket socIn) throws IOException {

        String msg = dis.readUTF();
        String[] token = msg.trim().split(":");
        InetAddress serverUDPaddress = initSocToServer.getInetAddress();

        if (token[0].trim().equals(first_token)) {
            int serverUDPport = Integer.parseInt(token[1]);
            System.out.println("Port:" + serverUDPaddress + ":" + serverUDPport);

            boolean accept = false;
            while (! accept) {
                DatagramPacket
                        seP =
                        new DatagramPacket("OKE".getBytes(), "OKE".length(), serverUDPaddress, 40000);

                // su dung them soc
                socIn.send(seP);

                try {
                    initSocToServer.setSoTimeout(1000);
                    msg = dis.readUTF();
                    if (msg.trim().equals(confirm_msg)) {
                        System.out.println(confirm_msg);
                        accept = true;
                        initSocToServer.setSoTimeout(5000);
                        System.out.println("send success");
                    }


                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

    public String[] requestIDandPass(DataOutputStream dos, DataInputStream dis) throws IOException {

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        dos.writeUTF("REQUEST:"+screen.width+":"+screen.height);

        String res = dis.readUTF();

        String[] token = res.split("~~");

        return token;
    }

    public static void main(String[] args) throws InterruptedException {
        new Initiualize();

    }

    public void start(){
        thread.start();
    }

    public void listenToIncomingConnection(){
        try {
            String msg = this.incomingDis.readUTF();

            String[] token = msg.trim().split(":");


            if (token[0].trim().equals("SEND-SCREEN-TO")){

                // dùng udp gửi đến master thử

                System.out.println("Địa chỉ UDP của master: "+token[1]+":"+token[2]);


                sendScreen = new Slave(InetAddress.getByName(token[1].trim()), Integer.parseInt(token[2].trim()));
                sendScreen.sendScreen.start();

                // tạo thread nhận chuột và phím -> skInKeyandMouse
                mouseAndKeyReceiver = new Thread(() -> {
                    System.out.println("Listnening incoming mouse and key");
                    byte[] data = new byte[1000];
                    DatagramPacket reP = new DatagramPacket(data, data.length);
                    String msg1;
                    int x,y;
                    String[] token1;

                    try {
                        Robot robot = new Robot();
                        while (true){
                            try {

                                skInSlaveKeyAndMouse.receive(reP);
                                msg1 = new String(reP.getData());
                                System.out.println(msg1);
                                token1 = msg1.trim().split(":");
                                if (token1[0].trim().equals("MOUSE-CLICK-AT")){
                                    x = Integer.parseInt(token1[1].trim());
                                    y = Integer.parseInt(token1[2].trim());

                                    robot.mouseMove(x, y);
                                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                } else if (token1[0].trim().equals("KEY-PRESS-AT")){
                                    try {
                                        robot.keyPress(Integer.parseInt(token1[1].trim()));
//                                        robot.keyRelease(Integer.parseInt(token1[1].trim()));
                                        System.out.println(Integer.parseInt(token1[1].trim()));
                                    } catch (NumberFormatException e){

                                    }
                                } else if (token1[0].trim().equals("KEY-RELEASE-AT")){
                                    try {
//                                        robot.keyPress(Integer.parseInt(token1[1].trim()));
                                        robot.keyRelease(Integer.parseInt(token1[1].trim()));
                                        System.out.println(Integer.parseInt(token1[1].trim()));
                                    } catch (NumberFormatException e){

                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }

                });
                mouseAndKeyReceiver.start();


            } else if (token[0].trim().equals("PARTNERDISCONNECT")){

                JOptionPane.showMessageDialog(null, "PARTNER IS DISCONNECTED!");
                // dung gui
                if (sendScreen!=null)  sendScreen.sendScreen.running = false;

                if (masterThread !=null) {

                    // dừng chờ nhận màn hình
                    this.master.receiveScreen.is_running = false;

                    // dừng cập nhật màn hình
                    this.master.is_running = false;

                    // TODO: đóng màn hình chat và file transfer, đóng các kết nối sk

                    this.master.dispose();

                    // đóng màn hình nhận và quay trở lại
                }

                if (chatBox.is_running_chat){

                    chatBox.is_running_chat = false;

                    chatBox.dispose();
                }
            } else if (token[0].trim().equals("MASTER-IN-PORT")){
                this.partner_sender_in_port = Integer.parseInt(token[1].trim());
                this.partner_receiver_in_port = Integer.parseInt(token[2].trim());
                this.partner_address = InetAddress.getByName(token[3].trim());
                this.partner_chat_in_port = Integer.parseInt(token[4].trim());

                System.out.println(msg);

                chatBox = new ChatAndFileTransfer(this.skInSenderFile, this.skInReceiverFile, this.partner_address, this.partner_sender_in_port, this.partner_receiver_in_port,
                        this.skInReceiveChat, this.partner_chat_in_port, "Slave");
                new Thread(chatBox).start();

                // TODO: khởi động màn hình chat và file transfer

//                String path = "C:\\Users\\damti\\OneDrive - Danang University of Technology\\OneDrive - The University of Technology\\Desktop\\Study\\Doan Coso Nganh Mang\\RemoteDesktop\\src\\Client";
//                fileReceiverThread = new Thread(()->{
//                    this.fileReceiver = new Receiver(this.skInReceiverFile, partner_sender_in_port, partner_address.getHostAddress().trim(), path);
//                });
//                fileReceiverThread.start();

                // kiểm tra nhận port từ sender
                // kiểm tra gửi đến sender

//                new Thread(new Runnable() {
//                    private String udpMsg = "";
//                    @Override
//                    public void run() {
//                        //create datagram packet to receive udp messages
//                        DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
//                        while (true) {
//                            try {
//                                skInReceiverFile.receive(receivePacket);    //receiver udp packet
//
//                                udpMsg = new String(receivePacket.getData());   //get data from the received udp packet
//
//                                System.out.println("Received: " + udpMsg.trim() + ", From: IP " + receivePacket.getAddress().getHostAddress().trim() + " Port " + receivePacket.getPort());
//                            } catch (IOException ex) {
//                                System.err.println("Error " + ex);
//                            }
//
//                        }
//                    }
//
//                }).start();

            }

        } catch (IOException e){
//            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Mất kết nối máy chủ");
            System.exit(-1);
        }
    }

    @Override
    public void run() {


        while (true){
            listenToIncomingConnection();
        }
    }
}
