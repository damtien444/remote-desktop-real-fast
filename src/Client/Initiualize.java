package Client;

import javax.swing.*;
import java.awt.*;
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



    Initiualize() throws InterruptedException {
        innitualizeScreen = new InnitualizeScreen();
        try {
            skInScreen = new DatagramSocket();
            byte[] data = new byte[1000];
            initSocToServer = new  Socket("localhost", 34567);
            incomingSoc = new Socket("localhost", 34568);

            this.incomingDis = new DataInputStream(incomingSoc.getInputStream());
            this.incomingDos = new DataOutputStream(incomingSoc.getOutputStream());

//            initSocToServer.setSoTimeout(100000);
            this.dos = new DataOutputStream(initSocToServer.getOutputStream());
            this.dis = new DataInputStream(initSocToServer.getInputStream());

            String[] idAndPass = requestIDandPass(dos, dis);
            innitualizeScreen.setId(idAndPass[0]);
            innitualizeScreen.setPass(idAndPass[1]);

            this.run();

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
                        int serverUDPport = Integer.parseInt(token[1]);

                        System.out.println("Port:"+serverUDPaddress+":"+serverUDPport);


                        boolean accept = false;
                        while (!accept) {
                            DatagramPacket
                                    seP =
                                    new DatagramPacket("OKE".getBytes(), "OKE".length(), serverUDPaddress, serverUDPport);

                            this.skInScreen.send(seP);
                            System.out.println("send success");

                            try {
                                msg = dis.readUTF();
                            } catch (IOException ex){
                                ex.printStackTrace();
                            }
                            if (msg.trim().equals("EXCHANGE-OKE")){
                                accept = true;
                            }
                        }

                        System.out.println("OKE");

                        String msg1 = this.dis.readUTF();
                        String[] token1 = msg1.trim().split(":");

                        if (token1[0].trim().equals("PREPARERECEIVE")){
                            Dimension receiveScreen = new Dimension(Integer.parseInt(token1[1]), Integer.parseInt(token1[2]));
                            System.out.println("Nhận thông số màn hình");

//                            new MasterScreen(skInScreen, receiveScreen);


                            while (true) {
                                DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
                                try {
                                    this.skInScreen.receive(receivePacket);    //receiver udp packet

                                    String udpMsg = new String(receivePacket.getData());   //get data from the received udp packet

                                    System.out.println("Received: " + udpMsg.trim() + ", From: IP " + receivePacket.getAddress().getHostAddress().trim() + " Port " + receivePacket.getPort());
                                } catch (IOException ex) {
                                    System.err.println("Error " + ex);
                                }

                            }

                            // thực hiện một vòng while kiểm tra nhận
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

//        innitualizeScreen.setId("haha");
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

    public void listenToIncomingConnection(){
        try {
            String msg = this.incomingDis.readUTF();

            String[] token = msg.trim().split(":");

            if (token[0].trim().equals("SEND-SCREEN-TO")){

                // dùng udp gửi đến master thử

                System.out.println("Địa chỉ UDP của master: "+token[1]+":"+token[2]);


                int j = 0;
                String msg1 = "";
                while (true) {
                    msg1 = "I AM slave " + j;

                    DatagramPacket sp = new DatagramPacket(msg1.getBytes(), msg1.getBytes().length, InetAddress.getByName(token[1].trim()), Integer.parseInt(token[2].trim()));
                    this.skInScreen.send(sp);
                    j++;
                    try{
                        Thread.sleep(2000);
                    }catch(Exception e){
                        System.err.println("Exception in Thread sleep"+e);
                    }

                }

//                new Slave(InetAddress.getByName(token[1]), Integer.parseInt(token[2]));
            }

        } catch (IOException e){
            e.printStackTrace();
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
