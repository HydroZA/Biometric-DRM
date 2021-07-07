package yhames.pro.project.afis.matchers;

import yhames.pro.project.afis.Fingerprint;

public abstract class Matcher {
    float threshold;
    public abstract boolean match (Fingerprint probe);
}

