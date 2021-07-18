package yhames.pro.project.server.requests;

import yhames.pro.project.server.DatabaseConnection;
import yhames.pro.project.server.matchers.Match;
import yhames.pro.project.server.responses.AuthenticationResponse;
import yhames.pro.project.server.responses.Response;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.SQLException;

public class AuthenticationRequest extends MatchRequest {
    private String softwareToCheckLicenseFor;

    public AuthenticationRequest() {
        super();
    }

    public String getSoftwareToCheckLicenseFor() {
        return softwareToCheckLicenseFor;
    }

    /*
            AUTHENTICATION REQUEST PROTOCOL:
            |--------|---------------------|----------------|------->
            0x05     | 4 bytes             | n bytes        | MatchRequest
                     | softwareName Length | softwareName   |
         */
    public AuthenticationRequest read(InputStream in) throws IOException {
        // Read the name length
        byte[] lenBytes = in.readNBytes(4);
        int len = ByteBuffer.wrap(lenBytes).getInt();

        this.softwareToCheckLicenseFor = new String(in.readNBytes(len)).strip();

        // The latter part of an authentication request is the same as a MatchRequest
        super.read(in);

        return this;
    }

    private boolean checkIfUserHasAuthorizationForSoftware(Match match) throws SQLException {
        // Sanity check to ensure we don't give authorization for a fingerprint that doesn't match
        // under any circumstances
        if (!match.isMatch())
            return false;

        return new DatabaseConnection()
                .checkAuthorization(
                        match.getFingerprint(),
                        this.softwareToCheckLicenseFor
                );
    }

    public SecretKey getDecryptionKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        // TODO: Verify that this key is the same each time given a fingerprint
        return getAESKeyWithSalt(super.getProbe().getImg());
    }

    // AES key derived from a password
    private static SecretKey getAESKeyWithSalt(byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // iterationCount = 65536
        // keyLength = 256
        KeySpec spec = new PBEKeySpec(null, salt, 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    @Override
    public boolean handle(OutputStream out) throws Exception {
        Match match = super.performMatch();
        Response response;
        // TODO: Can this mess of if statements be simplified?
        if (match.isMatch()) {
            boolean authorized = checkIfUserHasAuthorizationForSoftware(match);
            if (authorized) {
                SecretKey key = getDecryptionKey();
                response = new AuthenticationResponse(true, key);
            }
            else {
                response = new AuthenticationResponse(false);
            }
        }
        else {
            response = new AuthenticationResponse(false);
        }

        return response.send(out);
    }
}
