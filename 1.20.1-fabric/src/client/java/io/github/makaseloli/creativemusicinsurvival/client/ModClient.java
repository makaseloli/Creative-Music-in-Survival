package io.github.makaseloli.creativemusicinsurvival.client;

import io.github.makaseloli.creativemusicinsurvival.CreativeMusicInSurvival;
import io.github.makaseloli.creativemusicinsurvival.web.ClientLanguageSupplier;
import net.fabricmc.api.ClientModInitializer;

public class ModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CreativeMusicInSurvival.init(ClientLanguageSupplier::get);
    }
}
