package no.skatteetaten.aurora.version;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import no.skatteetaten.aurora.version.suggest.VersionSegment;

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
        Optional<String> incrementForExistingTag = getOptionalOptionValue(cmd, "increment-for-existing-tag", "default");

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
        suggesterOptions.setTryDeterminingCurrentVersionFromTagName(!incrementForExistingTag.isPresent());
        suggesterOptions.setForceSegmentIncrementForExistingTag(
            incrementForExistingTag.flatMap(value -> readEnumStringIgnoringCase(value, VersionSegment.class)));

        return suggesterOptions;
    }

    private static Options createApplicationOptions() {

        Options options = new Options();
        options.addOption("p", "path", true, "The path to the git repository");
        options.addOption("h", "help", false, "Display help");

        options.addOption(Option.builder().longOpt("suggest-releases")
            .desc("Comma separated list of branches for which to suggest release versions")
            .hasArg()
            .argName("BRANCH-CSV")
            .build());

        options.addOption(Option.builder().longOpt("version-hint")
            .desc("The version hint to use when suggesting the next release version. "
                + "Required when using --suggest-releases")
            .hasArg()
            .build());

        options.addOption(Option.builder().longOpt("force-patch-prefixes")
            .desc("Comma separated list for branch prefixes which will force increase of the versions patch segment. "
                + "Leave empty or unused to disable. Only usable together with --suggest-releases")
            .hasArg()
            .build());

        options.addOption(Option.builder().longOpt("force-minor-prefixes")
            .desc("Comma separated list for branch prefixes which will force increase of the versions minor segment. "
                + "Leave empty or unused to disable. Only usable together with --suggest-releases")
            .hasArg()
            .build());

        options.addOption(Option.builder().longOpt("increment-for-existing-tag")
            .desc("Overrides the default behaviour of using the version number found in existing tag. Normally used "
                + "to allow re-build in CI/CD pipelines with automatic version increment. Only usable together with "
                + "--suggest-releases. List of supported arguments:"
                + "\n 'default' or no argument : increment using default strategy (as if there where no tag)"
                + "\n 'patch'                  : force increment of patch segment"
                + "\n 'minor'                  : force increment of minor segment"
                )
            .hasArg()
            .optionalArg(true)
            .build());

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

    private static Optional<String> getOptionalOptionValue(CommandLine cmd, String opt, String defaultValue) {
        if (!cmd.hasOption(opt)) {
            return Optional.empty();
        }
        return Optional.of(cmd.getOptionValue(opt, defaultValue));
    }

    private static <T extends Enum<T>> Optional<T> readEnumStringIgnoringCase(String value, Class<T> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
            .map(T::toString)
            .filter(item -> item.equalsIgnoreCase(value))
            .map(item -> Enum.valueOf(enumType, item))
            .findAny();
    }


}
