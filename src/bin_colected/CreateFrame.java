package bin_colected;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;

public class CreateFrame extends Thread{
    String width, height;
    private JFrame frame = new JFrame();
    private JDesktopPane desktopPane = new JDesktopPane();
    private Socket socket;
    private JInternalFrame internalFrame = new JInternalFrame("Màn hình máy bạn", true,true,true);
    private JPanel panel = new JPanel();
    public CreateFrame(Socket socket, String width, String height){
        this.width = width;
        this.height = height;
        this.socket = socket;
        start();
    }

    public void drawGUI(){
        frame.add(desktopPane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setExtendedState(frame.getExtendedState()|Frame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        internalFrame.setLayout(new BorderLayout());
        internalFrame.getContentPane().add(panel, BorderLayout.CENTER);
        internalFrame.setSize(100,100);
        desktopPane.add(internalFrame);
    }


}
