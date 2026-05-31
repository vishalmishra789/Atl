package com.kevin.tiertagger.model;

import net.minecraft.util.Identifier;
import java.util.Arrays;
import java.util.Optional;

public enum TierList {
    // We changed the name and removed the 'char' icon in favor of an image Identifier
    ARISE_CORE("Arise Tier List", 
               "https://zsdtlcyzhcfgcwqucdjd.supabase.co/rest/v1/players", 
               Identifier.of("tiertagger", "textures/gui/logo.png"));

    private final String name;
    private final String url;
    private final Identifier iconPath;

    TierList(String name, String url, Identifier iconPath) {
        this.name = name;
        this.url = url;
        this.iconPath = iconPath;
    }

    public String getName() { return name; }
    public String getUrl() { return url; }
    public Identifier getIconPath() { return iconPath; }

    public String styledName(boolean current) {
        return current ? name + " (Selected)" : name;
    }

    public static Optional<TierList> findByUrl(String url) {
        if (url == null) return Optional.empty();
        String cleanUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return Arrays.stream(values()).filter(list -> list.getUrl().equals(cleanUrl)).findFirst();
    }
}
