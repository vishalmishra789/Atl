package com.kevin.tiertagger.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.kevin.tiertagger.tier.TierTagger;
import com.kevin.tiertagger.config.TierTaggerConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public record PlayerInfo(
    @SerializedName("id") String uuid, // Matches "id" from your snippet to "uuid" in code
    String ign, 
    String region, 
    int points, 
    Object tiers // Changed to Object to handle both String and Map formats
) {

    // Helper to safely get tiers as a Map even if stored as a String in DB
    public Map<String, String> getTiersMap() {
        if (tiers instanceof Map) {
            return (Map<String, String>) tiers;
        } else if (tiers instanceof String jsonStr) {
            return TierTagger.GSON.fromJson(jsonStr, new TypeToken<Map<String, String>>(){}.getType());
        }
        return Collections.emptyMap();
    }

    public enum PointInfo {
        GRANDMASTER("Combat Grandmaster", 0xE6C622, 0xFDE047),
        MASTER("Combat Master", 0xFBB03B, 0xFFD13A),
        ELITE("Combat Elite", 0xCD285C, 0xD65474),
        VETERAN("Combat Veteran", 0xAD78D8, 0xC7A3E8),
        APPRENTICE("Apprentice", 0x9291D9, 0xADACE2),
        CADET("Combat Cadet", 0x6C7178, 0x8B979C),
        UNRANKED("Unranked", 0xFFFFFF, 0xFFFFFF);

        private final String title;
        private final int color;
        private final int accentColor;

        PointInfo(String title, int color, int accentColor) {
            this.title = title;
            this.color = color;
            this.accentColor = accentColor;
        }

        public String getTitle() { return title; }
        public int getColor() { return color; }
        public int getAccentColor() { return accentColor; }
    }

    public PointInfo getPointInfo() {
        if (this.points >= 280) return PointInfo.GRANDMASTER;
        if (this.points >= 200) return PointInfo.MASTER;
        if (this.points >= 120) return PointInfo.ELITE;
        if (this.points >= 60)  return PointInfo.VETERAN;
        if (this.points >= 20)  return PointInfo.APPRENTICE;
        return PointInfo.CADET;
    }

    public int getRegionColor() {
        Map<String, Integer> colors = Map.of(
            "NA", 0xff6a6e, "EU", 0x6aff6e, "SA", 0xff9900, "AS", 0xc27ba0
        );
        return colors.getOrDefault(this.region != null ? this.region.toUpperCase() : "", 0xffffff);
    }

    public static CompletableFuture<PlayerInfo> get(HttpClient client, UUID uuid) {
        return fetch("id=eq." + uuid.toString()); // Changed query from uuid=eq to id=eq
    }

    public static CompletableFuture<PlayerInfo> search(HttpClient client, String query) {
        return fetch("ign=ilike." + query);
    }

    private static CompletableFuture<PlayerInfo> fetch(String query) {
        TierTaggerConfig config = TierTagger.getManager().getConfig();
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.getApiUrl() + "?" + query))
                .header("apikey", config.getSupabaseKey())
                .header("Authorization", "Bearer " + config.getSupabaseKey())
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return TierTagger.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    try {
                        List<PlayerInfo> list = TierTagger.GSON.fromJson(r.body(), new TypeToken<List<PlayerInfo>>(){}.getType());
                        return (list != null && !list.isEmpty()) ? list.get(0) : null;
                    } catch (Exception e) {
                        return null;
                    }
                });
    }

    public static String getHighestTier(Map<String, String> tiers) {
        if (tiers == null || tiers.isEmpty()) return null;
        Map<String, Integer> weight = Map.ofEntries(
            Map.entry("HT1", 40), Map.entry("LT1", 32), Map.entry("HT2", 24), Map.entry("LT2", 20),
            Map.entry("HT3", 16), Map.entry("LT3", 12), Map.entry("HT4", 8), Map.entry("LT4", 4),
            Map.entry("HT5", 2), Map.entry("LT5", 1), Map.entry("RET", 0)
        );
        return tiers.values().stream()
                .filter(weight::containsKey)
                .max(Comparator.comparingInt(weight::get))
                .orElse(null);
    }
}
