package tw.inysmp.inyac;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import tw.inysmp.inyac.checks.CheckManager;
import tw.inysmp.inyac.data.PlayerData;

import java.io.File; // <--- 修正: 導入 java.io.File
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class InyAC extends JavaPlugin {

    private static InyAC instance;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private DatabaseManager databaseManager;
    private CheckManager checkManager; 
    
    // messages.yml 相關 (修正: 將其定義為類別成員變數)
    private File customConfigFile;
    private FileConfiguration customConfig;

    // 定義插件版本號和前綴 (用於控制台啟動訊息)
    private final String VERSION = this.getDescription().getVersion();
    private final String CONSOLE_PREFIX = "&6[&b&lInyAC&6] &a";

    @Override
    public void onEnable() {
        instance = this;
        
        // 插件啟動訊息
        sendConsole("&a----------------------------------------");
        sendConsole(CONSOLE_PREFIX + "反作弊插件 &ev" + VERSION + " &a正在啟動...");
        sendConsole("&a----------------------------------------");
        
        // 1. 載入配置 (config.yml)
        saveDefaultConfig(); 
        
        // 2. 載入 messages.yml
        loadMessagesConfig();

        // 3. 初始化數據庫
        if (getConfig().getBoolean("database.enabled", true)) {
            try {
                this.databaseManager = new DatabaseManager(this, getConfig()); 
                this.databaseManager.initializeTables();
                sendConsole(CONSOLE_PREFIX + "數據庫連接已建立。"); // <--- 修正: 使用 sendConsole()
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to connect to database! Disabling plugin.", e);
                sendConsole(CONSOLE_PREFIX + "&c數據庫連接失敗，正在關閉插件..."); // <--- 修正: 使用 sendConsole()
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        // 4. 註冊檢查和事件
        this.checkManager = new CheckManager(this);
        this.checkManager.registerChecks(); 
        
        // 5. 註冊指令
        getCommand("inyac").setExecutor(new InyACCommand(this));
        
        sendConsole(CONSOLE_PREFIX + "&aInyAC v" + VERSION + " 啟動完成。"); // <--- 修正: 使用 sendConsole()
        sendConsole("&a----------------------------------------");
    }

    @Override
    public void onDisable() {
        // 6. 關閉數據庫連接
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        playerDataMap.clear();
        instance = null;
        
        sendConsole(CONSOLE_PREFIX + "&c插件正在關閉..."); // <--- 修正: 使用 sendConsole()
        sendConsole("&a----------------------------------------");
    }
    
    // ... (其他 Getter/Setter 保持不變)
    public static InyAC getInstance() {
        return instance;
    }
    
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(k));
    }
    
    public DatabaseManager getDatabaseManager() { return databaseManager; }

    // ----------------------------------------------------
    // Messages.yml 相關方法 (修正: 消除 messageFile/messageConfig 的符號錯誤)
    // ----------------------------------------------------
    
    public FileConfiguration getMessagesConfig() {
        if (this.customConfig == null) {
            // 注意: 這裡不能直接調用 loadConfiguration，因為 File 對象可能為空
            this.loadMessagesConfig(); 
        }
        return this.customConfig;
    }

    public void loadMessagesConfig() {
        this.customConfigFile = new File(getDataFolder(), "messages.yml"); // <--- 修正: 正確使用成員變數
        
        if (!this.customConfigFile.exists()) {
            this.customConfigFile.getParentFile().mkdirs();
            saveResource("messages.yml", false); 
        }

        this.customConfig = YamlConfiguration.loadConfiguration(this.customConfigFile); // <--- 修正: 正確使用成員變數

        try (InputStream defaultStream = getResource("messages.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                this.customConfig.setDefaults(defaultConfig);
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load messages.yml defaults", e);
        }
    }
    
    public void reloadMessagesConfig() {
        this.customConfig = YamlConfiguration.loadConfiguration(this.customConfigFile);
        try (InputStream defaultStream = getResource("messages.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                this.customConfig.setDefaults(defaultConfig);
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not reload messages.yml defaults", e);
        }
    }
    
    // ----------------------------------------------------
    // 新增: sendConsole 輔助方法
    // ----------------------------------------------------
    /**
     * 向伺服器控制台發送帶有顏色代碼的訊息
     * @param message 要發送的訊息字串 (支援 & 符號顏色代碼)
     */
    private void sendConsole(String message) {
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
        this.getLogger().info(coloredMessage);
    }
}