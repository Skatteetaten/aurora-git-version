package ske.aurora.gitversion;

public final class Assert {

    private Assert() {
    }

    public static void notNull(Object o, String msgFormat, Object... args) {
        if (o == null) {
            throw new IllegalArgumentException(String.format(msgFormat, args));
        }
    }
}
