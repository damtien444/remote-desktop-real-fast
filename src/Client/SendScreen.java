package Client;

import Client.Utilities.CONFIG;
import Client.Utilities.Screen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendScreen extends Thread{
    public Screen           screen;
    Dimension               screenSize;
    DatagramSocket          skOut;
    InetAddress             reAddr;
    int                     rePort;
    List<Dimension> portionSize;
    Map<Integer, Dimension> portionCordinate;
    Map<Integer, BufferedImage> portionScreen;
    List<Integer> changeList;
    boolean running = false;

    public SendScreen(DatagramSocket skOut, InetAddress reAddr, int rePort) throws AWTException {
        this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screen = new Screen();
        this.reAddr = reAddr;
        this.skOut = skOut;
        this.rePort = rePort;
        this.portionSize = calculateSmall(this.screenSize);
        this.portionCordinate = new HashMap<>();
        this.portionScreen = new HashMap<>();
        setPortionCoordinate(this.portionSize);
        // init gửi kích thước màn hình

        initialize();


//        this.start();
    }


    public void initialize() {
        try {

            ByteBuffer bb = ByteBuffer.allocate(8);
            bb.putInt(this.screenSize.width);
            bb.putInt(this.screenSize.height);
            byte[] out = bb.array();
            skOut.send(new DatagramPacket(out, out.length, reAddr, rePort));
            byte[] in_data = new byte[1000];
            DatagramPacket in_pkt = new DatagramPacket(in_data, in_data.length);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public BufferedImage getScreen() {
        return screen.takeSnap();
    }

    public Dimension getScreenSize() {
        return screenSize;
    }

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

    public boolean isChange(BufferedImage screen, List<Dimension> portionSizes){
        this.changeList = new ArrayList<>();

        for (int i = 0; i < portionCordinate.keySet().size(); i++) {
            if (portionCordinate.get(i) == null) continue;
            try {
                BufferedImage now = screen.getSubimage(portionCordinate.get(i).width,
                        portionCordinate.get(i).height,
                        portionSizes.get(i).width,
                        portionSizes.get(i).height);
                BufferedImage old = portionScreen.get(i);
                if (now != null && old != null){
                    if (! compareImages(now, old)){
                        this.changeList.add(i);
                    }
                } else this.changeList.add(i);
            } catch (RasterFormatException e){
                System.out.println(portionCordinate.get(i).width);
                System.out.println(portionCordinate.get(i).height);
                e.printStackTrace();
                System.exit(-1);
            }


        }
        return  (this.changeList.size()>0);
    }

    public static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
        // The images must be the same size.
        if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
            return false;
        }

        int width  = imgA.getWidth();
        int height = imgA.getHeight();

        // Loop over every pixel.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Compare the pixels for equality.
                if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    public BufferedImage copySubimage(BufferedImage image, int x, int y, int w, int h){
        BufferedImage img = image.getSubimage(x, y, w, h); //fill in the corners of the desired crop location here
        BufferedImage copyOfImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = copyOfImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return copyOfImage;
    }

    public void updatePortionScreen(List<Dimension> portionSizes) throws IOException {
        // nếu giống cũ thì ko cần fill -> có một mảng change list

        while (getScreen()==null){};

        BufferedImage screen = getScreen();

        boolean yes = isChange(screen, portionSizes);

        // nếu khác thì update và tiến hành gửi
        if (yes){
            for (int i = 0; i < changeList.size(); i++) {
                int ind = changeList.get(i);
//                System.out.println("Change detect and send "+ind);
                BufferedImage img = copySubimage(screen,
                        portionCordinate.get(ind).width,
                        portionCordinate.get(ind).height,
                        portionSizes.get(ind).width,
                        portionSizes.get(ind).height);

                portionScreen.put(i, img);
                // send i và portion đó qua

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "jpg", baos);
                byte[] bytes = baos.toByteArray();

                ByteBuffer bb = ByteBuffer.allocate(4+bytes.length);
                bb.putInt(ind);
                bb.put(bytes);

                byte[] out = bb.array();
                skOut.send(new DatagramPacket(out, out.length, reAddr, rePort));


            }
        }
    }




    @Override
    public void run() {
        super.run();
        this.running = true;
        while (this.running){
            try {
                updatePortionScreen(this.portionSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws AWTException, IOException {

//        SendScreen.calculateSmall(new Dimension(1920, 1080));

        DatagramSocket skOut = new DatagramSocket();
        SendScreen gui = new SendScreen(skOut, InetAddress.getByName("localhost"), CONFIG.PORT_UDP_SOCKET_IN_RECEIVE_SCREEN);
        gui.start();
//
//        File file1 = new File("C:\\Users\\damti\\OneDrive - Danang University of Technology\\OneDrive - The University of Technology\\Desktop\\Study\\Doan Coso Nganh Mang\\RemoteDesktop\\src\\Client\\Utilities\\img.png");
//        File file = new File("C:\\Users\\damti\\OneDrive - Danang University of Technology\\OneDrive - The University of Technology\\Desktop\\Study\\Doan Coso Nganh Mang\\RemoteDesktop\\src\\Client\\Utilities\\screen.png");
//        BufferedImage image = ImageIO.read(file1);
//        BufferedImage image1 = ImageIO.read(file1);
//        Graphics g = image1.getGraphics();
//        g.setColor(Color.RED);
//        g.drawLine(0,0, 3, 3);
//        g.dispose();
//        System.out.println(compareImages(image1,image));;
    }
}