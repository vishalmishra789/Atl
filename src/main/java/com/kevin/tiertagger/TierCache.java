package com.kevin.tiertagger;

import com.kevin.tiertagger.model.GameMode;
import com.kevin.tiertagger.model.PlayerInfo;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class TierCache {
    private static final List<GameMode> GAMEMODES = new ArrayList<>();
    // Stores UUID -> Map of <Gamemode, TierString>
    private static final Map<UUID, Optional<Map<String, String>>> TIERS = new ConcurrentHashMap<>();

    public static void init() {
        try {
            GAMEMODES.clear();
            GAMEMODES.addAll(GameMode.fetchGamemodes(TierTagger.getClient()).get());
        } catch (ExecutionException | InterruptedException e) {
            TierTagger.getLogger().error("Failed to load gamemodes!", e);
        }
    }

    public static List<GameMode> getGamemodes() {
        return GAMEMODES.isEmpty() ? Collections.singletonList(GameMode.NONE) : GAMEMODES;
    }

    public static Optional<Map<String, String>> getPlayerRankings(UUID uuid) {
        return TIERS.computeIfAbsent(uuid, u -> {
            PlayerInfo.get(TierTagger.getClient(), uuid).thenAccept(info -> {
                if (info != null) {
                    TIERS.put(uuid, Optional.ofNullable(info.tiers()));
                }
            });
            return Optional.empty();
        });
    }

    public static CompletableFuture<PlayerInfo> searchPlayer(String query) {
        return PlayerInfo.search(TierTagger.getClient(), query).thenApply(p -> {
            if (p != null) {
                UUID uuid = parseUUID(p.uuid());
                TIERS.put(uuid, Optional.ofNullable(p.tiers()));
            }
            return p;
        });
    }

    public static void clearCache() {
        TIERS.clear();
    }

    public static Optional<GameMode> findMode(String id) {
        return GAMEMODES.stream().filter(m -> m.id().equalsIgnoreCase(id)).findFirst();
    }

    public static GameMode findModeOrUgly(String id) {
        return findMode(id).orElseGet(() -> new GameMode(id, id));
    }

    private static UUID parseUUID(String uuidStr) {
        if (uuidStr.contains("-")) return UUID.fromString(uuidStr);
        return UUID.fromString(uuidStr.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
    }

    private TierCache() {}
}
