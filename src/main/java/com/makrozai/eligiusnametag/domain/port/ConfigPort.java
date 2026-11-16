package com.makrozai.eligiusnametag.domain.port;

import java.util.List;


public interface ConfigPort {
    List<String> getPlayerNametagTemplate(String group);
    List<String> getTamedMobNametagTemplate(String group);

    /**
     * Nametag template shown while the player stands inside the world spawn area
     * (players.yml "inspawn" section). Empty list = no spawn template (fall back to normal).
     */
    List<String> getSpawnNametagTemplate(String group);

    /** Radius in blocks around the world spawn point for the "inspawn" section (0 disables). */
    double getSpawnRadius();

    /** True when players.yml declares any "inspawn" template (section used at all). */
    boolean hasSpawnNametagConfig();
    boolean isTamedMobsEnabled();
    boolean isTamedMobsShowUnnamed();
    int getViewDistance();
    double getLineSpacing();
    double getYOffset();
    double getInterval();
    java.util.List<String> getCommandAliases();
    String getMessage(String key);
    void reload();
    
    boolean hasLanguage(String lang);
    void setLanguage(String lang);
    void setPetsEnabled(boolean enabled);
    boolean isCheckUpdates();
    
    String getDatabaseType();
    String getDatabaseHost();
    int getDatabasePort();
    String getDatabaseName();
    String getDatabaseUsername();
    String getDatabasePassword();
    
    boolean isRedisEnabled();
    String getRedisHost();
    int getRedisPort();
    String getRedisPassword();
}
