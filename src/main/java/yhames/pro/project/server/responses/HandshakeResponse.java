package yhames.pro.project.server.responses;

import java.io.IOException;
import java.io.OutputStream;

public class HandshakeResponse extends Response {
    public HandshakeResponse () {

    }

    public boolean send(OutputStream out) throws IOException {
        out.write(new byte[] { 0x00 });
        return true;
    }
}
