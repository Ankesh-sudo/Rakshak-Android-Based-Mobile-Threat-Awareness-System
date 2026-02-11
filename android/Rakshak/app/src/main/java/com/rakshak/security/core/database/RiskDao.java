package com.rakshak.security.core.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RiskDao {

    @Insert
    void insert(RiskEntity entity);

    @Query("SELECT * FROM risk_history ORDER BY timestamp DESC")
    List<RiskEntity> getAll();

    @Query("DELETE FROM risk_history")
    void clearAll();
}
