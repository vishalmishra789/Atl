package com.kevin.tiertagger.tierlist;

import com.kevin.tiertagger.TierCache;
import com.kevin.tiertagger.TierTagger;
import com.kevin.tiertagger.model.GameMode;
import com.kevin.tiertagger.model.PlayerInfo;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.uku3lig.ukulib.utils.Ukutils;

import java.util.Map;

public class PlayerInfoScreen extends Screen {
    private final Screen parent;
    private final PlayerInfo playerInfo;

    public PlayerInfoScreen(Screen parent, PlayerInfo playerInfo) {
        super(Text.of("Player Info: " + playerInfo.ign()));
        this.parent = parent;
        this.playerInfo = playerInfo;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> this.client.setScreen(this.parent))
                .dimensions(this.width / 2 - 100, this.height - 30, 200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int y = 40;

        // Draw IGN with Region Color
        MutableText title = Text.literal(playerInfo.ign()).styled(s -> s.withColor(playerInfo.getRegionColor()).withBold(true));
        context.drawCenteredTextWithShadow(this.textRenderer, title, this.width / 2, y, 0xFFFFFF);
        y += 15;

        // Draw Points and Rank Title (Combat Master, etc.)
        PlayerInfo.PointInfo pointInfo = playerInfo.getPointInfo();
        MutableText pointsText = Text.literal("Points: ").append(Text.literal(String.valueOf(playerInfo.points())).styled(s -> s.withColor(pointInfo.getColor())));
        pointsText.append(Text.literal(" (" + pointInfo.getTitle() + ")").formatted(Formatting.GRAY));
        context.drawCenteredTextWithShadow(this.textRenderer, pointsText, this.width / 2, y, 0xFFFFFF);
        y += 20;

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("--- Rankings ---").formatted(Formatting.DARK_GRAY), this.width / 2, y, 0xFFFFFF);
        y += 15;

        // Draw the tiers from the Map<String, String>
        if (playerInfo.tiers() == null || playerInfo.tiers().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("No tiers found.").formatted(Formatting.GRAY), this.width / 2, y, 0xFFFFFF);
        } else {
            for (Map.Entry<String, String> entry : playerInfo.tiers().entrySet()) {
                GameMode mode = TierCache.findModeOrUgly(entry.getKey());
                String tier = entry.getValue();

                Text modeText = mode.asStyled(true);
                Text tierText = formatTier(tier);

                MutableText line = Text.empty().append(modeText).append(": ").append(tierText);
                context.drawCenteredTextWithShadow(this.textRenderer, line, this.width / 2, y, 0xFFFFFF);
                y += 12;
            }
        }
    }

    // Fixed: Now accepts String instead of PlayerInfo.Ranking
    private Text formatTier(String tier) {
        if (tier == null) return Text.literal("None").formatted(Formatting.GRAY);
        
        int color = TierTagger.getTierColor(tier);
        return Text.literal(tier).styled(s -> s.withColor(color));
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
