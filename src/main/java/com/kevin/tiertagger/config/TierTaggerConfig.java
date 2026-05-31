package com.kevin.tiertagger.config;

import com.google.gson.internal.LinkedTreeMap;
import com.kevin.tiertagger.TierCache;
import com.kevin.tiertagger.model.GameMode;
import net.minecraft.util.TranslatableOption;

import java.io.Serializable;
import java.util.Optional;

public class TierTaggerConfig implements Serializable {
    private boolean enabled = true;
    private String gameMode = "crystal";
    private boolean showRetired = true;
    private HighestMode highestMode = HighestMode.NOT_FOUND;
    private boolean showIcons = true;
    private boolean playerList = true;
    private int retiredColor = 0xa2d6ff;
    private LinkedTreeMap<String, Integer> tierColors = defaultColors();

    private String apiUrl = "https://zsdtlcyzhcfgcwqucdjd.supabase.co/rest/v1/players";
    private String supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpzZHRsY3l6aGNmZ2N3cXVjZGpkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ1NDY2MDYsImV4cCI6MjA5MDEyMjYwNn0.IgjMXn3NbVw5nt8CvrJksD4NTEr26nLGwdytUgWFLe0";

    public TierTaggerConfig() {}

    // --- Manual Getters and Setters (Fixes "cannot find symbol" errors) ---
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getSupabaseKey() { return supabaseKey; }
    public void setSupabaseKey(String supabaseKey) { this.supabaseKey = supabaseKey; }

    public boolean isShowRetired() { return showRetired; }
    public void setShowRetired(boolean showRetired) { this.showRetired = showRetired; }

    public boolean isShowIcons() { return showIcons; }
    public void setShowIcons(boolean showIcons) { this.showIcons = showIcons; }

    public boolean isPlayerList() { return playerList; }
    public void setPlayerList(boolean playerList) { this.playerList = playerList; }

    public int getRetiredColor() { return retiredColor; }
    public void setRetiredColor(int retiredColor) { this.retiredColor = retiredColor; }

    public LinkedTreeMap<String, Integer> getTierColors() { return tierColors; }

    public HighestMode getHighestMode() { return highestMode; }
    public void setHighestMode(HighestMode mode) { this.highestMode = mode; }

    public GameMode getGameMode() {
        Optional<GameMode> opt = TierCache.findMode(this.gameMode);
        if (opt.isPresent()) {
            return opt.get();
        } else {
            if (TierCache.getGamemodes().isEmpty()) return GameMode.NONE;
            GameMode first = TierCache.getGamemodes().get(0);
            if (!first.isNone()) this.gameMode = first.id();
            return first;
        }
    }

    public void setGameMode(String id) { this.gameMode = id; }

    private static LinkedTreeMap<String, Integer> defaultColors() {
        LinkedTreeMap<String, Integer> colors = new LinkedTreeMap<>();
        colors.put("HT1", 0xe8ba3a);
        colors.put("LT1", 0xd5b355);
        colors.put("HT2", 0xc4d3e7);
        colors.put("LT2", 0xa0a7b2);
        colors.put("HT3", 0xf89f5a);
        colors.put("LT3", 0xc67b42);
        colors.put("HT4", 0x81749a);
        colors.put("LT4", 0x655b79);
        colors.put("HT5", 0x8f82a8);
        colors.put("LT5", 0x655b79);
        colors.put("RET", 0xa2d6ff);
        return colors;
    }

    public enum HighestMode implements TranslatableOption {
        NEVER(0, "tiertagger.highest.never"),
        NOT_FOUND(1, "tiertagger.highest.not_found"),
        ALWAYS(2, "tiertagger.highest.always");

        private final int id;
        private final String translationKey;

        HighestMode(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }
    }
}
