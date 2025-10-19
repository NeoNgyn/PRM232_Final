package com.example.final_project.utils;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBMigrationHelper {
    private static final String TAG = "DBMigrationHelper";

    /**
     * Normalize Menu.menu_id values that are not of format M### (3 digits).
     * This will assign new sequential IDs M001,M002,... to those rows
     * ordered by create_at, and update referencing RecipeInMenu.menu_id.
     * Returns number of rows changed.
     */
    public static int normalizeMenuIds(Connection conn) throws SQLException {
        if (conn == null) return 0;
        // Check if there are any invalid menu ids
        String countSql = "SELECT COUNT(*) AS cnt FROM Menu WHERE NOT (menu_id REGEXP '^M[0-9]{3}$')";
        try (PreparedStatement cstmt = conn.prepareStatement(countSql)) {
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    if (cnt <= 0) return 0; // nothing to do
                }
            }
        }

        // We'll perform updates in a transaction
        boolean previousAuto = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // find starting number from valid IDs
            String maxSql = "SELECT IFNULL(MAX(CAST(SUBSTRING(menu_id,2) AS UNSIGNED)), 0) AS maxnum FROM Menu WHERE menu_id REGEXP '^M[0-9]{1,}$'";
            int start = 0;
            try (PreparedStatement mstmt = conn.prepareStatement(maxSql)) {
                try (ResultSet rs = mstmt.executeQuery()) {
                    if (rs.next()) start = rs.getInt("maxnum");
                }
            }

            // Select bad ids ordered by create_at
            String listSql = "SELECT menu_id FROM Menu WHERE NOT (menu_id REGEXP '^M[0-9]{3}$') ORDER BY create_at ASC";
            try (PreparedStatement lstmt = conn.prepareStatement(listSql)) {
                try (ResultSet rs = lstmt.executeQuery()) {
                    int changed = 0;
                    while (rs.next()) {
                        String oldId = rs.getString("menu_id");
                        start++;
                        String newId = String.format(java.util.Locale.US, "M%03d", start);

                        // Update RecipeInMenu references first
                        try (PreparedStatement upRef = conn.prepareStatement("UPDATE RecipeInMenu SET menu_id = ? WHERE menu_id = ?")) {
                            upRef.setString(1, newId);
                            upRef.setString(2, oldId);
                            upRef.executeUpdate();
                        }

                        // Update Menu primary key row
                        try (PreparedStatement upMenu = conn.prepareStatement("UPDATE Menu SET menu_id = ? WHERE menu_id = ?")) {
                            upMenu.setString(1, newId);
                            upMenu.setString(2, oldId);
                            int affected = upMenu.executeUpdate();
                            if (affected <= 0) {
                                throw new SQLException("Failed to update Menu id from " + oldId + " to " + newId);
                            }
                        }
                        changed++;
                    }
                    conn.commit();
                    Log.i(TAG, "Normalized menu ids, changed=" + changed);
                    return changed;
                }
            }
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException e) { Log.e(TAG, "Rollback failed", e); }
            throw ex;
        } finally {
            try { conn.setAutoCommit(previousAuto); } catch (SQLException ignored) {}
        }
    }
}

