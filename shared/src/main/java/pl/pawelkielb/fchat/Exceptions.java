package pl.pawelkielb.fchat;

import java.util.function.Consumer;
import java.util.function.IntFunction;

public abstract class Exceptions {
    @SuppressWarnings("unchecked")
    private static <E extends Exception> void throwAsUnchecked(Exception exception) throws E {
        throw (E) exception;
    }

    @FunctionalInterface
    public interface IntFunction_WithExceptions<R, E extends Exception> {
        R apply(int value) throws E;
    }

    @FunctionalInterface
    public interface Runnable_WithExceptions<E extends Exception> {
        void run() throws E;
    }

    @FunctionalInterface
    public interface Consumer_WithExceptions<T, E extends Exception> {
        void accept(T t) throws E;
    }

    public static <R, E extends Exception> IntFunction<R> i(IntFunction_WithExceptions<R, E> fn) {
        return (t) -> {
            try {
                return fn.apply(t);
            } catch (Exception e) {
                throwAsUnchecked(e);
                return null;
            }
        };
    }

    public static <E extends Exception> Runnable r(Runnable_WithExceptions<E> fn) {
        return () -> {
            try {
                fn.run();
            } catch (Exception e) {
                throwAsUnchecked(e);
            }
        };
    }

    public static <T, E extends Exception> Consumer<T> c(Consumer_WithExceptions<T, E> fn) {
        return (t) -> {
            try {
                fn.accept(t);
            } catch (Exception e) {
                throwAsUnchecked(e);
            }
        };
    }
}
