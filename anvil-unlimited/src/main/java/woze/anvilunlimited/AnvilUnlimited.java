package woze.anvilunlimited;

public class AnvilUnlimited {
    public static final ThreadLocal<Boolean> IN_ANVIL = ThreadLocal.withInitial(() -> false);
}
