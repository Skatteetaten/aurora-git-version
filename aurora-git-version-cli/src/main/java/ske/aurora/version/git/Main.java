package ske.aurora.version.git;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import ske.aurora.version.SuggesterOptions;
import ske.aurora.version.VersionNumberSuggester;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {

        Options options = createApplicationOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("h")) {
            showHelp(options);
            return;
        }

        SuggesterOptions suggesterOptions = createSuggesterOptionsFromApplicationArgs(cmd);

        System.out.println(VersionNumberSuggester.suggestVersion(suggesterOptions));
    }

    private static SuggesterOptions createSuggesterOptionsFromApplicationArgs(CommandLine cmd) {

        String path = cmd.getOptionValue("p", "./");
        String suggestReleasesCsv = cmd.getOptionValue("suggest-releases", "");
        List<String> branchesToStipulateReleaseVersionsFor = Arrays.stream(suggestReleasesCsv.
            split(","))
            .map(String::trim)
            .collect(Collectors.toList());

        SuggesterOptions suggesterOptions = new SuggesterOptions();
        suggesterOptions.setGitRepoPath(path);
        suggesterOptions.setBranchesToInferReleaseVersionsFor(branchesToStipulateReleaseVersionsFor);
        
        return suggesterOptions;
    }

    private static Options createApplicationOptions() {

        Options options = new Options();
        options.addOption("p", "path", true, "the path to the git repository");
        options.addOption("h", "help", false, "display help");
        options.addOption(Option.builder().longOpt("suggest-releases")
            .desc("comma separated list of branches for which to suggest release versions")
            .hasArg()
            .argName("BRANCH-CSV")
            .build());
        return options;
    }

    private static void showHelp(Options options) {

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(150);
        formatter.printHelp("java -jar aurora-git-version-cli.jar", options);
    }
}
