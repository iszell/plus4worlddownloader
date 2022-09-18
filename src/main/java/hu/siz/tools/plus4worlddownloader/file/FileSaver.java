package hu.siz.tools.plus4worlddownloader.file;

import hu.siz.tools.plus4worlddownloader.Plus4WorldDownloaderApplication;
import hu.siz.tools.plus4worlddownloader.utils.AbstractLoggingUtility;
import hu.siz.tools.plus4worlddownloader.utils.CommandLineOption;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.http.client.fluent.Request;

import java.io.*;

public class FileSaver extends AbstractLoggingUtility {

    private final FileNameTools fileNameTools;

    private long filesSaved = 0;
    private long zipsExtracted = 0;
    private long zipsChecked = 0;

    public FileSaver(String sourceUrl, String targetDir) {
        this.fileNameTools = new FileNameTools(sourceUrl, targetDir);
    }

    public void downloadFile(String url) {
        if (url.endsWith(".zip")) {
            saveZip(url);
        } else if (fileNameTools.isFileSupported(url)) {
            saveFile(url);
        } else {
            verbose("Skipping " + url);
        }
    }

    private void saveFile(String url) {
        String directory = fileNameTools.getDirectoryFor(url);
        File d = new File(directory);
        if (!d.exists() && !d.mkdirs()) {
            System.err.println("Error creating directory " + d);
            return;
        }
        String fileName = fileNameTools.convertFileName(fileNameTools.getRawFileName(url), false);
        File f = new File(directory, fileName);
        if (f.exists() && !Plus4WorldDownloaderApplication.getBooleanOption(CommandLineOption.FORCE_DOWNLOAD)) {
            verbose("Skipping existing file " + f);
            return;
        }
        log("Downloading file " + f);

        try {
            Request.Get(url).execute().saveContent(f);
            filesSaved++;
        } catch (IOException e) {
            System.err.println("Failed to download from url " + url + ": " + e.getMessage());
        }
    }

    private void saveZip(String url) {
        verbose("Downloading zip " + url);
        File z = null;
        ArchiveInputStream zip = null;
        try {
            if (Plus4WorldDownloaderApplication.getBooleanOption(CommandLineOption.SAVE_ZIPS)) {
                String directory = fileNameTools.getDirectoryFor(url);
                String fileName = fileNameTools.getRawFileName(url);
                z = new File(directory, fileName);
                if (z.exists()) {
                    verbose("Skipping existing zip " + url);
                    return;
                }
            } else {
                z = File.createTempFile("p4wd_", null);
            }
            Request.Get(url).execute().saveContent(z);

            zip = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(new FileInputStream(z)));
            ArchiveEntry entry;
            boolean wasAnyExtracted = false;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    verbose("Ignoring zip directory entry in " + url);
                } else if (fileNameTools.isFileSupported(entry.getName())) {
                    wasAnyExtracted |= extractZipEntry(url, zip, entry);
                }
            }
            zip.close();
            zipsChecked++;
            if (wasAnyExtracted) {
                zipsExtracted++;
            }
        } catch (
                Exception e) {
            System.err.println("Error processing zip " + url + ": " + e.getMessage());
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    System.err.println("Error closing zip " + e.getMessage());
                }
            }
            if (z != null && !Plus4WorldDownloaderApplication.getBooleanOption(CommandLineOption.SAVE_ZIPS)) {
                z.delete();
            }
        }

    }

    private boolean extractZipEntry(String url, ArchiveInputStream zip, ArchiveEntry entry) throws IOException {
        String directory = fileNameTools.getDirectoryFor(url);
        File d = new File(directory);
        if (!d.exists()) {
            d.mkdirs();
        }
        String fileName = fileNameTools.convertFileName(fileNameTools.getRawFileName(entry.getName()), false);
        File extracted = new File(directory, fileName);
        if (extracted.exists() && !Plus4WorldDownloaderApplication.getBooleanOption(CommandLineOption.FORCE_DOWNLOAD)) {
            verbose("Skipping existing file " + extracted);
        } else {
            log("Extracting file " + extracted);
            try (FileOutputStream fos = new FileOutputStream(extracted)) {
                int len;
                byte[] buffer = new byte[16384];
                while ((len = zip.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                return true;
            } catch (IOException e) {
                extracted.delete();
                throw new IOException(e);
            }
        }
        return false;
    }

    public long getFilesSaved() {
        return filesSaved;
    }

    public long getZipsExtracted() {
        return zipsExtracted;
    }

    public long getZipsChecked() {
        return zipsChecked;
    }

    public FileNameTools getFileNameTools() {
        return fileNameTools;
    }
}
