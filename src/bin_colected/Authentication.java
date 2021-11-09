package bin_colected;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Authentication extends JFrame implements ActionListener {
    private Socket auth_soc = null;
    DataOutputStream dos = null;
    DataInputStream dis = null;
    String verify = "";
    JButton submit;
    JPanel panel;
    JLabel label_0, label_1;
    String witdh="", height="";
    JTextField text_1;

    Authentication(Socket auth_soc){
        label_0 = new JLabel();
        label_0.setText("Mật khẩu");
        text_1 = new JTextField(15);
        this.auth_soc = auth_soc;
        label_1 = new JLabel();
        label_1.setText("");
        this.setLayout(new BorderLayout());
        submit = new JButton("Gửi");
        panel = new JPanel(new GridLayout(2,1));
        panel.add(label_0);
        panel.add(text_1);
        panel.add(label_1);
        panel.add(submit);
        add(panel, BorderLayout.CENTER);
        submit.addActionListener(this);
        setTitle("Đăng nhập");
    }

    public void actionPerformed(ActionEvent ae){
        String val = text_1.getText();
        try {
            dos = new DataOutputStream(auth_soc.getOutputStream());
            dis = new DataInputStream(auth_soc.getInputStream());
            dos.writeUTF(val);
            verify = dis.readUTF();
        } catch (Exception e){
            e.printStackTrace();
        }

        if(verify.equals("valid")){
            try {
                witdh = dis.readUTF();
                height = dis.readUTF();
            } catch (Exception e){
                e.printStackTrace();
            }
            CreateFrame abc = new CreateFrame(auth_soc,witdh,height);
            dispose();

        } else {
            System.out.println("Mật khẩu sai rồi ạ!");
            JOptionPane.showMessageDialog(this, "Mật khẩu sai", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
}
