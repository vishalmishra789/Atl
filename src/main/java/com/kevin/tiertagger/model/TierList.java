package com.kevin.tiertagger.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum TierList {
    ARISE_CORE("AriseCore", "https://zsdtlcyzhcfgcwqucdjd.supabase.co/rest/v1/players", '\uE901'),
    ;

    private final String name;
    private final String url;
    private final char icon;

    public String styledName(boolean current) {
        String s = icon + " " + name;
        if (current) s += " (Selected)";
        return s;
    }

    public static Optional<TierList> findByUrl(String url) {
        String cleanUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return Arrays.stream(values()).filter(list -> list.url.equals(cleanUrl)).findFirst();
    }
}
