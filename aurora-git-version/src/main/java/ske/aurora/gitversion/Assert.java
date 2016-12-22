package ske.aurora.gitversion;

public class Assert {
    public static void notNull(Object o, String msgFormat, Object... args) {
        if (o == null) {
            throw new IllegalArgumentException(String.format(msgFormat, args));
        }
    }
}
