package yhames.pro.project.afis.matchers;

import yhames.pro.project.afis.Fingerprint;

import com.machinezoo.sourceafis.*;

public class SourceAFIS extends Matcher {
    private static final float threshold = 40;
    
    // Returns the fingerprint which was matched in the database, if any
    public Match search(Fingerprint fpProbe, Fingerprint[] db) {
        FingerprintMatcher matcher = new FingerprintMatcher(
            new FingerprintTemplate(
                new FingerprintImage(
                    fpProbe.getImg(),
                    new FingerprintImageOptions()
                        .dpi(500))));
        
        double highScore = 0;
        Match match = new Match(false);
        for (Fingerprint candidate : db) {
            double score = matcher.match(convertFingerprintToTemplate(candidate));
            if (score > highScore)
            {
                highScore = score;
                if (score > threshold) {
                    match = new Match(true, candidate, highScore);
                }
            }
        }
        return match;
    }

    public Match match (Fingerprint probe, Fingerprint candidate) {
        return null;
    }

    private static FingerprintTemplate convertFingerprintToTemplate (Fingerprint fp) {
        return new FingerprintTemplate(
            new FingerprintImage(
                fp.getImg(), 
                new FingerprintImageOptions()
                    .dpi(500))
        );
    }
}




