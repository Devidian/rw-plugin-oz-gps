package de.omegazirkel.risingworld.gps;

import net.risingworld.api.utils.Vector3f;

public class Marker {

    // Database primary key (auto-increment)
    private Integer id;

    // Player database id (owner)
    private int playerId;

    // Marker visibility / scope
    private MarkerType type;

    // Group identifier (only used for GROUP markers)
    private String group;

    // Creation timestamp
    private long createdAt;

    // World position
    private Vector3f position;

    // Display name
    private String name;

    // Icon key or asset name
    private String icon;

    // RGBA color packed into a single int
    private int color;

    // Cost to create or use this marker
    private int cost;

    /* ---------- Constructors ---------- */

    // Constructor for new markers (not yet persisted)
    public Marker(int playerId,
            MarkerType type,
            String group,
            Vector3f position,
            String name,
            String icon,
            int color,
            int cost) {

        this.playerId = playerId;
        this.type = type;
        this.group = group;
        this.position = position;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.cost = cost;
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor for loading from database
    public Marker(Integer id,
            int playerId,
            MarkerType type,
            String group,
            long createdAt,
            Vector3f position,
            String name,
            String icon,
            int color,
            int cost) {

        this.id = id;
        this.playerId = playerId;
        this.type = type;
        this.group = group;
        this.createdAt = createdAt;
        this.position = position;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.cost = cost;
    }

    /* ---------- Getters / Setters ---------- */

    public Integer getId() {
        return id;
    }

    public int getPlayerId() {
        return playerId;
    }

    public MarkerType getType() {
        return type;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Vector3f getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public int getCost() {
        return cost;
    }

    public String getGroup() {
        return group;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setType(MarkerType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setGroup(String group) {
        this.group = group;
    }

}
