package woze.toggleenchants;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class ToggleEnchants implements ModInitializer {
    public static final String DISABLED_KEY = "toggle_enchants_disabled";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(
            ToggleEnchantPayload.TYPE,
            ToggleEnchantPayload.STREAM_CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
            ToggleEnchantPayload.TYPE,
            (payload, context) -> {
                ServerPlayer player = context.player();
                context.server().execute(() -> {
                    ItemStack stack = getStack(player, payload.slotIndex());
                    if (stack.isEmpty()) return;

                    var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                    var optHolder = registry.get(payload.enchantmentId());
                    if (optHolder.isEmpty()) return;
                    Holder<Enchantment> enchantment = optHolder.get();

                    ItemEnchantments current = EnchantmentHelper.getEnchantmentsForCrafting(stack);
                    int level = current.getLevel(enchantment);

                    if (level > 0) {
                        disableEnchantment(stack, enchantment, level);
                    } else {
                        restoreEnchantment(stack, enchantment);
                    }

                    player.getInventory().setChanged();
                    player.containerMenu.broadcastChanges();
                });
            }
        );
    }

    private static ItemStack getStack(ServerPlayer player, int slotIndex) {
        if (slotIndex == -1) {
            return player.getMainHandItem();
        }
        if (slotIndex < 0 || slotIndex >= player.getInventory().getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return player.getInventory().getItem(slotIndex);
    }

    private static void disableEnchantment(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.removeIf(h -> h.equals(enchantment)));

        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag list = root.getListOrEmpty(DISABLED_KEY);
        CompoundTag entry = new CompoundTag();
        entry.putString("id", enchantment.unwrapKey().orElseThrow().identifier().toString());
        entry.putInt("level", level);
        list.addAndUnwrap(entry);
        root.put(DISABLED_KEY, list);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, root);
    }

    private static void restoreEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        String id = enchantment.unwrapKey().orElseThrow().identifier().toString();
        int level = 1;
        boolean found = false;

        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag list = root.getListOrEmpty(DISABLED_KEY);
        ListTag newList = new ListTag();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            if (entry.getString("id").orElse("").equals(id)) {
                level = entry.getIntOr("level", 1);
                found = true;
            } else {
                newList.addAndUnwrap(entry);
            }
        }

        if (!found) return;

        if (newList.isEmpty()) {
            root.remove(DISABLED_KEY);
        } else {
            root.put(DISABLED_KEY, newList);
        }
        CustomData.set(DataComponents.CUSTOM_DATA, stack, root);

        final int finalLevel = level;
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, finalLevel));
    }
}
