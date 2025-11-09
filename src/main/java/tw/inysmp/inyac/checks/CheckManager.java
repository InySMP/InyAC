package tw.inysmp.inyac.checks;

import org.bukkit.plugin.PluginManager;
import tw.inysmp.inyac.InyAC;

public class CheckManager {
    
    private final InyAC plugin;

    public CheckManager(InyAC plugin) {
        this.plugin = plugin;
    }

    public void registerChecks() {
        PluginManager pm = plugin.getServer().getPluginManager();
        
        // 註冊所有 Listener
        pm.registerEvents(new SpeedCheck(plugin), plugin);
        
        plugin.getLogger().info("All anti-cheat checks have been registered.");
    }
}