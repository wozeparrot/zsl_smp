package woze.toggleenchants;

import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EnchantMouseAction implements ItemSlotMouseAction {
    private final ScrollWheelHandler scrollWheelHandler = new ScrollWheelHandler();

    @Override
    public boolean matches(Slot slot) {
        return slot.hasItem() && hasEnchantments(slot.getItem());
    }

    private static boolean hasEnchantments(ItemStack stack) {
        if (!stack.getEnchantments().isEmpty()) return true;
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.copyTag().contains(ToggleEnchants.DISABLED_KEY);
    }

    @Override
    public boolean onMouseScrolled(double deltaX, double deltaY, int slotIndex, ItemStack stack) {
        int count = EnchantState.getEnchantments(stack).size();
        if (count <= 1) return false;

        var scroll = scrollWheelHandler.onMouseScroll(deltaX, deltaY);
        int scrollDelta = scroll.y != 0 ? -scroll.y : scroll.x;
        if (scrollDelta == 0) return false;

        EnchantState.scrollSelected(stack, scrollDelta);
        return true;
    }

    @Override
    public void onStopHovering(Slot slot) {
        EnchantState.resetSelection();
    }

    @Override
    public void onSlotClicked(Slot slot, ContainerInput input) {
    }
}
