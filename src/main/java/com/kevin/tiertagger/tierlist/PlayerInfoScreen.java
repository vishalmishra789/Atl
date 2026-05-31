package com.kevin.tiertagger.tierlist;

import com.kevin.tiertagger.model.PlayerInfo;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class PlayerInfoScreen extends Screen {
    private final Screen parent;
    private final PlayerInfo info;
    private final SkinTextures skin;

    public PlayerInfoScreen(Screen parent, PlayerInfo info, SkinTextures skin) {
        super(Text.literal("Player Info"));
        this.parent = parent;
        this.info = info;
        this.skin = skin;
    }

    @Override
    protected void init() {
        // Buttons could be added here
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        int x = this.width / 2;
        int y = this.height / 2;

        context.drawCenteredTextWithShadow(this.textRenderer, info.ign(), x, y - 60, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Points: " + info.points(), x, y - 40, 0xAAAAAA);
        
        if (info.region() != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, "Region: " + info.region(), x, y - 20, info.getRegionColor());
        }
        
        // Render the player face (1.21.4 compatible syntax)
        if (skin != null) {
            // Arguments: RenderLayer, Texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight
            context.drawTexture(
                    RenderLayer::getGuiTextured, 
                    skin.texture(), 
                    x - 16, y,      // Position
                    8f, 8f,         // UV (Face start)
                    32, 32,         // Size on screen
                    8, 8,           // Size of the face in the texture
                    64, 64          // Size of the whole texture
            );
            
            // Render the "Hat" layer (optional overlay)
            context.drawTexture(
                    RenderLayer::getGuiTextured, 
                    skin.texture(), 
                    x - 16, y, 
                    40f, 8f, 
                    32, 32, 
                    8, 8, 
                    64, 64
            );
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
