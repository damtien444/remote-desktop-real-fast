package Client.Utilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImagePanel extends JPanel {
    public BufferedImage image;
    public ImagePanel(){
        try {
            image = ImageIO.read(new File("src/Client/Utilities/screen.png"));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(resize(image, getWidth(), getHeight()), 0,0, this);
    }

    public BufferedImage resize(BufferedImage img, int newW, int newH) {
        float ratio = (float)img.getHeight()/img.getWidth();
        Image tmp;
        int x,y;

        if(((float)newH/newW) < ratio) {
            tmp = img.getScaledInstance(- 1, newH, Image.SCALE_SMOOTH);
            x = (this.getWidth() - tmp.getWidth(this)) / 2;
            y = 0;
        } else {
            tmp = img.getScaledInstance(newW, -1, Image.SCALE_SMOOTH);
            x = 0;
            y = (this.getHeight() - tmp.getHeight(this)) / 2;
        }

        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();

        g2d.drawImage(tmp, x, y, null);
        g2d.dispose();

        return dimg;
    }
}
