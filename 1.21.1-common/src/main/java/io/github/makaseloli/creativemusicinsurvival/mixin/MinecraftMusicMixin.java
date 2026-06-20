package io.github.makaseloli.creativemusicinsurvival.mixin;

import io.github.makaseloli.creativemusicinsurvival.music.MusicReplacer;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMusicMixin {
    @Inject(method = "getSituationalMusic", at = @At("RETURN"), cancellable = true)
    private void creativemusicinsurvival$replaceBaseMusic(CallbackInfoReturnable<Music> cir) {
        cir.setReturnValue(MusicReplacer.replace(cir.getReturnValue()));
    }
}
