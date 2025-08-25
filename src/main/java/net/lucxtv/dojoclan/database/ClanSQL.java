package net.lucxtv.dojoclan.database;

import net.lucxtv.dojoclan.DojoClan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ClanSQL {

    private final DojoClan plugin;
    private Connection connection;

    public ClanSQL(DojoClan plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        String host = plugin.getConfig().getString("database.host");
        int port = plugin.getConfig().getInt("database.port");
        String database = plugin.getConfig().getString("database.database");
        String username = plugin.getConfig().getString("database.user");
        String password = plugin.getConfig().getString("database.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";

        try {
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Erfolgreich mit MySQL verbunden.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler bei der Verbindung zu MySQL:");
            e.printStackTrace();
        }
    }

    public void createTables() {
        if (connection == null) {
            plugin.getLogger().severe("No Connection, Tabellen können nicht erstellt werden.");
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clans (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(32) NOT NULL," +
                    "owner_uuid VARCHAR(36) NOT NULL" +
                    ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clan_members (" +
                    "clan_id INT NOT NULL," +
                    "member_uuid VARCHAR(36) NOT NULL," +
                    "FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clan_invites (" +
                    "clan_id INT NOT NULL," +
                    "invited_uuid VARCHAR(36) NOT NULL," +
                    "invited_by VARCHAR(36) NOT NULL," +
                    "timestamp BIGINT NOT NULL," +
                    "FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");

            plugin.getLogger().info("MySQL-Tabellen erfolgreich erstellt.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Erstellen der Tabellen!");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("MySQL-Verbindung getrennt.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
}