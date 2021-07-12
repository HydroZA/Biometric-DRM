package yhames.pro.project.afis;

import yhames.pro.project.afis.Fingerprint;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseConnection {
    private Fingerprint[] fingerprints;
    private Connection db;

    private static final String url = "jdbc:sqlite:resources/fingerprints.db";

    public DatabaseConnection() {
        // connect to DB
        connect();
    }

    private void connect() {
        Connection conn = null;
        try {
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        } 
        this.db = conn;
    }

    public Fingerprint[] getFingerprints() {
        if (fingerprints == null) {
            downloadFingerprints();
        }
        return this.fingerprints;
    }

    protected void downloadFingerprints() {
        final String sqlGetFingerprints = "SELECT * FROM Fingerprints";
        final String sqlGetFingerprintsLength = "SELECT COUNT(*) FROM Fingerprints";

        try {
            if (db.isClosed()) {
                connect();
            }

            PreparedStatement size = db.prepareStatement(sqlGetFingerprintsLength);
            int len = size.executeQuery().getInt(1);

            PreparedStatement stmt = db.prepareStatement(sqlGetFingerprints);
            ResultSet rs = stmt.executeQuery();

            Fingerprint[] fps = new Fingerprint[len];
            int ptr = 0;
            while (rs.next()) {
                fps[ptr++] = new Fingerprint(
                    rs.getInt("id"),
                    rs.getBytes("image")
                );
            }

            this.fingerprints = fps;
            db.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean enroll(Fingerprint fp) {
        final String sqlEnroll = "INSERT INTO Fingerprints (image) VALUES (?)";

        try {
            if (db.isClosed()) {
                connect();
            }
            PreparedStatement stmt = db.prepareStatement(sqlEnroll);

            // Adds our byte array to the SQL query
            stmt.setBytes(1, fp.getImg());

            // execute() returns false if the DB was updated, invert it so that true indicates success
            boolean result = !stmt.execute();
            db.close();

            return result;
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
            return false;
        }
    }
}
