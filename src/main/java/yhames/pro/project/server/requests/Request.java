package yhames.pro.project.server.requests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Request {
    public abstract boolean handle(OutputStream out) throws Exception;

    /*
        COMMUNICATION PROTOCOL:
        |-----------------------------------|------------------------
        client sends : 1 byte               |
        request type                        |
        0x00 = handshake                    | -> return HandshakeResponse
        0x01 = match                        | -> return MatchResponse
        0x02 = enroll                       | -> return EnrollResponse
        0x03 = delete                       | -> return DeleteResponse
        0x04 = authentication               | -> return AuthenticationResponse
        0x05 = reserved                     |
     */

    public abstract Request read(InputStream in) throws IOException;

    public static Request getRequest(InputStream in) throws IOException {
        // read request type
        byte type = in.readNBytes(1)[0];

        switch(type) {
            case 0x00 -> {
                System.out.println("Client is requesting a handshake");
                return new HandshakeRequest().read(in);
            }
            case 0x01 -> {
                System.out.println("Client is requesting a fingerprint match");
                return new MatchRequest().read(in);
            }
            case 0x02 -> {
                System.out.println("Client is requesting to enroll a fingerprint");
                return new EnrollRequest().read(in);
            }
            case 0x05 -> {
                System.out.println("Client is requesting authentication");
                return new AuthenticationRequest().read(in);
            }
            default -> {
                // TODO: Implement the rest of the request types
                return null;
            }
        }
    }
}
