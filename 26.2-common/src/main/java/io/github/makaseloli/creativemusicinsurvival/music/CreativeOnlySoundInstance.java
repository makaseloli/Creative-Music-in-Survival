package io.github.makaseloli.creativemusicinsurvival.music;

import io.github.makaseloli.creativemusicinsurvival.mixin.WeighedSoundEventsAccessor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public final class CreativeOnlySoundInstance extends SimpleSoundInstance {
    private CreativeOnlySoundInstance(SoundEvent sound) {
        super(sound.location(), SoundSource.MUSIC, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, Attenuation.NONE, 0.0, 0.0, 0.0, true);
    }

    public static SimpleSoundInstance forMusic(SoundEvent sound) {
        return new CreativeOnlySoundInstance(sound);
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager soundManager) {
        WeighedSoundEvents event = soundManager.getSoundEvent(this.getIdentifier());
        if (event == null) {
            this.sound = SoundManager.EMPTY_SOUND;
            return null;
        }
        int totalWeight = 0;
        for (Weighted<Sound> entry : ((WeighedSoundEventsAccessor) event).creativemusicinsurvival$entries()) {
            if (entry instanceof Sound) {
                totalWeight += entry.getWeight();
            }
        }
        if (totalWeight == 0) {
            this.sound = SoundManager.EMPTY_SOUND;
            return event;
        }
        int selectedWeight = this.random.nextInt(totalWeight);
        for (Weighted<Sound> entry : ((WeighedSoundEventsAccessor) event).creativemusicinsurvival$entries()) {
            if (entry instanceof Sound) {
                selectedWeight -= entry.getWeight();
                if (selectedWeight < 0) {
                    this.sound = entry.getSound(this.random);
                    return event;
                }
            }
        }
        this.sound = SoundManager.EMPTY_SOUND;
        return event;
    }
}
