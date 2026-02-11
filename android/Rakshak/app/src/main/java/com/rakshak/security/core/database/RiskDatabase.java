package com.rakshak.security.core.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {RiskEntity.class}, version = 1)
public abstract class RiskDatabase extends RoomDatabase {

    private static RiskDatabase instance;

    public abstract RiskDao riskDao();

    public static synchronized RiskDatabase getInstance(Context context) {

        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    RiskDatabase.class,
                    "rakshak_risk_db"
            ).build();
        }

        return instance;
    }
}
