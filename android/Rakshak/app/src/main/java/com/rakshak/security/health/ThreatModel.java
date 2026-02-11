package com.rakshak.security.health;

import java.io.Serializable;

public class ThreatModel implements Serializable {

    public String name;
    public String path;
    public String reason;
    public long size;

    public ThreatModel(String name,
                       String path,
                       String reason,
                       long size) {
        this.name = name;
        this.path = path;
        this.reason = reason;
        this.size = size;
    }
}
