package com.rakshak.security.core.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {RiskEntity.class}, version = 2)
public abstract class RiskDatabase extends RoomDatabase {

    private static volatile RiskDatabase instance;

    public abstract RiskDao riskDao();

    public static RiskDatabase getInstance(Context context) {

        if (instance == null) {
            synchronized (RiskDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    RiskDatabase.class,
                                    "rakshak_risk_db"
                            )
                            // ✅ Important: Recreate DB when schema changes
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return instance;
    }
}
