package hu.siz.tools.plus4worlddownloader;

import hu.siz.tools.plus4worlddownloader.utils.CommandLineOption;
import hu.siz.tools.plus4worlddownloader.web.WebCrawler;

import java.nio.file.FileSystems;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;

import static hu.siz.tools.plus4worlddownloader.utils.CommandLineOption.*;

public class Plus4WorldDownloaderApplication {

    public static boolean commodoreNaming = true;
    public static boolean verbose = false;
    public static boolean quiet = false;
    public static boolean forceDownload = false;
    public static boolean saveZips = false;
    public static boolean createZipDirs = true;

    private static String sourceUrl = "http://plus4.othersi.de/plus4";
    private static String targetDir = null;

    private static void processCommandLineArgs(String[] args) {
        Arrays.stream(args)
                .forEach(arg -> {
                    if (FORCE_DOWNLOAD.getOption().equalsIgnoreCase(arg)) {
                        forceDownload = true;
                    }
                    if (QUIET.getOption().equalsIgnoreCase(arg)) {
                        verbose = false;
                        quiet = true;
                    }
                    if (VERBOSE.getOption().equalsIgnoreCase(arg)) {
                        verbose = true;
                        quiet = false;
                    }
                    if (NO_RENAME.getOption().equalsIgnoreCase(arg)) {
                        commodoreNaming = false;
                    }
                    if (SAVE_ZIPS.getOption().equalsIgnoreCase(arg)) {
                        saveZips = true;
                    }
                    if (ZIP_AS_DIRECTORY.getOption().equalsIgnoreCase(arg)) {
                        createZipDirs = false;
                    }
                    if (arg.toLowerCase(Locale.ROOT).startsWith(TARGET_DIR.getOption().toLowerCase(Locale.ROOT))) {
                        targetDir = arg.substring(TARGET_DIR.getOption().length());
                    }
                    if (arg.toLowerCase(Locale.ROOT).startsWith(URL.getOption())) {
                        sourceUrl = arg.substring(URL.getOption().length());
                    }
                    if (HELP.getOption().equalsIgnoreCase(arg)) {
                        System.out.print("Usage: java -jar <jarname> ");
                        Arrays.stream(values())
                                .forEach(opt -> {
                                    if (!opt.isMandatory()) {
                                        System.out.print('[');
                                    }
                                    System.out.print(opt.getOption());
                                    if (!opt.isMandatory()) {
                                        System.out.print(']');
                                    }
                                    System.out.print(' ');
                                });
                        System.out.println();
                    }
                });
    }

    public static void main(String[] args) {
        System.out.println("Plus/4 World mirror downloader");

        LocalDateTime start = LocalDateTime.now();

        processCommandLineArgs(args);

        if (targetDir == null) {
            System.err.println("-targetdir= option is mandatory");
            return;
        }

        if (!targetDir.endsWith(FileSystems.getDefault().getSeparator())) {
            targetDir = targetDir.concat(FileSystems.getDefault().getSeparator());
        }

        if (verbose) {
            System.out.println("Downloading from " + sourceUrl + " to " + targetDir);
            if (commodoreNaming) {
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
