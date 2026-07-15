package woze.anvilunlimited;

import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "fuzs.easyanvils.world.inventory.ModAnvilMenu")
public class ModAnvilMenuMixin {

    @Redirect(
        method = "createResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"
        )
    )
    private int removeMaxLevelCap(Enchantment enchantment) {
        return Math.max(enchantment.getMaxLevel(), 10);
    }
}
