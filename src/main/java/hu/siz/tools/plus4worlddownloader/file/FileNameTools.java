package hu.siz.tools.plus4worlddownloader.file;

import hu.siz.tools.plus4worlddownloader.Plus4WorldDownloaderApplication;
import hu.siz.tools.plus4worlddownloader.utils.AbstractLoggingUtility;

import java.nio.file.FileSystems;

public class FileNameTools extends AbstractLoggingUtility {

    private static final String DIRSEPARATOR = FileSystems.getDefault().getSeparator();
    private static final String[] SUPPORTED_EXTENSIONS = {".prg", ".tap", ".seq", ".d64"};

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
        String dirNames = url.substring(Plus4WorldDownloaderApplication.sourceUrl.length()).toLowerCase();
        dirNames = dirNames.substring(0, dirNames.lastIndexOf("/"));
        StringBuilder sb = new StringBuilder();
        for (String dirName : dirNames.split("\\/")) {
            sb.append(convertFileName(dirName, true));
            sb.append(DIRSEPARATOR);
        }
        return Plus4WorldDownloaderApplication.targetDir.concat(sb.toString());
    }

    public boolean isFileSupported(String name) {
        String lowerCaseName = name.toLowerCase();
        for (String extension : SUPPORTED_EXTENSIONS) {
            if (lowerCaseName.endsWith(extension)) {
                return true;
            }
        }
        // handle file names without extension
        return !getRawFileName(name).contains(".");
    }
}
