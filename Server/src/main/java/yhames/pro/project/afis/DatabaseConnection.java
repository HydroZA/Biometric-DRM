package yhames.pro.project.afis;

import yhames.pro.project.afis.Fingerprint;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;

public class DatabaseConnection {
    private Fingerprint[] fingerprints;
    private Connection db;

    private static final String url = "jdbc:sqlite:/Users/james/ownCloud/University/Masters/Project/Code/Stable/Server/resources/fingerprints.db";

    public DatabaseConnection() {
        // connect to DB
        db = connect();
    }

    private Connection connect() {
        Connection conn = null;
        try {
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        } 
        return conn;
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
        final String sqlGetFingerprints = "SELECT * FROM Fingerprints";
        final String sqlGetFingerprintsLength = "SELECT COUNT(*) FROM Fingerprints";

        try {
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
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
