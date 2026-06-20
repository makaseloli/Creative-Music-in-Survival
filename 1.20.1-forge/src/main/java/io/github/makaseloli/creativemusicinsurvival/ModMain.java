package io.github.makaseloli.creativemusicinsurvival;

import io.github.makaseloli.creativemusicinsurvival.web.ClientLanguageSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MODID)
public class ModMain {
    public ModMain(FMLJavaModLoadingContext ctx) {
        Constants.LOGGER.debug(Constants.INITIALIZING, ModUtils.loc("1.20.1-forge"));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CreativeMusicInSurvival.init(ClientLanguageSupplier::get);
        }
    }
}
