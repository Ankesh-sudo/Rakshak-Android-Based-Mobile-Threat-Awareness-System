package com.rakshak.security.core.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "risk_history")
public class RiskEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String type;        // SMS, CALL, FILE, LINK
    public String source;      // sender number, file name
    public int score;
    public String level;
    public long timestamp;

    // ✅ NEW: Store full message / details
    public String message;

    public RiskEntity(String type,
                      String source,
                      int score,
                      String level,
                      long timestamp,
                      String message) {

        this.type = type;
        this.source = source;
        this.score = score;
        this.level = level;
        this.timestamp = timestamp;
        this.message = message;
    }
}
