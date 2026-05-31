package com.kevin.tiertagger.model;

import java.util.Arrays;
import java.util.Optional;

public enum TierList {
    ARISE_CORE("AriseCore", "https://zsdtlcyzhcfgcwqucdjd.supabase.co/rest/v1/players", '\uE901');

    private final String name;
    private final String url;
    private final char icon;

    // Manual constructor (Fixes the "cannot be applied to given types" error)
    TierList(String name, String url, char icon) {
        this.name = name;
        this.url = url;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public char getIcon() {
        return icon;
    }

    public String styledName(boolean current) {
        String s = icon + " " + name;
        if (current) s += " (Selected)";
        return s;
    }

    public static Optional<TierList> findByUrl(String url) {
        if (url == null) return Optional.empty();
        String cleanUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return Arrays.stream(values()).filter(list -> list.getUrl().equals(cleanUrl)).findFirst();
    }
}
