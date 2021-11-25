package Client;

import Client.Utilities.Receiver;
import Client.Utilities.Sender;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;

public class ChatAndFileTransfer extends JFrame implements Runnable{
    private JTextArea MessageHistory;
    private JTextField MessageHolder;
    private JButton MessageSend;
    private JButton FileSend;
    private JPanel mainPanel;
    private JScrollPane ScrollPaneHistory;

    private DatagramSocket skInReceiver;
    private DatagramSocket skInSender;
    private Receiver fileReceiver;
    private Sender fileSender;

    private int partner_sender_in_port;
    private int partner_receiver_in_port;
    private InetAddress partner_address;

    private DatagramSocket skInChat;
    private DatagramSocket skOutChat;
    int partner_receiver_chat_port;

    private String chatHistory;

    boolean is_running_chat = false;

    Thread recreateReceiver;

    public ChatAndFileTransfer(DatagramSocket skInSender, DatagramSocket skInReceiver, InetAddress partner_address,
                               int partner_sender_in_port, int partner_receiver_in_port, DatagramSocket skInChat, int partner_receiver_chat_port, String title){

        // phục vụ nhận gửi file
        try {
            this.skInSender = skInSender;
            this.skInReceiver = skInReceiver;

            this.partner_address = partner_address;
            this.partner_sender_in_port = partner_sender_in_port;
            this.partner_receiver_in_port = partner_receiver_in_port;

//            JScrollPane jsp = new JScrollPane(MessageHistory);
//            this.add(jsp);

            chatHistory = "CHAT với: "+partner_address.getHostAddress();
            addHistory(chatHistory);
//            this.MessageHistory.setLineWrap(true);
//            this.MessageHistory.setWrapStyleWord(true);

            // phục vụ chat
            this.skInChat = skInChat;
            this.skOutChat = new DatagramSocket();
            this.partner_receiver_chat_port = partner_receiver_chat_port;

            this.MessageHolder.addActionListener(e -> {
                try {
                    String msg = MessageHolder.getText().trim();
                    byte[] data = msg.getBytes();
                    DatagramPacket seP = new DatagramPacket(data, data.length, partner_address, partner_receiver_chat_port);
                    addHistory("Me: "+msg);
                    MessageHolder.setText("");
                    skOutChat.send(seP);
                } catch (IOException e1){

                }
            });


            this.MessageSend.addActionListener(e -> {
                try {
                    String msg = this.MessageHolder.getText().trim();
                    byte[] data = msg.getBytes();
                    DatagramPacket seP = new DatagramPacket(data, data.length, partner_address, partner_receiver_chat_port);
                    addHistory("Me: "+msg);
                    MessageHolder.setText("");
                    skOutChat.send(seP);
                    // update lịch sử

                } catch (IOException e1){

                }
            });

            this.FileSend.addActionListener(e -> {


                JFileChooser fileChooser = new JFileChooser();

                int res = fileChooser.showOpenDialog(this);

                if (res == JFileChooser.APPROVE_OPTION){

                    String path = fileChooser.getSelectedFile().getPath();
                    String fileName = fileChooser.getSelectedFile().getName();
                    addHistory("Sending file "+fileName+" to partner");

                    fileSender = new Sender(partner_receiver_in_port, skInSender, partner_address, path,fileName);

//                    while (! fileSender.isTransferComplete){
//                    }

                    addHistory("File sent successfully!");

                }

            });

            new Thread(() -> {
                is_running_chat = true;
                boolean session = false;
                boolean isAnnounce = false;

                boolean has_prev = false;
                while (is_running_chat) {
                    if (fileReceiver == null || fileReceiver.is_done) {
                        if (has_prev){
                            addHistory("------File finish receiving------");
                        }
                        fileReceiver = new Receiver();
                        String
                                path =
                                "C:\\Users\\damti\\OneDrive - Danang University of Technology\\OneDrive - The University of Technology\\Desktop\\Study\\Doan Coso Nganh Mang\\RemoteDesktop\\src\\Client\\ReceiveFile";
                        fileReceiver.createReceiver(skInReceiver, partner_sender_in_port, partner_address, path);
                        session = true;
                        fileReceiver.is_done = true;
                        has_prev = true;
                    }
                    else if (! fileReceiver.is_done && session && ! isAnnounce){
                        addHistory("------File receiving from partner------");
                        isAnnounce = true;

                    }
//                    else if (fileReceiver.is_done && session ){
//                        addHistory("------File finish receiving------");
//                        session =false;
////                        fileReceiver = null;
////                        fileReceiver.is_done = false;
//                    }
                }
            }).start();

            setContentPane(mainPanel);
            setTitle(title);
            setSize(450, 400);
            this.MessageHistory.setEditable(false);
            setDefaultCloseOperation(HIDE_ON_CLOSE);
            setVisible(true);
        } catch (SocketException e){
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public void addHistory(String msg){
        chatHistory = chatHistory + "\n" + msg;
        MessageHistory.setText(chatHistory);
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        DatagramSocket datagramSocket = new DatagramSocket(10000);
        DatagramSocket datagramSocket1 = new DatagramSocket(10001);
        DatagramSocket datagramSocket2 = new DatagramSocket(10002);

        DatagramSocket datagramSocket3 = new DatagramSocket(10003);
        DatagramSocket datagramSocket4 = new DatagramSocket(10004);
        DatagramSocket datagramSocket5 = new DatagramSocket(10005);
        ChatAndFileTransfer u1 = new ChatAndFileTransfer(datagramSocket, datagramSocket1, InetAddress.getByName("localhost"), 10003, 10004,
                datagramSocket2, 10005, "U1");
        new Thread(u1).start();

        new Thread(new ChatAndFileTransfer(datagramSocket3, datagramSocket4, InetAddress.getByName("localhost"), 10000, 10001,
                datagramSocket5, 10002, "U2")).start();


//        new ChatAndFileTransfer()
    }


    // nếu mà đang nhận file thì ko nhận song song được
    @Override
    public void run() {
        is_running_chat = true;
        while (is_running_chat){
            byte[] data = new byte[1000];
            DatagramPacket receiver = new DatagramPacket(data, data.length);
            try {
                this.skInChat.receive(receiver);
                chatHistory = chatHistory + "\nPARTNER: "+ new String(receiver.getData()).substring(0, receiver.getLength());
                this.MessageHistory.setText(chatHistory);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }


    private void createUIComponents() {
        // TODO: place custom component creation code here

    }
}
