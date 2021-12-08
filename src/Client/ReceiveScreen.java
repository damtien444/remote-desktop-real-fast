package Client;

import Client.Utilities.CONFIG;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

public class ReceiveScreen extends Thread{
    DatagramSocket              skIn;

    List<Dimension>             portionSize;
    Map<Integer, Dimension>     portionCordinate;
    Map<Integer, BufferedImage> portionScreen;
    Dimension                   screenSize;
    BufferedImage               screen;

    boolean is_running;

//    public ReceiveScreen(DatagramSocket skIn){
//
//        this.skIn = skIn;
//        this.portionCordinate = new HashMap<>();
//        this.portionScreen = new HashMap<>();
//        // nhận kích thước
//        initialize();
//        this.portionSize = calculateSmall(this.screenSize);
//        screen = new BufferedImage(this.screenSize.width, this.screenSize.height, BufferedImage.TYPE_INT_RGB);
//        setPortionCoordinate(this.portionSize);
//
//    }

    public ReceiveScreen(DatagramSocket skIn, Dimension screenSize){

        this.skIn = skIn;
        this.portionCordinate = new HashMap<>();
        this.portionScreen = new HashMap<>();
        this.screenSize = screenSize;
        // nhận kích thước
//        initialize();
        this.portionSize = calculateSmall(this.screenSize);
        screen = new BufferedImage(this.screenSize.width, this.screenSize.height, BufferedImage.TYPE_INT_RGB);
        setPortionCoordinate(this.portionSize);

    }

    public boolean initialize(){
            byte[] in_data = new byte[1000];
            DatagramPacket in_pkt = new DatagramPacket(in_data, in_data.length);
//            try {
//                skIn.receive(in_pkt);
//                System.out.println("receive: "+in_pkt.getData().toString());
//            } catch (Exception e){
//                e.printStackTrace();
//            }
            byte[] width_bytes = Arrays.copyOfRange(in_data, 0, 4);
            byte[] height_bytes = Arrays.copyOfRange(in_data, 4, 8);
            int width = ByteBuffer.wrap(width_bytes).getInt();
            int height = ByteBuffer.wrap(height_bytes).getInt();
            System.out.println(""+ width+ height);
            if (width>0 && height>0) {
                this.screenSize = new Dimension(width, height);
                return true;
            }
            else return false;

//            if (width > 0 && height > 0) {
//                String str = "Confirm-screenSize";
//                System.out.println("Confirm");
//                this.screenSize = new Dimension(width, height);
//                try {
//                    skOut.send(new DatagramPacket(str.getBytes(), str.length(), reAddr, rePort));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            } else {
//                String str = "invalid";
//                try {
//                    skOut.send(new DatagramPacket(str.getBytes(), str.length(), reAddr, rePort));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }
//    }

    public static List<Dimension> calculateSmall(Dimension screenSize){
        int height = (int) screenSize.getHeight();
        int width = (int) screenSize.getWidth();

        List<Dimension> result = new ArrayList<>();
        // best size for UDP packet: 200x100 ~ 60000 bytes

        int ratioHeight = height / CONFIG.PORTION_SIZE.height;
        int ratioWidth =  width / CONFIG.PORTION_SIZE.width;

        int smallHeight = (int) ((int) height / ratioHeight);
        int smallWidth = (int) ((int) width / ratioWidth);

        int leftHeight = height%ratioHeight;
        int leftWidth = width% ratioWidth;

//        System.out.println("Size UDP: "+smallHeight+"x"+smallWidth);
//        System.out.println("Left Over: " +leftHeight+ " " + leftWidth);
//        System.out.println("con: "+(smallHeight*ratioHeight+leftHeight));
//        System.out.println("con: "+(smallWidth*ratioWidth+leftWidth));

        for (int i = 0; i < ratioHeight; i++) {
            for (int j = 0; j < ratioWidth; j++) {
                Dimension section = new Dimension();
                section.setSize(smallWidth, smallHeight);
                result.add(section);
            }
        }

        if (leftHeight!=0){
            result.add(new Dimension(width, leftHeight));
        } else {
            result.add(null);
        }
        if (leftWidth!=0){
            result.add(new Dimension(leftWidth, height-leftHeight));
        } else {
            result.add(null);
        }

        result.add(new Dimension(ratioWidth, ratioHeight));

//        int i = 0;
//        for (Dimension d: result) {
//            if (d == null) {
//                System.out.println("Ô thứ "+ i + " là null");
//            } else {
//                System.out.println("Ô thứ "+ i +": "+ d.width + "x" + d.height);
//            }
//            i++;
//        }

        return result;
    }

    public void setPortionCoordinate(List<Dimension> portionSizes){
        int numberW, numberH;
        numberW = portionSizes.get(portionSizes.size()-1).width;
        System.out.println("NumberW: "+numberW);
        numberH = portionSizes.get(portionSizes.size()-1).height;
        System.out.println("NumberH: "+numberH);
        int index = 0;
        for (int i = 0; i < numberW; i++) {
            for (int j = 0; j < numberH; j++) {
                this.portionCordinate.put(index, new Dimension(portionSizes.get(index).width*i, portionSizes.get(index).height*j));
                index++;
            }
        }
        if (portionSizes.get(portionSizes.size()-3) != null){
            this.portionCordinate.put(index, new Dimension(0, numberH*portionSizes.get(0).height));
            index++;
        } else {
            this.portionCordinate.put(index, null);
            index++;
        }
        if (portionSizes.get(portionSizes.size()-2)!= null){
            this.portionCordinate.put(index, new Dimension(numberW*portionSizes.get(0).width, 0));
            index++;
        } else {
            this.portionCordinate.put(index, null);
            index++;
        }
    }

    public void updatePortion(){
        byte[] in_data = new byte[CONFIG.SAFE_SIZE];
        DatagramPacket in_pkt = new DatagramPacket(in_data, in_data.length);
        try {
            skIn.receive(in_pkt);

            String raw = new String(in_pkt.getData());

            if (raw.trim().equals("ACK")) return;

//            System.out.println("receive portion");
            byte[] ind_data = Arrays.copyOfRange(in_data, 0, 4);
            byte[] screen_data = Arrays.copyOfRange(in_data, 4, in_pkt.getLength());
            int ind = ByteBuffer.wrap(ind_data).getInt();
            portionScreen.put(ind, ImageIO.read(new ByteArrayInputStream(screen_data)));

//            System.out.println("receive portion: "+ ind);

            // rebuild hình

            updateScreen(portionScreen.get(ind), portionCordinate.get(ind));

        } catch (IOException e){
        }
    }

    @Override
    public void run() {
        super.run();
        this.is_running = true;
        while (this.is_running) {
            updatePortion();
        }
    }

    public void updateScreen(BufferedImage portion, Dimension coordinate){
        Graphics g = this.screen.getGraphics();
        try {
            g.drawImage(portion, coordinate.width, coordinate.height, null);
            g.dispose();
        } catch (Exception ignored){

        }

    }

    public BufferedImage getScreen(){
        return this.screen;
    }


    public static void main(String[] args) throws SocketException, UnknownHostException {
//        DatagramSocket skIn = new DatagramSocket(CONFIG.PORT_UDP_SOCKET_IN_RECEIVE_SCREEN);
//
//        new ReceiveScreen(skIn).start();

    }

}
