package io.github.makaseloli.creativemusicinsurvival.client;

import io.github.makaseloli.creativemusicinsurvival.CreativeMusicInSurvival;
import io.github.makaseloli.creativemusicinsurvival.web.ClientLanguageSupplier;
import io.github.makaseloli.creativemusicinsurvival.web.NowPlayingState;
import net.fabricmc.api.ClientModInitializer;

public class ModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NowPlayingState.enable();
        CreativeMusicInSurvival.init(ClientLanguageSupplier::get);
    }
}
