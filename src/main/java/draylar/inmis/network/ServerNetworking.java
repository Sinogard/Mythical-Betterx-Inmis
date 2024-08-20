package draylar.inmis.network;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import draylar.inmis.Inmis;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.item.EnderBackpackItem;
import draylar.inmis.mixin.trinkets.TrinketsMixinPlugin;
import draylar.inmis.network.packet.BackpackPacket;
import draylar.inmis.network.packet.BackpackScreenPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ServerNetworking {

    public static void init() {
        PayloadTypeRegistry.playC2S().register(BackpackPacket.PACKET_ID, BackpackPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(BackpackScreenPacket.PACKET_ID, BackpackScreenPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BackpackPacket.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                if (TrinketsMixinPlugin.isTrinketsLoaded && Inmis.CONFIG.enableTrinketCompatibility) {
                    Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(context.player());

                    // Iterate over the player's Trinket inventory.
                    // Once a backpack has been found, open it.
                    if (component.isPresent()) {
                        List<Pair<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
                        for (Pair<SlotReference, ItemStack> entry : allEquipped) {
                            if (entry.getRight().getItem() instanceof BackpackItem) {
                                BackpackItem.openScreen(context.player(), entry.getRight());
                                return;
                            }
                        }
                    }
                }

                // Depending on whether the "disallow main inventory backpacks" option is set, either look through all inventory slots, or only the player's armor slots.
                Stream<ItemStack> inventoryItems = !Inmis.CONFIG.requireArmorTrinketToOpen
                        ? Stream.concat(Stream.concat(context.player().getInventory().offHand.stream(), context.player().getInventory().main.stream()), context.player().getInventory().armor.stream())
                        : context.player().getInventory().armor.stream();

                ItemStack firstBackpackItemStack = inventoryItems
                        .filter((itemStack) -> itemStack.getItem() instanceof BackpackItem)
                        .findFirst()
                        .orElse(ItemStack.EMPTY);
                if (firstBackpackItemStack != ItemStack.EMPTY) {
                    BackpackItem.openScreen(context.player(), firstBackpackItemStack);
                } else {
                    for (int u = 0; u < context.player().getInventory().size(); u++) {
                        if (context.player().getInventory().getStack(u).isOf(Inmis.ENDER_POUCH)) {
                            EnderChestInventory enderChestInventory = context.player().getEnderChestInventory();
                            context.player().openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                                    GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, enderChestInventory), Text.translatable("container.enderchest")));
                            context.player().incrementStat(Stats.OPEN_ENDERCHEST);
                            break;
                        }
                    }
                }
            });
        });
    }
}
