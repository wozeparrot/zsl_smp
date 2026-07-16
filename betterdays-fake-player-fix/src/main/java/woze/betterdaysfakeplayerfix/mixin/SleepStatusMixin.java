package woze.betterdaysfakeplayerfix.mixin;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "betterdays.time.SleepStatus")
public class SleepStatusMixin {

    @Shadow
    protected int activePlayerCount;

    @Shadow
    protected int sleepingPlayerCount;

    @Inject(method = "updatePlayerCounts", at = @At("HEAD"), cancellable = true)
    private void betterdaysfakeplayerfix$filterFakePlayers(List<ServerPlayer> playerList, CallbackInfo ci) {
        activePlayerCount = 0;
        sleepingPlayerCount = 0;

        for (ServerPlayer player : playerList) {
            if (!player.isSpectator() && !isFakePlayer(player)) {
                activePlayerCount++;
                if (player.isSleeping()) {
                    sleepingPlayerCount++;
                }
            }
        }

        ci.cancel();
    }

    private static final Class<?> FAKE_PLAYER_CLASS;

    static {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("carpet.patches.EntityPlayerMPFake");
        } catch (ClassNotFoundException ignored) {
        }
        FAKE_PLAYER_CLASS = clazz;
    }

    private static boolean isFakePlayer(ServerPlayer player) {
        return FAKE_PLAYER_CLASS != null && FAKE_PLAYER_CLASS.isInstance(player);
    }
}
