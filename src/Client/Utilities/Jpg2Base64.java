package Client.Utilities;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class Jpg2Base64 {
    public static String encoder(BufferedImage image) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        baos.flush();
        byte[] bs = baos.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(bs);
        baos.close();
        return base64Image;
    }
    public static BufferedImage decoder(String data) throws IOException {
        byte[] bs = Base64.getDecoder().decode(data);
        ByteArrayInputStream bais = new ByteArrayInputStream(bs);
        BufferedImage ret = ImageIO.read(bais);
        bais.close();
        return ret;
    }
}
