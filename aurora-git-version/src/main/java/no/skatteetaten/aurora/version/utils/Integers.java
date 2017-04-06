package no.skatteetaten.aurora.version.utils;

import java.util.concurrent.Callable;

public final class Integers {
    private Integers() {

    }

    public static void times(int times, Callable callable) {

        for (int i = 0; i < times; i++) {
            try {
                callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
