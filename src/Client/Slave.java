package Client;

import Client.Utilities.Jpg2Base64;
import Client.Utilities.Screen;
import Client.Utilities.CONFIG;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.zip.GZIPOutputStream;


public class Slave extends Thread {
    //    static Screen bufferedScreen;
    Robot robot;
    static Rectangle screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

//    static {
//        try {
//            bufferedScreen = new Screen();
//        } catch (AWTException e) {
//            e.printStackTrace();
//        }
//    }

    public Slave() {
        // gửi

        start();

        // nhận

    }

    @Override
    public void run() {
        super.run();
//        int width = ;
//        int height = ;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        try {
            DatagramSocket ds = new DatagramSocket();
            Date date = new Date();
            long last = System.currentTimeMillis();
            ServerSocket serverSocket = new ServerSocket(10000);
            Socket socket = serverSocket.accept();
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
//            BufferedImage image = ImageIO.read(new File("src/Client/Utilities/screen.png"));


//        BufferedImage recent_screen = new BufferedImage(Screen.bufferedScreen.get(0).getWidth(), Screen.bufferedScreen.get(0).getHeight(), BufferedImage.TYPE_3BYTE_BGR);

            while (true) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();



//                System.out.println("SENT");

                    BufferedImage current_screen = robot.createScreenCapture(screenRectangle);

                    ImageIO.write(current_screen, "jpg", socket.getOutputStream());

//                    byte[] bs = baos.toByteArray();

//                    String base64 = Jpg2Base64.encoder(current_screen);
//                    byte[] bs = base64.getBytes(StandardCharsets.UTF_8);

//                    breaksend(bs, ds);
//                    send(bs, dataOutputStream);
//                    sleep(100);

//                System.out.println("SENT at fps: " + (1000/(System.currentTimeMillis()-last)));

                    last = System.currentTimeMillis();

//                sleep((long) (1/CONFIG.FPS_CLIENT)*1000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void send(byte[] bs, DataOutputStream dos){
        try {
            dos.write(bs);
            System.out.println("Send: "+ dos.size());
            dos.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void breaksend(byte[] bs, DatagramSocket ds) throws InterruptedException {

        // Để tách một chuỗi byte ra nhiều datagram rồi gửi qua master

        int safe_size = CONFIG.SAFE_SIZE;
        int num_UDP = (int) Math.ceil((float) bs.length / safe_size);
        try {


//        byte[] lengh_flag = bigIntToByteArray(num_UDP);
        send_start_flag(ds, num_UDP);
        System.out.println(num_UDP);
        for (int i = 0; i < num_UDP; i++) {

            byte[] bs_small = Arrays.copyOfRange(bs, i * safe_size, (i + 1) * safe_size);

            // Thêm cờ đánh dấu ở cuối mỗi datagram để reconstruct
            byte[] flag = IntToByteArray(i);

//            System.out.println(flag.length);
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(flag);
                outputStream.write(bs_small);
                byte send[] = outputStream.toByteArray();

                DatagramPacket datagramPacket = new DatagramPacket(
                        send, send.length, new InetSocketAddress(CONFIG.CLIENT_IP, CONFIG.CLIENT_PORT)
                );


                ds.send(datagramPacket);
                sleep(0);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        sleep(3);

        send_end_flag(ds, num_UDP);
        sleep(3);

        } catch (Exception e){
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {

        Slave slave = new Slave();
    }

    private byte[] IntToByteArray(final int i) {
        byte[] bytes = ByteBuffer.allocate(Integer.BYTES).putInt(i).array();
        return bytes;
    }

    private byte[] IntToStringBytes(int i){
        String s = Integer.toString(i);
        int left = 4 - s.length();
        for (int j = 0; j < left; j++) {
            s = "0"+s;
        }
//        System.out.println(s);
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private void send_start_flag(DatagramSocket ds, int num_UDP) throws IOException {
        String flag = "START$#$#$#" + num_UDP;
        byte[] vOut = flag.getBytes(StandardCharsets.UTF_8);

        DatagramPacket datagramPacket = new DatagramPacket(
                vOut, vOut.length, new InetSocketAddress(CONFIG.CLIENT_IP, CONFIG.CLIENT_PORT)
        );

        ds.send(datagramPacket);

    }

    private void send_end_flag(DatagramSocket ds, int num_UDP) throws IOException {
        String flag = "#$END$#";
        byte[] vOut = flag.getBytes(StandardCharsets.UTF_8);
        System.out.println("END Of Pack");
        DatagramPacket datagramPacket = new DatagramPacket(
                vOut, vOut.length, new InetSocketAddress(CONFIG.CLIENT_IP, CONFIG.CLIENT_PORT)
        );

        ds.send(datagramPacket);


    }
}
