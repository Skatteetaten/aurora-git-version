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
        String versionHint = cmd.getOptionValue("version-hint", "");
        List<String> branchesToStipulateReleaseVersionsFor = getCommaSeparatedOptionValue(cmd, "suggest-releases");
        List<String> forcePatchPrefixes = getCommaSeparatedOptionValue(cmd, "force-patch-prefixes");
        List<String> forceMinorPrefixes = getCommaSeparatedOptionValue(cmd, "force-minor-prefixes");

        if (!branchesToStipulateReleaseVersionsFor.isEmpty()) {
            if (versionHint.isEmpty()) {
                throw new IllegalArgumentException("version-hint is required when using suggest-releases");
            }
        }

        SuggesterOptions suggesterOptions = new SuggesterOptions();
        suggesterOptions.setGitRepoPath(path);
        suggesterOptions.setBranchesToInferReleaseVersionsFor(branchesToStipulateReleaseVersionsFor);
        suggesterOptions.setVersionHint(versionHint);
        suggesterOptions.setForcePatchIncrementForBranchPrefixes(forcePatchPrefixes);
        suggesterOptions.setForceMinorIncrementForBranchPrefixes(forceMinorPrefixes);
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
        options.addOption(Option.builder().longOpt("force-patch-prefixes")
            .desc("comma separated list for branch prefixes which will force increase of the versions patch segment"
                + ", leave empty or unused to disable. Only usable together with --suggest-releases")
            .hasArg().build());
        options.addOption(Option.builder().longOpt("force-minor-prefixes")
            .desc("comma separated list for branch prefixes which will force increase of the versions minor segment"
                + ", leave empty or unused to disable. Only usable together with --suggest-releases")
            .hasArg().build());
        return options;
    }

    private static void showHelp(Options options) {

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(HELP_WIDTH);
        formatter.printHelp("java -jar aurora-git-version-cli.jar", options);
    }

    private static List<String> getCommaSeparatedOptionValue(CommandLine cmd, String opt) {
        return commaSeparatedStringToList(cmd.getOptionValue(opt, ""));
    }

    private static List<String> commaSeparatedStringToList(String commaSeparatedString) {
        return Arrays.stream(commaSeparatedString.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}
