package woze.anvilcore;

public class AnvilUnlimited {
    public static final ThreadLocal<Boolean> IN_ANVIL = ThreadLocal.withInitial(() -> false);
}
