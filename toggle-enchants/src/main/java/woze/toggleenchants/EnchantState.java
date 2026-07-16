package woze.toggleenchants;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnchantState {
    private static int selectedIndex = 0;
    private static ItemStack trackedStack = ItemStack.EMPTY;
    private static ItemStack tooltipStack = ItemStack.EMPTY;

    public static void setTooltipStack(ItemStack stack) {
        tooltipStack = stack;
    }

    public static ItemStack getTooltipStack() {
        return tooltipStack;
    }

    public static List<EnchantInfo> getEnchantments(ItemStack stack) {
        Map<Holder<Enchantment>, EnchantInfo> byHolder = new LinkedHashMap<>();

        ItemEnchantments enchants = stack.getEnchantments();
        for (var entry : enchants.entrySet()) {
            byHolder.put(entry.getKey(), new EnchantInfo(entry.getKey(), entry.getIntValue(), true));
        }

        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag root = customData.copyTag();
            ListTag disabled = root.getListOrEmpty(ToggleEnchants.DISABLED_KEY);
            var registry = Minecraft.getInstance().player.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);
            for (int i = 0; i < disabled.size(); i++) {
                CompoundTag e = disabled.getCompoundOrEmpty(i);
                String id = e.getString("id").orElse("");
                int level = e.getIntOr("level", 1);
                var optHolder = registry.get(Identifier.parse(id));
                if (optHolder.isPresent()) {
                    byHolder.put(optHolder.get(), new EnchantInfo(optHolder.get(), level, false));
                }
            }
        }

        var registry = Minecraft.getInstance().player.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT);
        Optional<HolderSet.Named<Enchantment>> tooltipOrderOpt = registry.get(EnchantmentTags.TOOLTIP_ORDER);

        List<EnchantInfo> result = new ArrayList<>();

        if (tooltipOrderOpt.isPresent()) {
            for (Holder<Enchantment> holder : tooltipOrderOpt.get()) {
                EnchantInfo info = byHolder.remove(holder);
                if (info != null) {
                    result.add(info);
                }
            }
        }

        result.addAll(byHolder.values());

        return result;
    }

    public static int getSelectedIndex(ItemStack stack) {
        if (trackedStack.isEmpty() || stack.getItem() != trackedStack.getItem()) {
            selectedIndex = 0;
            trackedStack = stack.copy();
        }
        int count = getEnchantments(stack).size();
        if (count == 0) return 0;
        return selectedIndex % count;
    }

    public static void scrollSelected(ItemStack stack, int delta) {
        int count = getEnchantments(stack).size();
        if (count <= 1) return;
        if (trackedStack.isEmpty() || stack.getItem() != trackedStack.getItem()) {
            selectedIndex = 0;
            trackedStack = stack.copy();
        }
        selectedIndex = Math.floorMod(selectedIndex + delta, count);
    }

    public static EnchantInfo getSelected(ItemStack stack) {
        List<EnchantInfo> list = getEnchantments(stack);
        if (list.isEmpty()) return null;
        int idx = getSelectedIndex(stack);
        if (idx >= list.size()) return null;
        return list.get(idx);
    }

    public static void resetSelection() {
        selectedIndex = 0;
    }

    public record EnchantInfo(Holder<Enchantment> holder, int level, boolean enabled) {}
}
