package yhames.pro.project.afis.matchers;

import yhames.pro.project.afis.Fingerprint;

public class Match {
    private boolean isMatch;
    private Fingerprint fingerprint;
    private double score;

    public Match (boolean im, Fingerprint fp, double score) {
        this.isMatch = im;
        this.fingerprint = fp;
        this.score = score;
    }
    public Match(boolean im) {
        this.isMatch = im;
    }

    public boolean isMatch() {
        return isMatch;
    }
    public void setMatch(boolean isMatch) {
        this.isMatch = isMatch;
    }
    public Fingerprint getFingerprint() {
        return fingerprint;
    }
    public void setFingerprint(Fingerprint fingerprint) {
        this.fingerprint = fingerprint;
    }
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }

    
}