package io.github.makaseloli.creativemusicinsurvival.mixin;

import io.github.makaseloli.creativemusicinsurvival.music.CreativeOnlySoundInstance;
import io.github.makaseloli.creativemusicinsurvival.music.DimensionMusicSoundInstance;
import io.github.makaseloli.creativemusicinsurvival.music.DimensionMusicSource;
import io.github.makaseloli.creativemusicinsurvival.music.MusicReplacer;
import io.github.makaseloli.creativemusicinsurvival.music.MusicGenre;
import io.github.makaseloli.creativemusicinsurvival.music.MusicSelectionConfig;
import io.github.makaseloli.creativemusicinsurvival.music.PendingMusicChoice;
import io.github.makaseloli.creativemusicinsurvival.web.NowPlayingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public class MusicManagerMixin {
    @Unique private long creativemusicinsurvival$revision = MusicSelectionConfig.revision();

    @Inject(method = "tick", at = @At("HEAD"))
    private void creativemusicinsurvival$applySelectionChange(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        MusicManager manager = (MusicManager) (Object) this;
        if (NowPlayingState.consumeSkipRequest()) {
            manager.stopPlaying();
            Music next = minecraft.getSituationalMusic();
            if (next != null) {
                manager.startPlaying(next);
            }
        }

        long revision = MusicSelectionConfig.revision();
        if (revision == this.creativemusicinsurvival$revision) {
            return;
        }

        this.creativemusicinsurvival$revision = revision;
        manager.stopPlaying();
        manager.setMinutesBetweenSongs(minecraft.options.musicFrequency().get());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void creativemusicinsurvival$publishNowPlaying(CallbackInfo ci) {
        String key = ((MusicManager) (Object) this).getCurrentMusicTranslationKey();
        if (key == null) {
            NowPlayingState.clear();
            return;
        }
        String translationKey = key.replace('/', '.');
        NowPlayingState.update(Language.getInstance().getOrDefault(translationKey), translationKey);
    }

    @Redirect(
            method = "startPlaying",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forMusic(Lnet/minecraft/sounds/SoundEvent;)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;")
    )
    private SimpleSoundInstance creativemusicinsurvival$filterCreativeMusic(SoundEvent sound, Music music) {
        DimensionMusicSource choice = PendingMusicChoice.consume();
        if (choice != null) {
            return DimensionMusicSoundInstance.forMusic(choice, sound);
        }
        if (sound.location().equals(SoundEvents.MUSIC_CREATIVE.value().location()) && creativemusicinsurvival$isModifiedSelection()) {
            return CreativeOnlySoundInstance.forMusic(sound);
        }
        return SimpleSoundInstance.forMusic(sound);
    }

    @Unique
    private static boolean creativemusicinsurvival$isModifiedSelection() {
        Minecraft minecraft = Minecraft.getInstance();
        MusicGenre target = minecraft.player == null
                ? MusicGenre.MENU
                : minecraft.player.getAbilities().instabuild && minecraft.player.getAbilities().mayfly
                        ? MusicGenre.CREATIVE
                        : MusicGenre.SURVIVAL;
        return !MusicSelectionConfig.isDefaultSelection(target, MusicReplacer.currentDimension());
    }
}
