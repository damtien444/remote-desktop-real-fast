package Client;

import Client.Utilities.CONFIG;
import Client.Utilities.ImagePanel;
import Client.Utilities.Jpg2Base64;
import Client.Utilities.Screen;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MasterScreen extends JFrame implements Runnable {
    public ImagePanel panel = new ImagePanel();
    private BufferedImage image;
    int masterHeight, masterWidth;
    Map<Integer, byte[]> buffer_UDP;

    MasterScreen() {
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setExtendedState(MAXIMIZED_BOTH);
//        this.setUndecorated(true);

        buffer_UDP = new HashMap<>();
        buffer_UDP.put(- 1, null);
        for (int i = 0; i < 1000; i++) {
            buffer_UDP.put(i, null);
        }


        this.add(panel, BorderLayout.CENTER);

        panel.setBorder(BorderFactory.createLineBorder(Color.RED));


        this.setVisible(true);
        masterHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        masterWidth = Toolkit.getDefaultToolkit().getScreenSize().width;

    }


    public static void main(String[] args) {
        MasterScreen master = new MasterScreen();
        new Thread(master).start();
    }

    @Override
    public void run() {
        // nhận màn hình
        try {
            byte[] data = new byte[1000];
            byte[] replace = new byte[CONFIG.SAFE_SIZE];
            DatagramPacket dp = new DatagramPacket(data, data.length);
            DatagramSocket ds = new DatagramSocket(CONFIG.CLIENT_PORT);
            int full_UDP_lenght = 0;
            int count = 0;
            while (true) {
                try {
                    ds.receive(dp);

                    int len = dp.getLength();
                    String restr = new String(dp.getData()).substring(0, dp.getLength());

                    if (restr.contains("START")) {
                        String lenght = new String(restr.substring(11, len));
                        full_UDP_lenght = Integer.parseInt(lenght);
                        count = 0;
                    } else if (restr.contains("END")) {
                        /// end packet

                        System.out.println("END received");

                        // reconstruct large data
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                        int lost = 0;
                        for (int i = 0; i < full_UDP_lenght; i++) {
                            if (buffer_UDP.get(i) == null) {
                                lost++;
                                outputStream.write(replace);
                            } else {
                                outputStream.write(buffer_UDP.get(i));
                            }
                        }
                        byte[] reconstruct = outputStream.toByteArray();
//                        String base64 = Arrays.toString(reconstruct);
//                        BufferedImage receive = Jpg2Base64.decoder(base64);



                        panel.setImage(receive);
                        System.out.println("Reconstruct with " + lost + "lost");
                        outputStream.close();

                        // bao hieu cap nhat frame

                    } else {


                        count++;
                        /// packet with index i
                        byte[] flag = Arrays.copyOfRange(dp.getData(), 0, 4);
                        ByteBuffer wrapped = ByteBuffer.wrap(flag); // big-endian by default
                        int index_of_packet = wrapped.getInt();

                        if (index_of_packet < 1000 && index_of_packet > 0) {

                            byte[] data_small = Arrays.copyOfRange(dp.getData(), 4, dp.getLength());

                            buffer_UDP.put(index_of_packet, data_small);
                        }
                    }


                    // sort type of data
                    // 1 hoặc là start
                    // 2 hoặc là end
                    // 3 hoặc là một segment

                    // segment ko bao giờ nhận dc => tùy vào độ quan trọng thì request lại

//                    System.out.println("receive something");


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        } catch (Exception e) {

        }
    }


}


