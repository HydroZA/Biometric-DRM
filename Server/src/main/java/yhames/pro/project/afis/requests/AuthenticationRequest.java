package yhames.pro.project.afis.requests;

import yhames.pro.project.afis.Fingerprint;
import yhames.pro.project.afis.matchers.Match;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AuthenticationRequest extends Request {
    private String softwareToCheckLicenseFor;
    private MatchRequest matchRequest;

    private AuthenticationRequest(String softwareName, MatchRequest mr) {
        this.softwareToCheckLicenseFor = softwareName;
        this.matchRequest = mr;
    }

    /*
        AUTHENTICATION REQUEST PROTOCOL:
        |--------|---------------------|
        0x05     | 4 bytes             |
                 | softwareName Length |
     */
    public static AuthenticationRequest read(InputStream in) throws IOException {
        // The latter part of an authentication request is the same as a MatchRequest
        MatchRequest mr = MatchRequest.read(in);

    }

    @Override
    public boolean handle(OutputStream out) throws IOException {
        return false;
    }
}
