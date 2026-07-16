package woze.toggleenchants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ToggleEnchantPayload(int slotIndex, Identifier enchantmentId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleEnchantPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.withDefaultNamespace("toggle_enchant"));

    public static final StreamCodec<FriendlyByteBuf, ToggleEnchantPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.slotIndex);
                buf.writeIdentifier(payload.enchantmentId);
            },
            buf -> new ToggleEnchantPayload(buf.readInt(), buf.readIdentifier())
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
