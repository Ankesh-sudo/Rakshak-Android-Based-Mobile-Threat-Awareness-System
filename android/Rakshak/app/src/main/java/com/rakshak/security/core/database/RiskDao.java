package com.rakshak.security.core.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RiskDao {

    // ===============================
    // Insert
    // ===============================
    @Insert
    void insert(RiskEntity entity);

    // ===============================
    // Get All (Latest First)
    // ===============================
    @Query("SELECT * FROM risk_history ORDER BY timestamp DESC")
    List<RiskEntity> getAll();

    // ===============================
    // Delete Single Entry
    // ===============================
    @Delete
    void delete(RiskEntity entity);

    // Optional: Delete by ID
    @Query("DELETE FROM risk_history WHERE id = :id")
    void deleteById(int id);

    // ===============================
    // Clear All History
    // ===============================
    @Query("DELETE FROM risk_history")
    void clearAll();
}
