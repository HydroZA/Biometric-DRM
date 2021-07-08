package yhames.pro.project.afis.matchers;

import yhames.pro.project.afis.Fingerprint;

public abstract class Matcher {
    float threshold;
    public abstract Match search (Fingerprint probe, Fingerprint[] db);
    public abstract Match match (Fingerprint probe, Fingerprint candidate);
}

