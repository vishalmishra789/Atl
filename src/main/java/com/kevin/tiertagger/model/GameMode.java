package com.kevin.tiertagger.model;

import com.kevin.tiertagger.TierTagger;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record GameMode(String id, String title) {
    public static final GameMode NONE = new GameMode("none_id", "§cNone§r");

    /**
     * Since Supabase doesn't have a "mode list" endpoint, we hardcode your server's modes 
     * here so they appear in the mod's settings menu.
     */
    public static CompletableFuture<List<GameMode>> fetchGamemodes(HttpClient client) {
        return CompletableFuture.supplyAsync(() -> {
            List<GameMode> modes = new ArrayList<>();
            modes.add(new GameMode("crystal", "Crystal"));
            modes.add(new GameMode("diapot", "DiaPot"));
            modes.add(new GameMode("mace", "Mace"));
            modes.add(new GameMode("sword", "Sword"));
            modes.add(new GameMode("uhc", "UHC"));
            modes.add(new GameMode("nethpot", "Nethpot"));
            modes.add(new GameMode("smp", "SMP"));
            return modes;
        });
    }

    public boolean isNone() {
        return this.id.equals(NONE.id);
    }

    private Pair<Character, TextColor> iconAndColor() {
        return switch (this.id.toLowerCase()) {
            // Changed Vanilla -> Crystal
            case "crystal" -> new Pair<>('\uE805', TextColor.fromFormatting(Formatting.AQUA));
            
            // Changed Pot -> Diapot
            case "diapot", "pot" -> new Pair<>('\uE704', TextColor.fromRgb(0xff0000));
            
            case "mace" -> new Pair<>('\uE702', TextColor.fromFormatting(Formatting.GRAY));
            case "nethpot", "neth_pot" -> new Pair<>('\uE703', TextColor.fromRgb(0x7d4a40));
            case "sword" -> new Pair<>('\uE706', TextColor.fromRgb(0xa4fdf0));
            case "uhc" -> new Pair<>('\uE707', TextColor.fromFormatting(Formatting.RED));
            case "smp" -> new Pair<>('\uE705', TextColor.fromRgb(0xeccb45));
            case "axe" -> new Pair<>('\uE701', TextColor.fromFormatting(Formatting.GREEN));
            
            default -> new Pair<>('•', TextColor.fromFormatting(Formatting.WHITE));
        };
    }

    public Optional<Character> icon() {
        Pair<Character, TextColor> pair = this.iconAndColor();
        return pair.getRight().getRgb() == 0xFFFFFF ? Optional.empty() : Optional.of(pair.getLeft());
    }

    public Text asStyled(boolean withDefaultDot) {
        Pair<Character, TextColor> pair = this.iconAndColor();
        if (pair.getRight().getRgb() == 0xFFFFFF && !withDefaultDot) {
            return Text.of(this.title);
        } else {
            Text name = Text.literal(this.title).styled(s -> s.withColor(pair.getRight()));
            return Text.literal(pair.getLeft() + " ").append(name);
        }
    }
}
