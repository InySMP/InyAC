package tw.inysmp.inyac.data;

import java.util.UUID;

public class PunishmentData {
    private final UUID playerUuid;
    private final String playerName;
    private final String type; 
    private final String reason;
    private final UUID staffUuid; 

    public PunishmentData(UUID playerUuid, String playerName, String type, String reason, UUID staffUuid) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.type = type;
        this.reason = reason;
        this.staffUuid = staffUuid;
    }

    // Getters
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public String getType() { return type; }
    public String getReason() { return reason; }
    public UUID getStaffUuid() { return staffUuid; }
}