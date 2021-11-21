package Client.Utilities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Screen extends Thread{
    static Rectangle screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    public static ArrayList<BufferedImage> bufferedScreen = new ArrayList<>();
    Robot robot;
    static int pointer = 0;
    public static int MAX = 60;

    public Screen() throws AWTException {
        this.robot = new Robot();
//        start();
    }

    public BufferedImage takeSnap(){
        return this.robot.createScreenCapture(screenRectangle);
    }

    public BufferedImage getSnap(){
        try {
            return bufferedScreen.get(bufferedScreen.size()-1);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        super.run();

        while (pointer <= MAX){
            bufferedScreen.add(takeSnap());
            pointer++;
        }

        while (true){
            try {
                bufferedScreen.remove(0);
                bufferedScreen.add(takeSnap());
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }



}
