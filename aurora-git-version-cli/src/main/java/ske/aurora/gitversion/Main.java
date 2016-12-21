package ske.aurora.gitversion;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {

        Arrays.asList(args).forEach(System.out::println);
        String path = ".";
        if (args.length == 1) {
            path = args[0];
        }
        System.out.print(GitVersion.determineVersion(new File(path)));
    }
}
