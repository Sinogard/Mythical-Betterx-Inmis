package draylar.inmis.mixin;

import draylar.inmis.Inmis;
import draylar.inmis.config.BackpackInfo;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.item.component.BackpackComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ShapedRecipe.class)

public abstract class ShapedRecipeMixin {
    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void tablecraftMixin(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup, CallbackInfoReturnable<ItemStack> info) {
        // get both backpacks
        if (craftingRecipeInput.getSize() > 4) {
            ItemStack centerSlotItemStack = craftingRecipeInput.getStackInSlot(4);

            // only attempt to apply nbt if the center stack of the original recipe was a backpack
            if (centerSlotItemStack.getItem() instanceof BackpackItem && !Inmis.isBackpackEmpty(centerSlotItemStack)) {
                ItemStack newBackpackItemStack = this.getResult(wrapperLookup).copy();
                if (newBackpackItemStack.getItem() instanceof BackpackItem backpackItem) {
                    SimpleInventory simpleInventory = new SimpleInventory(backpackItem.getTier().getRowWidth() * backpackItem.getTier().getNumberOfRows());
                    SimpleInventory oldInventory = centerSlotItemStack.get(Inmis.BACKPACK_COMPONENT).getSimpleInventory();
                    for (int i = 0; i < oldInventory.size(); i++) {
                        simpleInventory.setStack(i, oldInventory.getStack(i));
                    }

                    newBackpackItemStack.set(Inmis.BACKPACK_COMPONENT, new BackpackComponent(simpleInventory));
                    info.setReturnValue(newBackpackItemStack);
                }
            }

        }
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void smithcraftMixin(ItemStack base, ItemStack input, RegistryWrapper.WrapperLookup wrapperLookup, CallbackInfoReturnable<ItemStack> info) {
        // Check if the base item is a backpack and has NBT data
        if (base.getItem() instanceof BackpackItem && !Inmis.isBackpackEmpty(base)) {
            ItemStack newBackpackItemStack = this.getResult(wrapperLookup).copy();
            if ((newBackpackItemStack.getItem() instanceof BackpackItem backpackItem)) {
                SimpleInventory simpleInventory = new SimpleInventory(backpackItem.getTier().getRowWidth() * backpackItem.getTier().getNumberOfRows());
                SimpleInventory oldInventory = base.get(Inmis.BACKPACK_COMPONENT).getSimpleInventory();
            // Transfer inventory from the input (base) backpack to the output backpack
                for (int i = 0; i < oldInventory.size(); i++) {
                 simpleInventory.setStack(i, oldInventory.getStack(i));
                }

                newBackpackItemStack.set(Inmis.BACKPACK_COMPONENT, new BackpackComponent(simpleInventory));
                info.setReturnValue(newBackpackItemStack);
            }
        }
    }
    @Shadow
    public abstract ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup);
}
