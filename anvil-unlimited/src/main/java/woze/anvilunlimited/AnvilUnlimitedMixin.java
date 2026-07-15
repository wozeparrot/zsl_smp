package woze.anvilunlimited;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import woze.anvilcore.AnvilUnlimited;

@Mixin(targets = "fuzs.easyanvils.common.world.inventory.ModAnvilMenu")
public class AnvilUnlimitedMixin {

    @Inject(method = "createAnvilResult", at = @At("HEAD"))
    private void setFlag(CallbackInfo ci) {
        AnvilUnlimited.IN_ANVIL.set(true);
    }

    @Inject(method = "createAnvilResult", at = @At("RETURN"))
    private void clearFlag(CallbackInfo ci) {
        AnvilUnlimited.IN_ANVIL.remove();
    }
}
