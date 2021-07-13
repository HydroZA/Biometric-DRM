package yhames.pro.project.afis.requests;

import yhames.pro.project.afis.DatabaseConnection;
import yhames.pro.project.afis.Fingerprint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/*
    EnrollRequest messages take the following format:

    |---------|--------------|----------------------|
    0x02      |  4 bytes = n | n bytes
              |  length      | msg body
              ^
              |
      This class reads from here onwards
 */


public class EnrollRequest extends Request {
    private final Fingerprint toEnroll;
    private EnrollRequest(Fingerprint fp){
        this.toEnroll = fp;
    }

    public static EnrollRequest read(InputStream in) throws IOException {
        // Read the message length
        byte[] lenBytes = in.readNBytes(4);
        int len = ByteBuffer.wrap(lenBytes).getInt();

        // Read the image body
        byte[] img = in.readNBytes(len);

        return new EnrollRequest(new Fingerprint(img));
    }

    @Override
    public boolean handle(OutputStream out) {
        DatabaseConnection db = new DatabaseConnection();
        boolean result = db.enroll(toEnroll);


        return result;
    }
}
