package woze.anvilunlimited;

import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import woze.anvilcore.AnvilUnlimited;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @Inject(
        method = "getMaxLevel",
        at = @At("RETURN"),
        cancellable = true
    )
    private void capAtTen(CallbackInfoReturnable<Integer> cir) {
        if (AnvilUnlimited.IN_ANVIL.get()) {
            cir.setReturnValue(Math.max(cir.getReturnValue(), 10));
        }
    }
}
