package io.github.makaseloli.creativemusicinsurvival.music;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.world.level.Level;

public final class MusicReplacer {
    private MusicReplacer() {}

    public static Music replace(Music original) {
        PendingMusicChoice.set(null);
        MusicGenre target = targetGenre(original);
        MusicDimension dimension = currentDimension();
        if (target == null) {
            return original;
        }
        if (MusicSelectionConfig.isDefaultSelection(target, dimension)) {
            return original;
        }
        boolean biomeAvailable = isBiomeOrEnvironmentMusic(original);
        MusicPick replacement = MusicSelectionConfig.pickReplacement(target, dimension, biomeAvailable);
        PendingMusicChoice.set(replacement == null ? null : replacement.source());
        if (replacement == null) return null;
        return replacement.source() == DimensionMusicSource.OVERWORLD ? toMusic(original, replacement.choice()) : original;
    }

    private static MusicGenre targetGenre(Music music) {
        if (music == Musics.MENU) {
            return MusicGenre.MENU;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            return minecraft.player.getAbilities().instabuild && minecraft.player.getAbilities().mayfly
                    ? MusicGenre.CREATIVE
                    : MusicGenre.SURVIVAL;
        }
        return null;
    }

    private static boolean isBiomeOrEnvironmentMusic(Music music) {
        return music != Musics.MENU && music != Musics.GAME && music != Musics.CREATIVE;
    }

    public static MusicDimension currentDimension() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return MusicDimension.OVERWORLD;
        if (minecraft.level.dimension() == Level.NETHER) return MusicDimension.NETHER;
        if (minecraft.level.dimension() == Level.END) return MusicDimension.END;
        return MusicDimension.OVERWORLD;
    }

    private static Music toMusic(Music original, MusicChoice choice) {
        if (choice == MusicChoice.BIOME) {
            return original;
        }
        Music selected = switch (choice) {
            case MENU -> Musics.MENU;
            case SURVIVAL -> Musics.GAME;
            case CREATIVE -> Musics.CREATIVE;
            case BIOME -> throw new IllegalStateException("Handled above");
        };
        return new Music(selected.getEvent(), original.getMinDelay(), original.getMaxDelay(), original.replaceCurrentMusic());
    }
}
