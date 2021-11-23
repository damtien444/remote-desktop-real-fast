package Client;

import javax.swing.*;
import java.awt.*;

public class InnitualizeScreen extends JFrame{
    JTextField id;
    JTextField pass;
    JTextField otherID;
    JTextField otherPass;
    JButton connect;

    InnitualizeScreen(){
        JFrame frame = new JFrame();
        frame.setTitle("Chào mừng đến với trình điều khiển từ xa!");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(500, 700);
        frame.setLayout(new GridLayout(5,1));

        // request id và pass từ máy chủ


        id = new JTextField();
        id.setEditable(false);
        id.setText("----------------");
        frame.add(id);

        pass = new JTextField();
        pass.setEditable(false);
        pass.setText("---------------");
        frame.add(pass);



        otherID = new JTextField();
        otherID.setPreferredSize(new Dimension(200, 30));
        frame.add(otherID);

        otherPass = new JTextField();
        otherPass.setPreferredSize(new Dimension(200, 30));
        frame.add(otherPass);

        connect = new JButton();
        connect.setText("Connect to peer");


        // nếu có incomming connection thì destroy màn hình init
        // nếu ngắt kết nối thì hiển thị lại màn hình init

        frame.add(connect);

        frame.setVisible(true);
    }

    public void setId(String id){
        this.id.setText(id);
    }

    public void setPass(String pass) {
        this.pass.setText(pass);;
    }
}
