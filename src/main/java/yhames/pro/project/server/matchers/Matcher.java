package yhames.pro.project.server.matchers;

import yhames.pro.project.server.Fingerprint;

public abstract class Matcher {
    float threshold;
    public abstract Match search (Fingerprint probe, Fingerprint[] db);
    public abstract Match match (Fingerprint probe, Fingerprint candidate);
}

