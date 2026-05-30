package com.kevin.tiertagger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kevin.tiertagger.config.TierTaggerConfig;
import com.kevin.tiertagger.model.GameMode;
import com.kevin.tiertagger.model.PlayerInfo;
import com.mojang.brigadier.context.CommandContext;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.utils.PlayerArgumentType;
import net.uku3lig.ukulib.utils.Ukutils;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TierTagger implements ModInitializer {
    public static final String MOD_ID = "tiertagger";
    public static final Gson GSON = new GsonBuilder().create();

    @Getter
    private static final ConfigManager<TierTaggerConfig> manager = ConfigManager.createDefault(TierTaggerConfig.class, MOD_ID);
    @Getter
    private static final Logger logger = LoggerFactory.getLogger(TierTagger.class);
    @Getter
    private static final HttpClient client = HttpClient.newHttpClient();

    @Override
    public void onInitialize() {
        TierCache.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(
                literal(MOD_ID)
                        .then(argument("player", PlayerArgumentType.player())
                                .executes(TierTagger::displayTierInfo))));

        Ukutils.registerKeybinding(new KeyBinding("tiertagger.keybind.gamemode", GLFW.GLFW_KEY_UNKNOWN, "tiertagger.name"),
                mc -> {
                    GameMode next = TierCache.findNextMode(manager.getConfig().getGameMode());
                    manager.getConfig().setGameMode(next.id());

                    if (mc.player != null) {
                        Text message = Text.literal("Displayed gamemode: ").append(next.asStyled(false));
                        mc.player.sendMessage(message, true);
                    }
                });
    }

    public static Text appendTier(UUID uuid, Text text) {
        return getPlayerTier(uuid)
                .map(tierStr -> {
                    MutableText tierText = getTierText(tierStr);
                    GameMode mode = manager.getConfig().getGameMode();

                    MutableText prefix = Text.empty();
                    if (manager.getConfig().isShowIcons() && mode.icon().isPresent()) {
                        prefix.append(Text.literal(mode.icon().get().toString()));
                    }
                    prefix.append(tierText).append(Text.literal(" | ").formatted(Formatting.GRAY));
                    
                    return prefix.append(text);
                })
                .orElse(text);
    }

    public static Optional<String> getPlayerTier(UUID uuid) {
        String modeId = manager.getConfig().getGameMode().id();
        
        return TierCache.getPlayerRankings(uuid).map(rankings -> {
            String ranking = rankings.get(modeId);
            TierTaggerConfig.HighestMode highestMode = manager.getConfig().getHighestMode();

            if (ranking == null) {
                if (highestMode != TierTaggerConfig.HighestMode.NEVER) {
                    return PlayerInfo.getHighestTier(rankings);
                }
                return null;
            } else {
                if (highestMode == TierTaggerConfig.HighestMode.ALWAYS) {
                    return PlayerInfo.getHighestTier(rankings);
                }
                return ranking;
            }
        });
    }

    private static MutableText getTierText(String tierStr) {
        int color = manager.getConfig().getTierColors().getOrDefault(tierStr, 0xD3D3D3);
        return Text.literal(tierStr).styled(s -> s.withColor(color));
    }

    private static int displayTierInfo(CommandContext<FabricClientCommandSource> ctx) {
        PlayerArgumentType.PlayerSelector selector = ctx.getArgument("player", PlayerArgumentType.PlayerSelector.class);

        // Search logic for /tiertagger <name> command
        TierCache.searchPlayer(selector.name()).thenAccept(p -> {
            if (p != null) {
                MinecraftClient.getInstance().execute(() -> 
                    ctx.getSource().sendFeedback(printPlayerInfo(p.ign(), p.tiers(), p.points()))
                );
            } else {
                ctx.getSource().sendError(Text.of("Could not find player " + selector.name()));
            }
        });

        return 0;
    }

    private static Text printPlayerInfo(String name, Map<String, String> tiers, int points) {
        if (tiers == null || tiers.isEmpty()) {
            return Text.literal(name + " does not have any tiers.");
        } else {
            PlayerInfo tempPlayer = new PlayerInfo(null, name, null, points, tiers);
            PlayerInfo.PointInfo pointsInfo = tempPlayer.getPointInfo();
            
            MutableText text = Text.empty().append("§l=== Rankings for " + name + " ===§r\n");
            text.append(Text.literal("Points: ").append(Text.literal(String.valueOf(points)).styled(s -> s.withColor(pointsInfo.getColor()))));
            text.append(Text.literal(" (" + pointsInfo.getTitle() + ")\n").formatted(Formatting.GRAY));

            tiers.forEach((m, r) -> {
                GameMode mode = TierCache.findModeOrUgly(m);
                text.append(Text.literal("\n").append(mode.asStyled(true)).append(": ").append(getTierText(r)));
            });

            return text;
        }
    }
}
