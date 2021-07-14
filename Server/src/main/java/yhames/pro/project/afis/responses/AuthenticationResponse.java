package yhames.pro.project.afis.responses;

import yhames.pro.project.afis.matchers.Match;

import java.io.IOException;
import java.io.OutputStream;

/*
    AUTHENTICATION RESPONSE PROTOCOL:
    |-----------|-------------------|------------>
    1 byte      | ? bytes           | MatchResponse
    isAuthorized| decryption key    |
 */

public class AuthenticationResponse extends MatchResponse {
    private String decryptionKey;

    public AuthenticationResponse(Match match, String decryptionKey) {
        super(match);
        this.decryptionKey = decryptionKey;
    }

    private byte[] serialize() {
        //TODO:
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean send(OutputStream out) throws IOException {
        //TODO:
        throw new UnsupportedOperationException();
    }
}
