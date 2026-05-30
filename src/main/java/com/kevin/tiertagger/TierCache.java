package com.kevin.tiertagger;

import com.kevin.tiertagger.model.GameMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TierCache {
    private static final List<GameMode> GAMEMODES = new ArrayList<>();
    private static final Map<UUID, Optional<Map<String, String>>> TIERS = new ConcurrentHashMap<>();

    public static void init() {
        GameMode.fetchGamemodes(null).thenAccept(modes -> {
            GAMEMODES.clear();
            GAMEMODES.addAll(modes);
        });
    }

    public static List<GameMode> getGamemodes() { return GAMEMODES; }

    public static Optional<Map<String, String>> getPlayerRankings(UUID uuid) {
        return TIERS.computeIfAbsent(uuid, u -> {
            com.kevin.tiertagger.model.PlayerInfo.get(null, uuid).thenAccept(p -> {
                if (p != null) TIERS.put(uuid, Optional.of(p.tiers()));
            });
            return Optional.empty();
        });
    }

    public static GameMode findNextMode(String currentId) {
        if (GAMEMODES.isEmpty()) return GameMode.NONE;
        int index = 0;
        for (int i = 0; i < GAMEMODES.size(); i++) {
            if (GAMEMODES.get(i).id().equals(currentId)) index = (i + 1) % GAMEMODES.size();
        }
        return GAMEMODES.get(index);
    }

    public static Optional<GameMode> findMode(String id) {
        return GAMEMODES.stream().filter(m -> m.id().equalsIgnoreCase(id)).findFirst();
    }
}
