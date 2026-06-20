package io.github.makaseloli.creativemusicinsurvival.client;

import io.github.makaseloli.creativemusicinsurvival.CreativeMusicInSurvival;
import io.github.makaseloli.creativemusicinsurvival.Constants;
import io.github.makaseloli.creativemusicinsurvival.web.ClientLanguageSupplier;
import io.github.makaseloli.creativemusicinsurvival.web.NowPlayingState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Constants.MODID, dist = Dist.CLIENT)
public class ModClient {
    public ModClient() {
        NowPlayingState.enable();
        CreativeMusicInSurvival.init(ClientLanguageSupplier::get);
    }
}
