package yhames.pro.project.afis.requests;

import yhames.pro.project.afis.DatabaseConnection;
import yhames.pro.project.afis.Fingerprint;
import yhames.pro.project.afis.matchers.Match;
import yhames.pro.project.afis.responses.AuthenticationResponse;
import yhames.pro.project.afis.responses.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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

    private String getDecryptionKey() {
        //TODO:
        return "DecryptionKeyFam";
    }

    @Override
    public boolean handle(OutputStream out) throws Exception {
        Match match = super.performMatch();
        Response response;
        // TODO: Can this mess of if statements be simplified?
        if (match.isMatch()) {
            boolean authorized = checkIfUserHasAuthorizationForSoftware(match);
            if (authorized) {
                String key = getDecryptionKey();
                response = new AuthenticationResponse(true, key);
            }
            else {
                response = new AuthenticationResponse(false, "");
            }
        }
        else {
            response = new AuthenticationResponse(false, "");
        }

        return response.send(out);
    }
}
