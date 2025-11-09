package tw.inysmp.inyac.checks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import tw.inysmp.inyac.InyAC;
import tw.inysmp.inyac.data.PlayerData;
import tw.inysmp.inyac.data.PunishmentData;
import java.util.UUID;

public class SpeedCheck implements Listener {

    private final InyAC plugin;
    private final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); // 系統執行者 UUID

    public SpeedCheck(InyAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || event.getFrom().equals(event.getTo())) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("inyac.bypass")) return;

        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        Location from = event.getFrom();
        Location to = event.getTo();

        // 排除在水裡、飛行模式、或創造模式下的移動
        if (player.isFlying() || player.getAllowFlight() || player.isInsideVehicle() || player.isSwimming() || player.isRiptiding()) {
            data.setSpeedViolations(0);
            return;
        }

        double distanceXZ = from.distance(to); 
        
        // 獲取配置值
        double maxAllowedBase = plugin.getConfig().getDouble("checks.speed.max_allowed_speed_base", 0.4);
        double maxViolations = plugin.getConfig().getDouble("checks.speed.max_violations", 20.0);
        double decayRate = plugin.getConfig().getDouble("checks.speed.violation_decay_rate", 0.05);

        double maxAllowed = maxAllowedBase;
        
        // 速度藥水修正
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int amplifier = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1;
            maxAllowed *= (1.0 + 0.2 * amplifier); // 每級速度藥水增加 20%
        }
        
        // 衝刺修正
        if (player.isSprinting()) {
            maxAllowed *= 1.3; 
        }

        // 50ms 是一個 Tick 的時間
        if (distanceXZ > maxAllowed) {
            // 違規行為
            data.setSpeedViolations(data.getSpeedViolations() + 1.0);
            
            if (data.getSpeedViolations() > maxViolations) {
                punish(player, "Speed Hack", "Player's XZ movement was too fast (" + String.format("%.2f", distanceXZ) + " > " + String.format("%.2f", maxAllowed) + ")");
                event.setCancelled(true); 
                player.teleport(from); 
                data.setSpeedViolations(0); 
            } else if (data.getSpeedViolations() > maxViolations / 2) {
                // 警告階段
                player.sendMessage("§c[InyAC] §7警告: 偵測到移動異常。");
            }
        } else {
            // 緩慢衰減違規等級
            data.setSpeedViolations(Math.max(0, data.getSpeedViolations() - decayRate));
        }
    }

    private void punish(Player player, String type, String reason) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // 1. 建立懲罰數據對象
            PunishmentData data = new PunishmentData(
                player.getUniqueId(), 
                player.getName(), 
                "KICK", 
                "[InyAC-" + type + "] " + reason, 
                SYSTEM_UUID
            );
            
            // 2. 寫入數據庫
            if (plugin.getDatabaseManager() != null) {
                plugin.getDatabaseManager().savePunishment(data);
            }
            
            // 3. 執行踢出動作
            player.kickPlayer("§c[InyAC] 偵測到作弊: " + type);
        });
    }
}