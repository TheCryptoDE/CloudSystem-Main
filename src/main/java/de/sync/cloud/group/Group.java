package de.sync.cloud.group;

public class Group {
    private String groupName;
    private String type; // "spigot" oder "bungeecord"
    private String staticFlag; // "static" oder "nostatic"
    private int minServer;
    private int maxPlayers; // <-- NEU

    public Group(String groupName, String type, String staticFlag, int minServer, int maxPlayers) {
        this.groupName = groupName;
        this.type = type.toLowerCase();
        this.staticFlag = staticFlag.toLowerCase();
        this.minServer = minServer;
        this.maxPlayers = maxPlayers;
    }

    // Getter & Setter

    public String getGroupName() { return groupName; }
    public String getType() { return type; }
    public String getStaticFlag() { return staticFlag; }
    public int getMinServer() { return minServer; }
    public int getMaxPlayers() { return maxPlayers; } // <-- NEU

    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setType(String type) { this.type = type; }
    public void setStaticFlag(String staticFlag) { this.staticFlag = staticFlag; }
    public void setMinServer(int minServer) { this.minServer = minServer; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; } // <-- NEU
}
