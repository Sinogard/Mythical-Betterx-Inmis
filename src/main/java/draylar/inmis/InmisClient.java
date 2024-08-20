package draylar.inmis;

import draylar.inmis.api.TrinketCompat;
import draylar.inmis.client.InmisKeybinds;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.ui.BackpackHandledScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.ColorHelper;

@Environment(EnvType.CLIENT)
public class InmisClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(Inmis.BACKPACK_SCREEN_HANDLER, BackpackHandledScreen::new);
        InmisKeybinds.initialize();

        for (BackpackItem backpack : Inmis.BACKPACKS) {
            if (Inmis.TRINKETS_LOADED) {
                TrinketCompat.registerTrinketRenderer(backpack);
            }
            // Dyable itemtag is empty at this point
            if (backpack.getDefaultStack().getTranslationKey().equals("item.inmis.frayed_backpack") || backpack.getDefaultStack().isIn(ItemTags.DYEABLE)) {
                ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                    if (tintIndex > 0) {
                        return -1;
                    } else {
                        return ColorHelper.Argb.fullAlpha(DyedColorComponent.getColor(stack, -6265536));
                    }
                }, backpack);
            }
        }
    }
}
