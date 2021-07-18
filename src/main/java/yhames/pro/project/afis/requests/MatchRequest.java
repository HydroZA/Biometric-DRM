package yhames.pro.project.afis.requests;

import yhames.pro.project.afis.ComparisonMethod;
import yhames.pro.project.afis.DatabaseConnection;
import yhames.pro.project.afis.Fingerprint;
import yhames.pro.project.afis.responses.MatchResponse;
import yhames.pro.project.afis.matchers.Match;
import yhames.pro.project.afis.matchers.SourceAFIS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;

public class MatchRequest extends Request {
    private ComparisonMethod method;
    private Fingerprint probe;

    public MatchRequest() {
    }

    private MatchRequest (ComparisonMethod m, Fingerprint fp) {
        this.method = m;
        this.probe = fp;
    }

    public ComparisonMethod getMethod() {
        return method;
    }

    public void setMethod(ComparisonMethod method) {
        this.method = method;
    }

    public Fingerprint getProbe() {
        return probe;
    }

    public void setProbe(Fingerprint probe) {
        this.probe = probe;
    }

/* Clients request matches in the following format:
|-----------|--------|------------------------|--------------|
1 byte      |1 byte            |  4 bytes      | n bytes
0x01        |method            |  body length  | body
            0x00 = sourceafis |                |
            0x01 = ssim       |
            0x02 = mse        |
    */
    public MatchRequest read(InputStream in) throws IOException {
        // Read the method
        this.method = ComparisonMethod.values()[in.readNBytes(1)[0]];

        // Read the message length
        byte[] lenBytes = in.readNBytes(4);
        int len = ByteBuffer.wrap(lenBytes).getInt();

        // Read the image body
        this.probe = new Fingerprint(in.readNBytes(len));

        return this;
    }

    public Match performMatch() {
        DatabaseConnection db = new DatabaseConnection();

        Match result;
        switch (this.getMethod()) {
            case SOURCE_AFIS -> {
                System.out.println("SourceAFIS Method chosen by Client");
                result = new SourceAFIS()
                        .search(
                                this.getProbe(),
                                db.getFingerprints()
                        );
            }
            case MSE -> {
                // TODO: Implement MSE
                System.out.println("Mean Squared Error Method chosen by Client");
                result = new Match(false);
            }
            case SSIM -> {
                // TODO: Implement SSIM
                System.out.println("Structural Similarity Index Method chosen by client");
                result = new Match(false);
            }
            default -> {
                return null;
            }
        }
        return result;
    }

    @Override
    public boolean handle(OutputStream out) throws Exception {
        Match result = performMatch();

        if (result.isMatch()) {
            System.out.println("MatchRequest succeeded with score: " + result.getScore());
        } else {
            System.out.println("MatchRequest failed with score: " + result.getScore());
        }

        // Respond to the client
        MatchResponse response = new MatchResponse(result);
        return response.send(out);
    }
}
