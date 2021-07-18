package yhames.pro.project.server.responses;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/*
    AUTHENTICATION RESPONSE PROTOCOL:
    |-----------|-------------------|--------------|
    1 byte      | 4 bytes           | n bytes
    isAuthorized| decryption key len| decryption key
    0x00 = true |                   |
    else false  |
 */

public class AuthenticationResponse extends Response {
    private SecretKey decryptionKey;
    private boolean isAuthorized;

    public AuthenticationResponse(boolean isAuthorized, SecretKey decryptionKey) {

        this.decryptionKey = decryptionKey;
        this.isAuthorized = isAuthorized;
    }
    public AuthenticationResponse(boolean isAuthorized) {
        this.isAuthorized = false;
    }

    private byte[] serialize() {
        if (isAuthorized) {


            byte[] key = decryptionKey.getEncoded();

            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            int keyLen = key.length;
            lengthBuffer.putInt(keyLen);
            byte[] lengthBytes = lengthBuffer.array();

            byte[] serialized = new byte[1 + 4 + keyLen];

            // Add successful authorization
            serialized[0] = 0x00;

            // Add the decryption key length
            System.arraycopy(
                    lengthBytes,
                    0,
                    serialized,
                    1,
                    lengthBytes.length
            );

            // Add the decryption key
            System.arraycopy(
                    key,
                    0,
                    serialized,
                    5,
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
