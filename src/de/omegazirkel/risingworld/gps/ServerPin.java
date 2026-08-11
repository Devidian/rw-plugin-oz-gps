package de.omegazirkel.risingworld.gps;

/** A globally visible destination for a cross-server transfer. */
public class ServerPin {
    private Integer id;
    private final int creatorId;
    private final long createdAt;
    private String name;
    private String icon;
    private String address;
    private String password;

    public ServerPin(int creatorId, String name, String icon, String address, String password) {
        this(null, creatorId, System.currentTimeMillis(), name, icon, address, password);
    }

    public ServerPin(Integer id, int creatorId, long createdAt, String name, String icon, String address, String password) {
        this.id = id;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.name = name;
        this.icon = icon;
        this.address = address;
        this.password = password == null ? "" : password;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getCreatorId() { return creatorId; }
    public long getCreatedAt() { return createdAt; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password == null ? "" : password; }
}
