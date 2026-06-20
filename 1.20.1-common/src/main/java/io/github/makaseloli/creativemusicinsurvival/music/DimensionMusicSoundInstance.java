package io.github.makaseloli.creativemusicinsurvival.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class DimensionMusicSoundInstance extends SimpleSoundInstance {
    private DimensionMusicSoundInstance(ResourceLocation location) {
        super(location, SoundSource.MUSIC, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, Attenuation.NONE, 0.0, 0.0, 0.0, true);
    }

    public static SimpleSoundInstance forMusic(DimensionMusicSource choice, SoundEvent fallback) {
        List<ResourceLocation> candidates = Minecraft.getInstance().getSoundManager().getAvailableSounds().stream()
                .filter(location -> matches(choice, location.getPath()))
                .toList();
        return candidates.isEmpty()
                ? SimpleSoundInstance.forMusic(fallback)
                : new DimensionMusicSoundInstance(candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())));
    }

    private static boolean matches(DimensionMusicSource choice, String path) {
        return switch (choice) {
            case OVERWORLD -> false;
            case NETHER -> path.startsWith("music.nether.");
            case END -> path.equals("music.end") || path.startsWith("music.end.");
            default -> false;
        };
    }
}
