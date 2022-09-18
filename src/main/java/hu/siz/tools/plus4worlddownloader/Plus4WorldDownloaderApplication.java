package hu.siz.tools.plus4worlddownloader;

import hu.siz.tools.plus4worlddownloader.utils.CommandLineOption;
import hu.siz.tools.plus4worlddownloader.web.WebCrawler;

import javax.swing.plaf.basic.BasicArrowButton;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static hu.siz.tools.plus4worlddownloader.utils.CommandLineOption.*;

public class Plus4WorldDownloaderApplication {

    private static Map<CommandLineOption, Object> options = new HashMap<>();

    static {
        Arrays.stream(CommandLineOption.values())
                .forEach(opt -> {
                    options.put(opt, opt.getDefaultValue());
                });
    }

    private static void processCommandLineArgs(String[] args) {
        Arrays.stream(args)
                .forEach(arg -> {
                    var successfullyProcessed = false;
                    if ('-' == arg.charAt(0)) {
                        successfullyProcessed = processOption(arg);
                    }
                    if (!successfullyProcessed) {
                        System.out.println("Unknown option " + arg);
                    }
                });
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

        processCommandLineArgs(args);

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

        var webCrawler = new WebCrawler(sourceUrl, targetDir);

        webCrawler.crawl();

        LocalDateTime end = LocalDateTime.now();
        Duration d = Duration.between(start, end);

        System.out.printf("%1d zips checked, %2d zips extracted; %3d files downloaded in %4d seconds%n", webCrawler.getFileSaver().getZipsChecked(), webCrawler.getFileSaver().getZipsExtracted(), webCrawler.getFileSaver().getFilesSaved(), d.getSeconds());
        System.out.printf("Unhandled extensions found: %1s%n", String.join(", ", webCrawler.getFileSaver().getFileNameTools().getExtensionsFound()));
    }
}
