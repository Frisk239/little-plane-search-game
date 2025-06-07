package com.frame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RankingDB {
    private static final String DB_URL = "jdbc:h2:./db/game_ranking;AUTO_SERVER=TRUE";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS rankings (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "player_name VARCHAR(50) NOT NULL," +
            "score INT NOT NULL," +
            "game_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    
    private Connection conn;

    public RankingDB() {
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection(DB_URL);
            initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        }
    }

    public void addScore(String playerName, int score) {
        String sql = "INSERT INTO rankings (player_name, score) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<RankingEntry> getTopScores(int limit) {
        List<RankingEntry> rankings = new ArrayList<>();
        String sql = "SELECT player_name, score, game_time FROM rankings ORDER BY score DESC LIMIT ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                rankings.add(new RankingEntry(
                    rs.getString("player_name"),
                    rs.getInt("score"),
                    rs.getTimestamp("game_time")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return rankings;
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class RankingEntry {
        public final String playerName;
        public final int score;
        public final Timestamp gameTime;

        public RankingEntry(String playerName, int score, Timestamp gameTime) {
            this.playerName = playerName;
            this.score = score;
            this.gameTime = gameTime;
        }
    }
}
