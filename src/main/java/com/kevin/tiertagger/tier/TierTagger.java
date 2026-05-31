package com.kevin.tiertagger.tier;

import com.google.gson.Gson;
import com.kevin.tiertagger.config.TierTaggerConfig;
import com.kevin.tiertagger.model.PlayerInfo;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.ClientModInitializer; // ADDED THIS
import net.fabricmc.loader.api.Version;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.uku3lig.ukulib.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import java.util.Optional;
import java.util.UUID;

// IMPLEMENT BOTH ModInitializer AND ClientModInitializer
public class TierTagger implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "tiertagger";
    public static final Gson GSON = new Gson();

    private static final ConfigManager<TierTaggerConfig> manager = ConfigManager.createDefault(TierTaggerConfig.class, MOD_ID);
    private static final Logger logger = LoggerFactory.getLogger(TierTagger.class);
    private static final HttpClient client = HttpClient.newHttpClient();

    @Override 
    public void onInitialize() { 
        TierCache.init(); 
    }

    // ADD THIS METHOD TO FIX THE CRASH
    @Override
    public void onInitializeClient() {
        // This is called during client startup
        // You can leave it empty or move TierCache.init() here if you want
    }

    public static ConfigManager<TierTaggerConfig> getManager() {
        return manager;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static HttpClient getClient() {
        return client;
    }

    public static Text appendTier(UUID uuid, Text text) {
        return getPlayerTier(uuid).map(tierStr -> {
            MutableText tierText = Text.literal(tierStr).styled(s -> s.withColor(getTierColor(tierStr)));
            return (Text) Text.empty().append(tierText).append(Text.literal(" | ").formatted(Formatting.GRAY)).append(text);
        }).orElse(text);
    }

    public static Optional<String> getPlayerTier(UUID uuid) {
        return TierCache.getPlayerRankings(uuid).map(rankings -> {
            String ranking = rankings.get(manager.getConfig().getGameMode().id());
            return ranking != null ? ranking : PlayerInfo.getHighestTier(rankings);
        });
    }

    public static int getTierColor(String tier) {
        return manager.getConfig().getTierColors().getOrDefault(tier, 0xD3D3D3);
    }

    public static Version getLatestVersion() { return null; }
    public static boolean isObsolete() { return false; }
}
