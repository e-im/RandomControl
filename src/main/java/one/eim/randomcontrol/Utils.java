package one.eim.randomcontrol;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public final class Utils {
    public static <E extends Throwable> void sneakyThrow(final Throwable ex) throws E {
        throw (E) ex;
    }

    public static <T> T sneakyThrows(final ThrowableSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (final Throwable ex) {
            sneakyThrow(ex);
            return null;
        }
    }

    public interface ThrowableSupplier<T> {
        T get() throws Throwable;
    }

    public static @Nullable Class<?> clazz(final String className) {
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    public static <T> T requireNonNullElseGet(T obj, Supplier<? extends T> supplier) {
        return (obj != null) ? obj
                : Objects.requireNonNull(Objects.requireNonNull(supplier, "supplier").get(), "supplier.get()");
    }
}
