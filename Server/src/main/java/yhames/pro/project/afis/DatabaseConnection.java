package yhames.pro.project.afis;

import yhames.pro.project.afis.Fingerprint;

public class DatabaseConnection {
    private Fingerprint[] fingerprints;

    public DatabaseConnection() {
        // connect to DB
    }

    public Fingerprint[] getFingerprints() {
        if (fingerprints == null) {
            downloadFingerprints();
            return this.fingerprints;
        }
        else {
            return this.fingerprints;
        }
    }

    protected void downloadFingerprints() {
        throw new UnsupportedOperationException();
    }
}
