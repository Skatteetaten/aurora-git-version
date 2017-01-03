package ske.aurora.version.git;

import java.io.IOException;

import ske.aurora.version.VersionNumberSuggester;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {

        String path = "./";
        if (args.length == 1) {
            path = args[0];
        }
        VersionNumberSuggester.Options options = new VersionNumberSuggester.Options();
        options.setGitRepoPath(path);
        System.out.println(VersionNumberSuggester.suggestVersion(options));
    }
}
