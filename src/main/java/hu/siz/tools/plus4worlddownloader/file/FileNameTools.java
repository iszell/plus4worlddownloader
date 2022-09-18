package hu.siz.tools.plus4worlddownloader.file;

import hu.siz.tools.plus4worlddownloader.Plus4WorldDownloaderApplication;
import hu.siz.tools.plus4worlddownloader.utils.AbstractLoggingUtility;

import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileNameTools extends AbstractLoggingUtility {

    private static final String DIR_SEPARATOR = FileSystems.getDefault().getSeparator();
    private static final List<String> SUPPORTED_EXTENSIONS = List.of("bin", "crt", "d64", "d71", "d81", "g64", "mid", "prg", "rom", "seq", "sid", "tap");
    private static final List<String> IGNORED_EXTENSIONS = List.of("bat", "bmp", "exe", "gif", "java", "jpg", "mp3", "mp4", "mpeg", "pdf", "txt", "wav", "xls");
    private final Set<String> extensionsFound = new HashSet<>();

    private final String sourceUrl;
    private final String targetDir;

    public FileNameTools(String sourceUrl, String targetDir) {
        this.sourceUrl = sourceUrl;
        this.targetDir = targetDir;
    }

    public String getRawFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public String convertFileName(String name, boolean isDirectory) {
        if (!Plus4WorldDownloaderApplication.commodoreNaming) {
            return name;
        }

        String fileName = isDirectory ? name : name.substring(0, name.length() - 4);
        String result = fileName.toLowerCase().replace("_", "");
        if (result.length() > 16) {
            result = result.replace("(", "").replace(")", "");
            verbose("Name still too long, replacing parentheses for URL " + name + "->" + result);
        }
        if (result.length() > 16) {
            result = result.substring(0, 16);
            verbose("Name still too long, truncating for URL " + name + "->" + result);
        }
        return isDirectory ? result : result.concat(name.substring(name.length() - 4));
    }

    public String getDirectoryFor(String url) {
        String dirNames = url.substring(sourceUrl.length()).toLowerCase();
        if (url.endsWith(".zip") && Plus4WorldDownloaderApplication.createZipDirs) {
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
        int dotLocation = lowerCaseName.lastIndexOf('.');
        if (dotLocation < 0) {
            return true;
        }
        String extension = lowerCaseName.substring(dotLocation + 1);
        if (SUPPORTED_EXTENSIONS.contains(extension)) {
            return true;
        } else if (!IGNORED_EXTENSIONS.contains(extension)) {
            extensionsFound.add(extension);
        }
        return false;
    }

    public Set<String> getExtensionsFound() {
        return extensionsFound;
    }
}
