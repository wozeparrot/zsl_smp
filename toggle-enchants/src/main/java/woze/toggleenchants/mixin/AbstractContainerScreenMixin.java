package woze.toggleenchants.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import woze.toggleenchants.EnchantMouseAction;
import woze.toggleenchants.EnchantState;
import woze.toggleenchants.ToggleEnchantPayload;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Shadow
    protected void addItemSlotMouseAction(ItemSlotMouseAction action) {
        throw new AssertionError();
    }

    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "init", at = @At("RETURN"))
    private void toggleEnchants$addEnchantMouseAction(CallbackInfo ci) {
        addItemSlotMouseAction(new EnchantMouseAction());
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"))
    private void toggleEnchants$trackTooltipStack(GuiGraphicsExtractor g, int mouseX, int mouseY, CallbackInfo ci) {
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            EnchantState.setTooltipStack(hoveredSlot.getItem());
        } else {
            EnchantState.setTooltipStack(ItemStack.EMPTY);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void toggleEnchants$onRightClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() != 1) return;
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>)(Object)this;
        Slot hoveredSlot = this.hoveredSlot;
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;
        ItemStack stack = hoveredSlot.getItem();
        if (stack.isEmpty()) return;

        var selected = EnchantState.getSelected(stack);
        if (selected == null) return;

        var enchantId = selected.holder().unwrapKey().orElseThrow().identifier();

        int slotIndex;
        if (hoveredSlot.container == Minecraft.getInstance().player.getInventory()) {
            slotIndex = hoveredSlot.getContainerSlot();
        } else {
            slotIndex = -1;
        }

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
            new ToggleEnchantPayload(slotIndex, enchantId)
        );
        cir.setReturnValue(true);
    }
}
