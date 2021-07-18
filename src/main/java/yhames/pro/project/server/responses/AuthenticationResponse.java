package yhames.pro.project.server.responses;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/*
    AUTHENTICATION RESPONSE PROTOCOL:
    |-----------|-------------------|
    1 byte      | 44 bytes          |
    isAuthorized| decryption key    |
    0x00 = true |                   |
    else false  |
 */

public class AuthenticationResponse extends Response {
    private String decryptionKey;
    private boolean isAuthorized;

    public AuthenticationResponse(boolean isAuthorized, String decryptionKey) {
        this.decryptionKey = decryptionKey;
        this.isAuthorized = isAuthorized;
    }

    private byte[] serialize() {
        if (isAuthorized) {
            byte[] serialized = new byte[45];
            serialized[0] = 0x00;

            byte[] key = decryptionKey.getBytes(StandardCharsets.UTF_8);

            System.arraycopy(
                    key,
                    0,
                    serialized,
                    1,
                    key.length
            );

            return serialized;
        }
        else {
            return new byte[] { 0x01 };
        }
    }

    @Override
    public boolean send(OutputStream out) throws IOException {
        out.write(this.serialize());
        return true;
    }
}
