package Client;

import Client.Utilities.CONFIG;
import Client.Utilities.ImagePanel;

import javax.swing.*;
import java.awt.*;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MasterScreen extends JFrame implements Runnable {
    public ImagePanel panel = new ImagePanel();
    int masterHeight, masterWidth;
    ReceiveScreen receiveScreen;

    boolean is_running = false;


    MasterScreen(Dimension screenSize) throws SocketException, UnknownHostException {
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());
//        this.setUndecorated(true);
        this.add(panel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.RED));
        this.setVisible(true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        masterHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        masterWidth = Toolkit.getDefaultToolkit().getScreenSize().width;

        DatagramSocket skIn = new DatagramSocket(CONFIG.PORT_UDP_SOCKET_IN_RECEIVE_SCREEN);

        receiveScreen = new ReceiveScreen(skIn, screenSize);
        receiveScreen.start();
    }

    MasterScreen(DatagramSocket skIn, Dimension screenSize) {
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());
//        this.setUndecorated(true);
        this.add(panel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.RED));
        this.setVisible(true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        masterHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        masterWidth = Toolkit.getDefaultToolkit().getScreenSize().width;

        receiveScreen = new ReceiveScreen(skIn, screenSize);
        receiveScreen.start();
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
//        MasterScreen master = new MasterScreen(Toolkit.getDefaultToolkit().getScreenSize());
//        new Thread(master).start();
    }

    @Override
    public void run() {
        // nhận màn hình
        try {
            is_running = true;

            while (is_running) {
                try {



                    panel.setImage(receiveScreen.getScreen());
//                    ds.receive(dp);
//
//                    int len = dp.getLength();
//                    System.out.println(dp.getLength());
//                    String restr = new String(dp.getData()).substring(0, dp.getLength());
//
//                    if (restr.contains("START")) {
//                        String lenght = new String(restr.substring(11, len));
//                        System.out.println("Start: "+ lenght);
//                        full_UDP_lenght = Integer.parseInt(lenght);
//                        for (int i = 0; i < full_UDP_lenght; i++) {
//                            buffer_UDP.put(i, null);
//                        }
//                        count = 0;
//                    } else if (restr.contains("END")) {
//                        /// end packet
//
//                        System.out.println("END received");
//
//                        // reconstruct large data
////                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                        ByteBuffer img = ByteBuffer.allocate(full_UDP_lenght*CONFIG.SAFE_SIZE);
//
//                        int lost = 0;
//                        for (int i = 0; i < full_UDP_lenght; i++) {
////                            if (buffer_UDP.get(i) == null) {
////                                lost++;
////                                img.put(replace);
////                            } else {
////                                System.out.println(i+ "Package: " + Arrays.toString(buffer_UDP.get(i)));
//                                img.put(buffer_UDP.get(i));
////                            }
//                        }
////                        byte[] reconstruct = outputStream.toByteArray();
////                        String base64 = Arrays.toString(reconstruct);
////                        BufferedImage receive = Jpg2Base64.decoder(base64);
//
//
////                        ByteArrayInputStream bais = new ByteArrayInputStream(reconstruct);
//                        ByteArrayInputStream bais = new ByteArrayInputStream(img.array());
//                        BufferedImage receive = ImageIO.read(bais);
////                        if (receive != null){
//                            panel.setImage(receive);
//                            System.out.println("Success: Reconstruct with " + lost + " lost " + ((receive != null)? " khac null": " null"));
////                        }
//                        bais.close();
//
//                        // bao hieu cap nhat frame
//
//                    } else {
//
//
//                        count++;
//                        /// packet with index i
//                        byte[] flag = Arrays.copyOfRange(dp.getData(), 0, 4);
//                        ByteBuffer wrapped = ByteBuffer.wrap(flag); // big-endian by default
//                        int index_of_packet = wrapped.getInt();
//
//                        if (index_of_packet < 1000 && index_of_packet > 0) {
//
//                            byte[] data_small = Arrays.copyOfRange(dp.getData(), 4, dp.getLength());
//
//                            buffer_UDP.put(index_of_packet, data_small);
//                        }
//                    }


                    // sort type of data
                    // 1 hoặc là start
                    // 2 hoặc là end
                    // 3 hoặc là một segment

                    // segment ko bao giờ nhận dc => tùy vào độ quan trọng thì request lại

//                    System.out.println("receive something")
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {

        }
    }


}


