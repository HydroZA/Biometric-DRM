package yhames.pro.project.afis;

import yhames.pro.project.afis.Fingerprint;

import javax.management.openmbean.OpenDataException;
import javax.naming.OperationNotSupportedException;
import java.io.IOException;
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

    // TODO: Remove all these try/catch blocks and change the method headers to throw them instead
    // The server shouldn't crash when we encounter an issue dealing with one clients request
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

    private int getIDFromSoftwareTitle(String title) throws SQLException {
        String sqlGetIDFromSoftwareName = String.format(
                "SELECT id FROM Software WHERE title=\"%s\"",
                title
        );

        PreparedStatement stmt = db.prepareStatement(sqlGetIDFromSoftwareName);
        ResultSet rs = stmt.executeQuery();

        return rs.getInt(1);
    }
    public boolean checkAuthorization(Fingerprint fingerprint, int softwareID) throws SQLException {
        String sqlCheckIfLicense = String.format(
                "SELECT COUNT(1) FROM Licenses WHERE fingerprint=%o AND software=%o",
                fingerprint.getId(),
                softwareID
        );

        PreparedStatement stmt = db.prepareStatement(sqlCheckIfLicense);
        ResultSet rs = stmt.executeQuery();

        // The query will return 1 if the entry exists otherwise 0
        return rs.getInt(1) == 1;
    }
    public boolean checkAuthorization(Fingerprint fingerprint, String softwareTitle) throws SQLException {
        int softwareID = getIDFromSoftwareTitle(softwareTitle);
        return checkAuthorization(fingerprint, softwareID);
    }
}
