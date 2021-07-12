package yhames.pro.project.afis.requests;

import java.io.InputStream;
import java.io.OutputStream;

public class HandshakeRequest extends Request {
    public HandshakeRequest() {

    }
    public static HandshakeRequest read(InputStream in) {
        //TODO: Implement read() for HandshakeRequest
        return new HandshakeRequest();
    }
    @Override
    public boolean handle(OutputStream out) {
        return false;
        // TODO: Implement handle() for HandshakeRequest
    }
}
