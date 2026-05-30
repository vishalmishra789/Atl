package com.kevin.tiertagger.model;

import com.google.gson.reflect.TypeToken;
import com.kevin.tiertagger.TierTagger;
import com.kevin.tiertagger.TierCache;
import com.kevin.tiertagger.config.TierTaggerConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public record PlayerInfo(String uuid, String ign, String region, int points, Map<String, String> tiers) {

    @Getter
    @AllArgsConstructor
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
        Map<String, Integer> colors = Map.of("NA", 0xff6a6e, "EU", 0x6aff6e, "SA", 0xff9900, "AS", 0xc27ba0);
        return colors.getOrDefault(this.region != null ? this.region.toUpperCase() : "", 0xffffff);
    }

    public static CompletableFuture<PlayerInfo> get(HttpClient client, UUID uuid) {
        return fetch("uuid=eq." + uuid.toString());
    }

    public static CompletableFuture<PlayerInfo> search(HttpClient client, String query) {
        return fetch("ign=ilike." + query);
    }

    private static CompletableFuture<PlayerInfo> fetch(String query) {
        TierTaggerConfig config = TierTagger.getManager().getConfig();
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.getApiUrl() + "?" + query))
                .header("apikey", config.getSupabaseKey())
                .header("Authorization", "Bearer " + config.getSupabaseKey())
                .GET().build();

        return TierTagger.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    List<PlayerInfo> list = TierTagger.GSON.fromJson(r.body(), new TypeToken<List<PlayerInfo>>(){}.getType());
                    return (list != null && !list.isEmpty()) ? list.get(0) : null;
                });
    }

    public static String getHighestTier(Map<String, String> tiers) {
        // Use Map.ofEntries for more than 10 elements
        Map<String, Integer> weight = new HashMap<>();
        weight.put("HT1", 40); weight.put("LT1", 32); weight.put("HT2", 24); weight.put("LT2", 20);
        weight.put("HT3", 16); weight.put("LT3", 12); weight.put("HT4", 8); weight.put("LT4", 4);
        weight.put("HT5", 2); weight.put("LT5", 1); weight.put("RET", 0);

        return tiers.entrySet().stream()
                .max(Comparator.comparingInt(e -> weight.getOrDefault(e.getValue(), -1)))
                .map(Map.Entry::getValue).orElse(null);
    }
}
