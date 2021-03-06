package yhames.pro.project.afis.requests;

import yhames.pro.project.afis.responses.HandshakeResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// The handshake request is used by clients to make sure the server is running
// A single byte is sent by the client (0x00) and if the server is running then a 0x00 is returned

public class HandshakeRequest extends Request {
    private HandshakeRequest() {

    }
    public static HandshakeRequest read(InputStream in) {
        return new HandshakeRequest();
    }

    @Override
    public boolean handle(OutputStream out) throws IOException {
       return new HandshakeResponse().send(out);
    }
}
