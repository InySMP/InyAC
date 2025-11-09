package tw.inysmp.inyac;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import tw.inysmp.inyac.data.PunishmentData;
import java.sql.*;
import java.util.logging.Level;

public class DatabaseManager {

    private final InyAC plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(InyAC plugin, FileConfiguration config) {
        this.plugin = plugin;
        initializeDataSource(config);
    }

    private void initializeDataSource(FileConfiguration config) {
        HikariConfig hikariConfig = new HikariConfig();
        String type = config.getString("database.type", "MYSQL").toUpperCase();
        
        if ("MYSQL".equals(type)) {
            String host = config.getString("database.host");
            int port = config.getInt("database.port");
            String db = config.getString("database.database");
            String user = config.getString("database.username");
            String pass = config.getString("database.password");

            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&useSSL=false");
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(pass);
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hikariConfig.setPoolName("InyAC-MySQL-Pool");
        } else if ("SQLITE".equals(type)) {
            String filePath = plugin.getDataFolder().getAbsolutePath() + "/" + config.getString("database.file", "InyAC.db");
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + filePath);
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
            hikariConfig.setPoolName("InyAC-SQLite-Pool");
        } else {
            plugin.getLogger().severe("Unknown database type: " + type);
            return;
        }

        hikariConfig.setMinimumIdle(5);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setConnectionTimeout(30000);

        try {
            this.dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database connection pool!", e);
        }
    }

    public void initializeTables() {
        if (dataSource == null) return;
        
        String type = plugin.getConfig().getString("database.type", "MYSQL").toUpperCase();
        String idType = "MYSQL".equals(type) ? "INT AUTO_INCREMENT PRIMARY KEY" : "INTEGER PRIMARY KEY AUTOINCREMENT";
        
        final String SQL_CREATE_TABLE = String.format(
            "CREATE TABLE IF NOT EXISTS inyac_punishments ("
                + "id %s,"
                + "player_uuid VARCHAR(36) NOT NULL,"
                + "player_name VARCHAR(16) NOT NULL,"
                + "punishment_type VARCHAR(10) NOT NULL,"
                + "reason VARCHAR(255) NOT NULL,"
                + "staff_uuid VARCHAR(36) NOT NULL,"
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");", idType);
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(SQL_CREATE_TABLE);
            plugin.getLogger().info("Database tables initialized successfully.");
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create database tables!", e);
        }
    }
    
    public void savePunishment(PunishmentData data) {
        if (dataSource == null) return;
        
        final String SQL_INSERT = "INSERT INTO inyac_punishments "
                + "(player_uuid, player_name, punishment_type, reason, staff_uuid) "
                + "VALUES (?, ?, ?, ?, ?)";
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
                
                ps.setString(1, data.getPlayerUuid().toString());
                ps.setString(2, data.getPlayerName());
                ps.setString(3, data.getType());
                ps.setString(4, data.getReason());
                ps.setString(5, data.getStaffUuid().toString());
                
                ps.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save punishment record for " + data.getPlayerName(), e);
            }
        });
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}