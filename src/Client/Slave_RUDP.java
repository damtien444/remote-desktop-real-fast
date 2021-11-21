package Client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Slave_RUDP extends Thread{
    // TODO: Khởi tạo các socket
    // TODO: Khởi tạo thread in
    // TODO: Khởi tạo thread out
    // TODO: Cache

    static int data_size = 988;  // Total 1000 Bytes -> checksum: 8, seqNum: 4, data<=988
    static ArrayList<byte[]> cache = new ArrayList<>(100000); // cache 100000 packet


    public Slave_RUDP(InetAddress recAddress, int recPort){
        Screen screen;
        DatagramSocket skIn, skOut;

        try {
            skOut = new DatagramSocket();           // outgoing channel
            skIn = new DatagramSocket(recPort);     // incomming channel
            screen = new Screen();                  // create and start capture current screen

            // TODO: Khởi tạo hai thread in, out và xử lý lỗi
//            SlaveInThread th_in = new Thread()

        } catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        super.run();
    }

    // TODO: Đây là lớp đóng vai trò controller, nhận Giữ một cache gồm các packet gần nhất, hoặc

}



class SlaveInThread extends Thread{
    // TODO: Nhận các action của master
    // Sử dụng UDP thông thường
    // Báo đến lostsolve để xử lý lỗi packet
    // ghi nhận các action của master
}

class SlaveLostSolveThread extends Thread{
    // TODO: Gọi đến khi slave in nhận được yêu cầu gửi lại
    // Chứa một danh sách các packet lỗi để thêm (nhận yêu cầu từ in), kiểm soát, gửi lại và xóa nếu cần
    // truy cập đến bộ nhớ cache của controller để tìm packet đã mất tương ứng để gửi lại.
    // Mỗi packet gửi lại sẽ có một bộ đếm, nếu quá giờ ko thấy yêu cầu lại thì ko gửi nữa và xóa đi khỏi danh sách lỗi.
}

class SlaveOutThread extends Thread{
    // TODO: Gửi các packet từ hàng đợi gửi
    // Sử dụng hybrid RUDP:
    // Chứa danh sách các packet cần gửi được chuẩn bị bởi controller.

    DatagramSocket skOut;
    public SlaveOutThread (DatagramSocket skOut){
        this.skOut = skOut;
    }

    @Override
    public void run() {
        super.run();



    }
}

class Screen extends Thread{
    Robot robot;
    static Rectangle screenRec = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    public static BufferedImage current_screen;

    @Override
    public void run() {
        super.run();
        current_screen = robot.createScreenCapture(screenRec);
    }

    public Screen() throws AWTException {
        this.robot = new Robot();
        this.start();
    }

    public BufferedImage getScreen(){
        return current_screen;
    }
}

