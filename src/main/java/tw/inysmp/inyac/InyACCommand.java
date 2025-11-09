package tw.inysmp.inyac;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import tw.inysmp.inyac.data.PlayerData;
import java.util.List;
import java.util.UUID; // <--- 修正: 導入 UUID

public class InyACCommand implements CommandExecutor {

    private final InyAC plugin;

    public InyACCommand(InyAC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 獲取配置和前綴
        FileConfiguration messages = plugin.getMessagesConfig(); // <--- 修正: 此方法現在應該能找到
        String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix", "&7[&cInyAC&7] &f"));
        
        // 檢查權限
        if (!sender.hasPermission("inyac.admin")) {
            String noPerm = messages.getString("command.no_permission", "&c您沒有權限執行此命令。");
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPerm));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender, messages);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig(); // 重新載入 config.yml
            plugin.reloadMessagesConfig(); // <--- 修正: 此方法現在應該能找到
            
            // 重新讀取更新後的訊息配置
            messages = plugin.getMessagesConfig();
            prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix", "&7[&cInyAC&7] &f"));
            
            String successMessage = messages.getString("command.reload_success", "&a配置已重新載入。");
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', successMessage));
            return true;
        }

        if (args[0].equalsIgnoreCase("check")) {
            if (args.length < 2) {
                String usage = messages.getString("command.usage_check", "&e用法: /inyac check <玩家名稱>");
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', usage));
                return true;
            }
            
            String targetName = args[1];
            Player target = plugin.getServer().getPlayer(targetName);

            if (target == null) {
                String notFound = messages.getString("command.player_not_found", "&c找不到玩家 %target% 或其未上線。").replace("%target%", targetName);
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notFound));
                return true;
            }

            showPlayerData(sender, target);
            return true;
        }

        String unknown = messages.getString("command.unknown_command", "&e未知子命令。請使用 /inyac help 查看可用命令。");
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', unknown));
        return true;
    }

    private void sendHelpMessage(CommandSender sender, FileConfiguration messages) {
        String header = ChatColor.translateAlternateColorCodes('&', messages.getString("command.help_header", "&8--- &cInyAC Anti-Cheat Help &8---"));
        String footer = ChatColor.translateAlternateColorCodes('&', messages.getString("command.help_footer", "&8------------------------------"));
        List<String> helpList = messages.getStringList("command.help_list");

        sender.sendMessage(header);
        for (String line : helpList) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
        sender.sendMessage(footer);
    }

    private void showPlayerData(CommandSender sender, Player target) {
        PlayerData data = plugin.getPlayerData(target.getUniqueId());

        sender.sendMessage(ChatColor.DARK_GRAY + "--- Player Data: " + target.getName() + " ---");
        sender.sendMessage(ChatColor.YELLOW + "UUID: " + ChatColor.AQUA + target.getUniqueId().toString());
        
        sender.sendMessage(ChatColor.YELLOW + "Speed Violations: " + ChatColor.RED + String.format("%.2f", data.getSpeedViolations()));
        
        sender.sendMessage(ChatColor.DARK_GRAY + "---------------------------------");
    }
}