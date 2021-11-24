package Client;

import Client.Utilities.Receiver;
import Client.Utilities.Sender;

import javax.swing.*;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ChatAndFileTransfer extends JFrame implements Runnable{
    private JTextArea MessageHistory;
    private JTextField MessageHolder;
    private JButton MessageSend;
    private JButton FileSend;
    private JPanel mainPanel;

    private DatagramSocket skInReceiver;
    private DatagramSocket skInSender;
    private Receiver fileReceiver;
    private Sender fileSender;

    private int partner_sender_in_port;
    private int partner_receiver_in_port;
    private InetAddress partner_address;

    boolean is_running = false;

    public ChatAndFileTransfer(DatagramSocket skInSender, DatagramSocket skInReceiver, InetAddress partner_address,
                               int partner_sender_in_port, int partner_receiver_in_port){

        this.skInSender = skInSender;
        this.skInReceiver = skInReceiver;
        this.partner_address = partner_address;
        this.partner_sender_in_port = partner_sender_in_port;
        this.partner_receiver_in_port = partner_receiver_in_port;


        setContentPane(mainPanel);
        setTitle("Chat to partner!");
        setSize(450, 400);
        this.MessageHistory.setEditable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
//        new ChatAndFileTransfer();
    }


    // todo: tự khởi chạy receiver lại mỗi khi hoàn thành nhận một file
    // nếu mà đang nhận file thì ko nhận song song được
    @Override
    public void run() {
        is_running = true;
        while (is_running){
            if (this.fileReceiver == null){

            }
        }
    }
}
