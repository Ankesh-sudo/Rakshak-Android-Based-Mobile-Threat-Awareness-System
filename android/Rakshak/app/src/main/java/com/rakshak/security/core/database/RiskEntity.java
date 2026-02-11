package com.rakshak.security.core.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "risk_history")
public class RiskEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String type;      // SMS, CALL, FILE, LINK
    public String source;    // sender number, file name
    public int score;
    public String level;
    public long timestamp;

    public RiskEntity(String type,
                      String source,
                      int score,
                      String level,
                      long timestamp) {
        this.type = type;
        this.source = source;
        this.score = score;
        this.level = level;
        this.timestamp = timestamp;
    }
}
