package com.kevin.tiertagger.tierlist;

import com.kevin.tiertagger.model.PlayerInfo;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import java.util.Map;

public class PlayerInfoScreen extends Screen {
    private final Screen parent;
    private final PlayerInfo info;
    private final SkinTextures skin;

    public PlayerInfoScreen(Screen parent, PlayerInfo info, SkinTextures skin) {
        super(Text.literal("Player Info: " + info.ign()));
        this.parent = parent;
        this.info = info;
        this.skin = skin;
    }

    @Override
    protected void init() {
        // Add buttons like "Back" here
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        int x = this.width / 2;
        int y = this.height / 2;

        // Example: Render player name and points
        context.drawCenteredTextWithShadow(this.textRenderer, info.ign(), x, y - 50, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Points: " + info.points(), x, y - 30, 0xAAAAAA);
        
        // Render Region
        if (info.region() != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, "Region: " + info.region(), x, y - 10, info.getRegionColor());
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
