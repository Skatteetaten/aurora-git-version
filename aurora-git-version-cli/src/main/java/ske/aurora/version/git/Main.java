package ske.aurora.version.git;

import java.io.File;
import java.io.IOException;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {

        String path = ".";
        if (args.length == 1) {
            path = args[0];
        }
        System.out.print(GitVersion.determineVersion(new File(path)).getVersion());
    }
}
