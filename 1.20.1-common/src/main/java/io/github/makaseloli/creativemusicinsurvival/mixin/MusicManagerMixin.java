package io.github.makaseloli.creativemusicinsurvival.mixin;

import io.github.makaseloli.creativemusicinsurvival.music.CreativeOnlySoundInstance;
import io.github.makaseloli.creativemusicinsurvival.music.DimensionMusicSoundInstance;
import io.github.makaseloli.creativemusicinsurvival.music.DimensionMusicSource;
import io.github.makaseloli.creativemusicinsurvival.music.MusicReplacer;
import io.github.makaseloli.creativemusicinsurvival.music.MusicGenre;
import io.github.makaseloli.creativemusicinsurvival.music.MusicSelectionConfig;
import io.github.makaseloli.creativemusicinsurvival.music.PendingMusicChoice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(MusicManager.class)
public class MusicManagerMixin {
    @Shadow @Nullable private SoundInstance currentMusic;
    @Unique private long creativemusicinsurvival$revision = MusicSelectionConfig.revision();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void creativemusicinsurvival$applySelectionChange(CallbackInfo ci) {
        long revision = MusicSelectionConfig.revision();
        if (revision != this.creativemusicinsurvival$revision) {
            this.creativemusicinsurvival$revision = revision;
            ((MusicManager) (Object) this).stopPlaying();
        }

        if (MusicSelectionConfig.hasEmptySelection() && Minecraft.getInstance().getSituationalMusic() == null) {
            if (this.currentMusic != null) {
                ((MusicManager) (Object) this).stopPlaying();
            }
            ci.cancel();
        }
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
        if (sound.getLocation().equals(SoundEvents.MUSIC_CREATIVE.value().getLocation()) && creativemusicinsurvival$isModifiedSelection()) {
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
