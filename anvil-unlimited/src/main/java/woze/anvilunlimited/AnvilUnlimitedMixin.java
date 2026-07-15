package woze.anvilunlimited;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "fuzs.easyanvils.world.inventory.ModAnvilMenu")
public class AnvilUnlimitedMixin {

    @Inject(method = "createResult", at = @At("HEAD"))
    private void setFlag(CallbackInfo ci) {
        AnvilUnlimited.IN_ANVIL.set(true);
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void clearFlag(CallbackInfo ci) {
        AnvilUnlimited.IN_ANVIL.remove();
    }
}
