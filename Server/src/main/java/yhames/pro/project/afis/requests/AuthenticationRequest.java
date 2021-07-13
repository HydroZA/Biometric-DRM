package yhames.pro.project.afis.requests;

import yhames.pro.project.afis.Fingerprint;
import yhames.pro.project.afis.matchers.Match;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class AuthenticationRequest extends MatchRequest {
    private String softwareToCheckLicenseFor;

    public AuthenticationRequest(String softwareName) {
        super();
        this.softwareToCheckLicenseFor = softwareName;
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

        this.softwareToCheckLicenseFor = new String(in.readNBytes(len));

        // The latter part of an authentication request is the same as a MatchRequest
        super.read(in);

        return this;
    }

    @Override
    public boolean handle(OutputStream out) throws IOException {
        return false;
    }
}
