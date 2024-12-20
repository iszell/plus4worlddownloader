package hu.siz.tools.plus4worlddownloader.file;

import hu.siz.tools.plus4worlddownloader.Plus4WorldDownloaderApplication;
import hu.siz.tools.plus4worlddownloader.utils.AbstractLoggingUtility;
import hu.siz.tools.plus4worlddownloader.utils.CommandLineOption;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.*;

public class FileNameTools extends AbstractLoggingUtility {

    private static final String DIR_SEPARATOR = FileSystems.getDefault().getSeparator();
    private final Set<String> SUPPORTED_EXTENSIONS;
    private final Set<String> IGNORED_EXTENSIONS;
    private final Set<String> SUPPORTED_PREFIXES;
    private final Set<String> IGNORED_PREFIXES;
    private final Set<String> extensionsFound = new TreeSet<>();

    private final String sourceUrl;
    private final String targetDir;

    public FileNameTools(String sourceUrl, String targetDir) throws IOException {
        this.sourceUrl = sourceUrl;
        this.targetDir = targetDir;

        Properties fileExtensions = new Properties();
        fileExtensions.load(Plus4WorldDownloaderApplication.class.getResourceAsStream("/fileextensions.properties"));
        SUPPORTED_EXTENSIONS = Set.of(fileExtensions.getProperty("supported").split(","));
        IGNORED_EXTENSIONS = Set.of(fileExtensions.getProperty("ignored").split(","));

        Properties filePrefixes = new Properties();
        filePrefixes.load(Plus4WorldDownloaderApplication.class.getResourceAsStream("/fileprefixes.properties"));
        SUPPORTED_PREFIXES = Set.of(filePrefixes.getProperty("supported").split(","));
        IGNORED_PREFIXES = Set.of(filePrefixes.getProperty("ignored").split(","));
    }

    public String getRawFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public String convertFileName(String name, boolean isDirectory) {
        if (Plus4WorldDownloaderApplication.getBooleanOption(CommandLineOption.NO_RENAME)) {
            return name;
        }

        String extension = isDirectory || name.startsWith(".") ? "" : name.substring(name.lastIndexOf('.') + 1);
        String fileName = isDirectory || name.length() <= extension.length() ?
                name : name.substring(0, name.length() - extension.length() - 1);
        String result = fileName.toLowerCase();
        if (extension.isEmpty() || extension.charAt(0) != 'd') {
            result = result.replace("_", " ");
        }
        if (result.length() > 16) {
            result = result.replace("_", "");
            verbose("Name too long, removing spaces: " + name + "->" + result);
        }
        if (result.length() > 16) {
            result = result.replace("(", "").replace(")", "");
            verbose("Name still too long, removing parentheses for URL " + name + "->" + result);
        }
        if (result.length() > 16) {
            result = result.substring(0, 16);
            verbose("Name still too long, truncating for URL " + name + "->" + result);
        }
        result = result.trim();

        return isDirectory || name.length() <= extension.length() ?
                result : result.concat(name.substring(name.length() - extension.length() - 1));
    }

    public String getDirectoryFor(String url) {
        String dirNames = url.substring(sourceUrl.length()).toLowerCase();
        if ((url.endsWith(".zip") || url.endsWith(".7z")) && !Plus4WorldDownloaderApplication.getBooleanOption(CommandLineOption.DONT_CREATE_ARCHIVE_DIRECTORY)) {
            dirNames = dirNames.substring(0, dirNames.lastIndexOf("."));
        } else {
            dirNames = dirNames.substring(0, dirNames.lastIndexOf("/"));
        }
        StringBuilder sb = new StringBuilder();
        for (String dirName : dirNames.split("/")) {
            sb.append(convertFileName(dirName, true));
            sb.append(DIR_SEPARATOR);
        }
        return targetDir.concat(sb.toString());
    }

    public boolean isFileSupported(String name) {
        String lowerCaseName = name.toLowerCase();
        int extensionDotLocation = lowerCaseName.lastIndexOf('.');
        if (extensionDotLocation < 0) {
            return true;
        }
        int prefixDotLocation = lowerCaseName.indexOf('.');
        if (prefixDotLocation < 0) {
            return true;
        }
        String extension = lowerCaseName.substring(extensionDotLocation + 1);
        String prefix = lowerCaseName.substring(0, prefixDotLocation);
        if (SUPPORTED_EXTENSIONS.contains(extension) || SUPPORTED_PREFIXES.contains(prefix)) {
            return true;
        } else if (!IGNORED_EXTENSIONS.contains(extension)) {
            log(String.format("Unknown extension %1s for name %2s", extension, name));
            extensionsFound.add(extension);
        }
        return false;
    }

    public Set<String> getExtensionsFound() {
        return extensionsFound;
    }
}
