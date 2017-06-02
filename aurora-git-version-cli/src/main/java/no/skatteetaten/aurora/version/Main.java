package no.skatteetaten.aurora.version;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


public final class Main {

    public static final int HELP_WIDTH = 150;

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

        SuggesterOptions suggesterOptions;
        try {
            suggesterOptions = createSuggesterOptionsFromApplicationArgs(cmd);
        } catch (IllegalArgumentException e) {
            showHelp(options);
            return;
        }

        System.out.println(VersionNumberSuggester.suggestVersion(suggesterOptions));
    }

    private static SuggesterOptions createSuggesterOptionsFromApplicationArgs(CommandLine cmd) {

        String path = cmd.getOptionValue("p", "./");
        String suggestReleasesCsv = cmd.getOptionValue("suggest-releases", "");
        List<String> branchesToStipulateReleaseVersionsFor = Arrays.stream(suggestReleasesCsv.
            split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
        String versionHint = cmd.getOptionValue("version-hint", "");
        if (!branchesToStipulateReleaseVersionsFor.isEmpty()) {
            if (versionHint.isEmpty()) {
                throw new IllegalArgumentException("version-hint is required when using suggest-releases");
            }
        }

        SuggesterOptions suggesterOptions = new SuggesterOptions();
        suggesterOptions.setGitRepoPath(path);
        suggesterOptions.setBranchesToInferReleaseVersionsFor(branchesToStipulateReleaseVersionsFor);
        suggesterOptions.setVersionHint(versionHint);

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
        options.addOption(Option.builder().longOpt("version-hint")
            .desc("the version hint to use when suggesting the next release version "
                + "- required when using --suggest-releases")
            .hasArg()
            .build());
        return options;
    }

    private static void showHelp(Options options) {

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(HELP_WIDTH);
        formatter.printHelp("java -jar aurora-git-version-cli.jar", options);
    }
}
