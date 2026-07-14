package woze.fastturtles;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "dan200.computercraft.shared.turtle.core.TurtleBrain")
public class FastTurtlesMixin {

    @ModifyConstant(method = "updateAnimation", constant = @Constant(intValue = 8), require = 1)
    private int modifyAnimDurationInt(int original) {
        return 2;
    }

    @ModifyConstant(method = "updateAnimation", constant = @Constant(floatValue = 8.0f), require = 0)
    private float modifyAnimDurationFloat(float original) {
        return 2.0f;
    }

    @ModifyConstant(method = "updateAnimation", constant = @Constant(floatValue = 0.125f), require = 0)
    private float modifyPushStep(float original) {
        return 0.5f;
    }

    @ModifyConstant(method = "getAnimationFraction", constant = @Constant(floatValue = 8.0f), require = 1)
    private float modifyAnimDurationFraction(float original) {
        return 2.0f;
    }

    @ModifyConstant(method = "playAnimation", constant = @Constant(intValue = 4), require = 0)
    private int modifyShortWaitProgress(int original) {
        return 1;
    }
}
