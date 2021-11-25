package Client;

import Client.Utilities.CONFIG;
import Client.Utilities.ImagePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;

public class MasterScreen extends JFrame implements Runnable {
    public ImagePanel panel = new ImagePanel();
    int masterHeight, masterWidth;
    ReceiveScreen receiveScreen;
    Dimension contentDimention;

    DatagramSocket skOutMouseKey;

    boolean is_running = false;

    int minH, maxH;
    int minW, maxW;

    int vlminH, vlmaxH;
    int vlminW, vlmaxW;

    int slaveX, slaveY;


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

        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                System.out.println(e.getKeyChar());
                System.out.println("Key type at: "+e.getKeyCode() +":"+e.getKeyChar());
                String msg = "KEY-TYPE-AT:"+e.getKeyChar();
//                byte[] data = msg.getBytes();
//                DatagramPacket seP = new DatagramPacket(data, data.length, partnerAddress, partner_slave_mouse_key_port);
//                try {
//                    skOutMouseKey.send(seP);
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }

            }

            @Override
            public void keyPressed(KeyEvent e) {
//                System.out.println(e.getKeyChar());
            }

            @Override
            public void keyReleased(KeyEvent e) {
//                System.out.println(e.getKeyChar());
            }
        });

        System.out.println(panel.size().toString());


//        receiveScreen = new ReceiveScreen(skIn, screenSize);
//        receiveScreen.start();



    }

    MasterScreen(DatagramSocket skIn, Dimension screenSize, InetAddress partnerAddress, int partner_slave_mouse_key_port) throws SocketException {
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());
//        this.setUndecorated(true);
        this.add(panel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.RED));
        this.setMinimumSize(new Dimension(400,400));
        this.setVisible(true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        masterHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        masterWidth = Toolkit.getDefaultToolkit().getScreenSize().width;

        skOutMouseKey = new DatagramSocket();
        receiveScreen = new ReceiveScreen(skIn, screenSize);
        receiveScreen.start();

        // gửi chuột và phím đến partner_address qua cổng mouse_key_port

        panel.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {

                Dimension afterResize = panel.resize(panel.image, panel.getWidth(), panel.getHeight(), -1);
                vlminH = Math.abs(afterResize.height - panel.getHeight())/2;
                vlmaxH = panel.getHeight() - vlminH;

                vlminW = Math.abs(afterResize.width - panel.getWidth())/2;
                vlmaxW = panel.getWidth() - vlminW;

                maxW = afterResize.width;
                maxH = afterResize.height;
            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });

        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            // todo: thêm debounce cho các hàm press và release

            @Override
            public void keyPressed(KeyEvent e) {
//                System.out.println(e.getKeyChar());
                System.out.println(e.getKeyChar());
                System.out.println("Key type at: "+e.getKeyCode() +":"+e.getKeyChar());
                String msg = "KEY-PRESS-AT:"+e.getKeyCode() +":"+e.getKeyChar();
                byte[] data = msg.getBytes();
                DatagramPacket seP = new DatagramPacket(data, data.length, partnerAddress, partner_slave_mouse_key_port);
                try {
                    skOutMouseKey.send(seP);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
//                System.out.println(e.getKeyChar());
                System.out.println(e.getKeyChar());
                System.out.println("Key type at: "+e.getKeyCode() +":"+e.getKeyChar());
                String msg = "KEY-RELEASE-AT:"+e.getKeyCode() +":"+e.getKeyChar();
                byte[] data = msg.getBytes();
                DatagramPacket seP = new DatagramPacket(data, data.length, partnerAddress, partner_slave_mouse_key_port);
                try {
                    skOutMouseKey.send(seP);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

//        byte[] data = new byte[1000];

        panel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                System.out.println(e.getKeyChar());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyChar());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                System.out.println(e.getKeyChar());
            }
        });

        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                if(x<vlmaxW && x>vlminW && y<vlmaxH && y>vlminH){

                    slaveX = ((x-vlminW)*screenSize.width)/(vlmaxW-vlminW);
                    slaveY = ((y-vlminH)*screenSize.height)/(vlmaxH-vlminH);
                    System.out.println("Mouse position: "+slaveX+":"+slaveY);
                    String msg = "MOUSE-CLICK-AT:"+slaveX+":"+slaveY;
                    byte[] data = msg.getBytes();
                    DatagramPacket seP = new DatagramPacket(data, data.length, partnerAddress, partner_slave_mouse_key_port);
                    try {
                        skOutMouseKey.send(seP);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }).run();

    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        MasterScreen master = new MasterScreen(Toolkit.getDefaultToolkit().getScreenSize());
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


