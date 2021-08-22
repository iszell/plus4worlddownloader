package hu.siz.tools.plus4worlddownloader;

import hu.siz.tools.plus4worlddownloader.web.WebCrawler;

import java.nio.file.FileSystems;
import java.time.Duration;
import java.time.LocalDateTime;

public class Plus4WorldDownloaderApplication {

    public static final String FORCEDOWNLOAD = "-forcedownload";
    public static final String QUIET = "-quiet";
    public static final String VERBOSE = "-verbose";
    public static final String TARGETDIR = "-targetdir=";
    public static final String HELP = "-help";
    public static final String NORENAME = "-norename";
    public static final String URL = "-url=";
    public static final String SAVEZIPS = "-savezips";

    public static String sourceUrl = "http://plus4.othersi.de/plus4";
    public static String targetDir = null;
    public static boolean commodoreNaming = true;
    public static boolean verbose = false;
    public static boolean quiet = false;
    public static boolean forceDownload = false;
    public static boolean saveZips = false;

    private static WebCrawler webCrawler = new WebCrawler();

    public static void main(String[] args) {
        System.out.println("Plus/4 World mirror downloader");

        LocalDateTime start = LocalDateTime.now();
        for (String arg : args) {
            if (FORCEDOWNLOAD.equalsIgnoreCase(arg)) {
                forceDownload = true;
            }
            if (QUIET.equalsIgnoreCase(arg)) {
                verbose = false;
                quiet = true;
            }
            if (VERBOSE.equalsIgnoreCase(arg)) {
                verbose = true;
                quiet = false;
            }
            if (NORENAME.equalsIgnoreCase(arg)) {
                commodoreNaming = false;
            }
            if (SAVEZIPS.equalsIgnoreCase(arg)) {
                saveZips = true;
            }
            if (arg.startsWith(TARGETDIR)) {
                targetDir = arg.substring(TARGETDIR.length());
            }
            if (arg.startsWith(URL)) {
                sourceUrl = arg.substring(URL.length());
            }
            if (HELP.equalsIgnoreCase(arg)) {
                System.out.print("Usage: java -jar <jarname> ");
                System.out.print("[" + FORCEDOWNLOAD + "] ");
                System.out.print("[" + QUIET + "] ");
                System.out.print("[" + VERBOSE + "] ");
                System.out.print("[" + NORENAME + "] ");
                System.out.print("[" + SAVEZIPS + "] ");
                System.out.print("[" + URL + "<download source url>]");
                System.out.println(TARGETDIR + "<download target dir>");
            }
        }

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

        webCrawler.crawl(sourceUrl);

        LocalDateTime end = LocalDateTime.now();
        Duration d = Duration.between(start, end);

        System.out.printf("%1d zips extracted; %2d files downloaded in %3d seconds%n", webCrawler.getFileSaver().getZipsExtracted(), webCrawler.getFileSaver().getFilesSaved(), d.getSeconds());
    }
}
