package tw.inysmp.inyac.data;

import org.bukkit.Location;
import java.util.UUID;

public class PlayerData {
    
    private final UUID uuid;
    private long lastMoveTime = System.currentTimeMillis();
    private Location lastLocation;
    
    private double speedViolations = 0; 
    
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }
    
    // Getters and Setters (為保持簡潔，只提供核心方法，您需自行補齊其他 Getter/Setter)
    
    public double getSpeedViolations() {
        return speedViolations;
    }

    public void setSpeedViolations(double speedViolations) {
        this.speedViolations = speedViolations;
    }
    
    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public void setLastMoveTime(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }
}