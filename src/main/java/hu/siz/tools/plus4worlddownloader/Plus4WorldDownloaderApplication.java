package hu.siz.tools.plus4worlddownloader;

import hu.siz.tools.plus4worlddownloader.utils.CommandLineOption;
import hu.siz.tools.plus4worlddownloader.web.WebCrawler;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static hu.siz.tools.plus4worlddownloader.utils.CommandLineOption.*;

public class Plus4WorldDownloaderApplication {

    private static final Map<CommandLineOption, Object> options = new HashMap<>();

    private static boolean processCommandLineArgs(String[] args) {
        Arrays.stream(args)
                .forEach(arg -> {
                    if ('-' != arg.charAt(0) || !processOption(arg)) {
                        System.out.println("Unknown option " + arg);
                    }
                });

        if (checkForMissingMandatoryOptions() || options.containsKey(HELP)) {
            usage();
            return false;
        }
        addDefaultOptionValues();
        return true;
    }

    private static void usage() {
        System.out.println("Valid options:");
        Arrays.stream(CommandLineOption.values())
                .forEach(o -> {
                    System.out.printf(" -%1s%2s%3s"
                            , o.getOption()
                            , " ".repeat(15 - o.getOption().length())
                            , o.getDescription());
                    if (o.getDefaultValue() != null) {
                        System.out.printf(" (default: %1s)%n", o.getDefaultValue());
                    } else {
                        System.out.println();
                    }
                });
    }

    private static void addDefaultOptionValues() {
        Arrays.stream(CommandLineOption.values())
                .filter(f -> !options.containsKey(f))
                .forEach(f -> options.put(f, f.getDefaultValue()));
    }

    private static boolean checkForMissingMandatoryOptions() {
        var missingOption = Arrays.stream(values())
                .filter(CommandLineOption::isMandatory)
                .filter(f -> !options.containsKey(f))
                .findAny();
        if (missingOption.isPresent()) {
            System.err.printf("Missing mandatory option -%1s%n%n", missingOption.get().getOption());
            return true;
        }
        return false;
    }

    private static boolean processOption(String arg) {
        boolean isString = arg.contains("=");
        var optString = isString ? arg.substring(1, arg.indexOf('=') + 1) : arg.substring(1);
        var opt = Arrays.stream(CommandLineOption.values())
                .filter(option -> option.getOption().equalsIgnoreCase(optString))
                .findAny()
                .orElse(null);
        if (opt == null) {
            return false;
        }
        if (isString) {
            options.put(opt, arg.substring(arg.indexOf("=") + 1));
        } else {
            options.put(opt, !(Boolean) opt.getDefaultValue());
        }

        return true;
    }

    public static boolean getBooleanOption(CommandLineOption option) {
        return (boolean) options.get(option);
    }

    public static String getStringOption(CommandLineOption option) {
        return (String) options.get(option);
    }

    public static void main(String[] args) {
        System.out.println("Plus/4 World mirror downloader");

        LocalDateTime start = LocalDateTime.now();

        if (processCommandLineArgs(args)) {

            String targetDir = getStringOption(TARGET_DIR);
            if (targetDir == null || targetDir.length() == 0) {
                System.err.println("-targetdir= option is mandatory");
                return;
            }

            if (!targetDir.endsWith(FileSystems.getDefault().getSeparator())) {
                targetDir = targetDir.concat(FileSystems.getDefault().getSeparator());
            }

            var sourceUrl = getStringOption(URL);

            if (getBooleanOption(VERBOSE)) {
                System.out.println("Downloading from " + sourceUrl + " to " + targetDir);
                if (!getBooleanOption(NO_RENAME)) {
                    System.out.println("Renaming downloaded files to 16 characters");
                }
            }

            WebCrawler webCrawler = null;
            try {
                webCrawler = new WebCrawler(sourceUrl, targetDir);

                webCrawler.crawl();

                LocalDateTime end = LocalDateTime.now();
                Duration d = Duration.between(start, end);

                System.out.printf("%1d zips checked, %2d zips extracted; %3d files downloaded in %4d seconds%n", webCrawler.getFileSaver().getZipsChecked(), webCrawler.getFileSaver().getZipsExtracted(), webCrawler.getFileSaver().getFilesSaved(), d.getSeconds());
                System.out.printf("Unhandled extensions found: %1s%n", String.join(", ", webCrawler.getFileSaver().getFileNameTools().getExtensionsFound()));

            } catch (IOException e) {
                System.err.print("Can't initialize application");
            }
        }
    }
}
