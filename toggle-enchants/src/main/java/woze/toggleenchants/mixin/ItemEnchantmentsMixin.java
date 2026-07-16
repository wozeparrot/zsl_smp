package woze.toggleenchants.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import woze.toggleenchants.EnchantState;
import woze.toggleenchants.ToggleEnchants;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ItemEnchantments.class)
public class ItemEnchantmentsMixin {

    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private void toggleEnchants$rebuildTooltip(
        Item.TooltipContext context,
        Consumer<Component> consumer,
        TooltipFlag flag,
        DataComponentGetter getter,
        CallbackInfo ci
    ) {
        ItemEnchantments self = (ItemEnchantments)(Object)this;

        HolderLookup.Provider registries = context.registries();
        if (registries == null) return;

        var registry = registries.lookupOrThrow(Registries.ENCHANTMENT);

        Map<Holder<Enchantment>, EnchantEntry> allEnchants = new LinkedHashMap<>();

        for (var entry : self.entrySet()) {
            allEnchants.put(entry.getKey(), new EnchantEntry(entry.getKey(), entry.getIntValue(), true));
        }

        CustomData customData = getter.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag root = customData.copyTag();
            ListTag disabledList = root.getListOrEmpty(ToggleEnchants.DISABLED_KEY);
            for (int i = 0; i < disabledList.size(); i++) {
                CompoundTag entry = disabledList.getCompoundOrEmpty(i);
                String id = entry.getString("id").orElse("");
                int level = entry.getIntOr("level", 1);
                var optHolder = registry.get(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(id)));
                if (optHolder.isPresent()) {
                    allEnchants.put(optHolder.get(), new EnchantEntry(optHolder.get(), level, false));
                }
            }
        }

        ItemStack tooltipStack = EnchantState.getTooltipStack();
        boolean isHovered = !tooltipStack.isEmpty() && tooltipStack.getEnchantments() == self;
        var selected = isHovered ? EnchantState.getSelected(tooltipStack) : null;

        Optional<HolderSet.Named<Enchantment>> tooltipOrderOpt = registry.get(EnchantmentTags.TOOLTIP_ORDER);

        if (tooltipOrderOpt.isPresent()) {
            for (Holder<Enchantment> holder : tooltipOrderOpt.get()) {
                EnchantEntry entry = allEnchants.remove(holder);
                if (entry != null) {
                    consumer.accept(formatEnchant(entry, selected));
                }
            }
        }

        for (EnchantEntry entry : allEnchants.values()) {
            consumer.accept(formatEnchant(entry, selected));
        }

        ci.cancel();
    }

    private static Component formatEnchant(EnchantEntry entry, EnchantState.EnchantInfo selected) {
        Component fullname = Enchantment.getFullname(entry.holder(), entry.level());
        boolean isSelected = selected != null && selected.holder().equals(entry.holder());

        MutableComponent line = Component.literal("").append(fullname);

        if (!entry.enabled()) {
            line = line.withStyle(net.minecraft.ChatFormatting.STRIKETHROUGH);
        }

        if (isSelected) {
            net.minecraft.ChatFormatting color = entry.enabled()
                ? net.minecraft.ChatFormatting.YELLOW
                : net.minecraft.ChatFormatting.RED;
            line = Component.literal("> ").append(line).withStyle(color);
        }

        return line;
    }

    private record EnchantEntry(Holder<Enchantment> holder, int level, boolean enabled) {}
}
