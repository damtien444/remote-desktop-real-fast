package Client.Utilities;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Utilities {
    public static byte[] generatePacket(int seqNum, byte[] dataBytes){
        // Đổi số thứ tự thành byte array
        byte[] seqNumBytes = ByteBuffer.allocate(4).putInt(seqNum).array();

        // Sinh checksum
        CRC32 checksum = new CRC32();
        checksum.update(seqNumBytes);
        checksum.update(dataBytes);
        byte[] checksumBytes = ByteBuffer.allocate(8).putLong(checksum.getValue()).array();

        // Sinh packet
        ByteBuffer pktBuf = ByteBuffer.allocate(8+4+ dataBytes.length);
        pktBuf.put(checksumBytes);
        pktBuf.put(seqNumBytes);
        pktBuf.put(dataBytes);
        return pktBuf.array();
    }

//    public static int isNormalPacket(byte[] pkt){
//        byte[] received_checksumBytes = copyOfRange(pkt, 0, 8);
//        byte[] akcNumBytes = copyOfRange(pkt, 8, 12);
//        CRC32 checksum = new CRC32();
//        checksum.update(ack);
//    }

    public static byte[] copyOfRange(byte[] srcArr, int start, int end){
        int length = (end > srcArr.length)? srcArr.length-start: end-start;
        byte[] destArr = new byte[length];
        System.arraycopy(srcArr, start, destArr, 0, length);
        return destArr;
    }
}
