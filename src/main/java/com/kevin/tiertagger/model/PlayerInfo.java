package com.kevin.tiertagger.model;

import com.google.gson.reflect.TypeToken;
import com.kevin.tiertagger.TierTagger;
import com.kevin.tiertagger.config.TierTaggerConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public record PlayerInfo(
    String uuid, 
    String ign, 
    String region, 
    int points, 
    Map<String, String> tiers
) {

    // --- YOUR SERVER'S POINT SYSTEM ---
    @Getter
    @AllArgsConstructor
    public enum PointInfo {
        GRANDMASTER("Combat Grandmaster", 0xE6C622, 0xFDE047), // Gold
        MASTER("Combat Master", 0xFBB03B, 0xFFD13A),           // Orange
        ELITE("Combat Elite", 0xCD285C, 0xD65474),            // Pink/Red
        VETERAN("Combat Veteran", 0xAD78D8, 0xC7A3E8),        // Purple
        APPRENTICE("Apprentice", 0x9291D9, 0xADACE2),         // Blue
        CADET("Combat Cadet", 0x6C7178, 0x8B979C),            // Gray
        UNRANKED("Unranked", 0xFFFFFF, 0xFFFFFF);

        private final String title;
        private final int color;
        private final int accentColor;
    }

    public PointInfo getPointInfo() {
        if (this.points >= 280) return PointInfo.GRANDMASTER;
        if (this.points >= 200) return PointInfo.MASTER;
        if (this.points >= 120) return PointInfo.ELITE;
        if (this.points >= 60)  return PointInfo.VETERAN;
        if (this.points >= 20)  return PointInfo.APPRENTICE;
        return PointInfo.CADET;
    }

    // --- REGION COLORS (Kept for AS region support) ---
    private static final Map<String, Integer> REGION_COLORS = Map.of(
            "NA", 0xff6a6e, "EU", 0x6aff6e, "SA", 0xff9900,
            "AU", 0xf6b26b, "ME", 0xffd966, "AS", 0xc27ba0, "AF", 0x674ea7
    );

    public int getRegionColor() {
        return REGION_COLORS.getOrDefault(this.region != null ? this.region.toUpperCase(Locale.ROOT) : "", 0xffffff);
    }

    // --- SUPABASE NETWORK LOGIC ---

    private static HttpRequest createSupabaseRequest(String query) {
        TierTaggerConfig config = TierTagger.getManager().getConfig();
        // Updated URL for Supabase filtering
        String url = config.getApiUrl() + "?" + query;
        
        return HttpRequest.newBuilder(URI.create(url))
                .header("apikey", config.getSupabaseKey())
                .header("Authorization", "Bearer " + config.getSupabaseKey())
                .header("Content-Type", "application/json")
                .GET()
                .build();
    }

    public static CompletableFuture<PlayerInfo> get(HttpClient client, UUID uuid) {
        HttpRequest request = createSupabaseRequest("uuid=eq." + uuid.toString());

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    // Supabase returns a List [], so we take the first element
                    List<PlayerInfo> list = TierTagger.GSON.fromJson(response.body(), new TypeToken<List<PlayerInfo>>(){}.getType());
                    return (list != null && !list.isEmpty()) ? list.get(0) : null;
                })
                .whenComplete((i, t) -> {
                    if (t != null) TierTagger.getLogger().warn("Error getting player info from Supabase ({})", uuid, t);
                });
    }

    public static CompletableFuture<PlayerInfo> search(HttpClient client, String query) {
        HttpRequest request = createSupabaseRequest("ign=ilike." + query);

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    List<PlayerInfo> list = TierTagger.GSON.fromJson(response.body(), new TypeToken<List<PlayerInfo>>(){}.getType());
                    return (list != null && !list.isEmpty()) ? list.get(0) : null;
                });
    }

    // --- TIER DATA HELPERS ---

    /**
     * Gets the tier string (e.g. "HT1") for the currently selected gamemode.
     */
    public String getTierValue(String gameModeId) {
        if (this.tiers == null) return null;
        return this.tiers.get(gameModeId); // Directly returns "HT1", "LT3", etc.
    }

    // This helper determines which tier is "Highest" based on your point map
    public static String getHighestTier(Map<String, String> tiers) {
        Map<String, Integer> weight = Map.of(
            "HT1", 40, "LT1", 32, "HT2", 24, "LT2", 20, 
            "HT3", 16, "LT3", 12, "HT4", 8, "LT4", 4, 
            "HT5", 2, "LT5", 1, "RET", 0
        );

        return tiers.values().stream()
                .max(Comparator.comparingInt(t -> weight.getOrDefault(t, -1)))
                .orElse(null);
    }
}
