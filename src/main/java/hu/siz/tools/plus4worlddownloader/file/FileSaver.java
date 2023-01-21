package hu.siz.tools.plus4worlddownloader.file;

import hu.siz.tools.plus4worlddownloader.Plus4WorldDownloaderApplication;
import hu.siz.tools.plus4worlddownloader.utils.AbstractLoggingUtility;
import hu.siz.tools.plus4worlddownloader.utils.CommandLineOption;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.http.client.fluent.Request;

import java.io.*;

public class FileSaver extends AbstractLoggingUtility {

    private final FileNameTools fileNameTools;

    private long filesSaved = 0;
    private long archivesExtracted = 0;
    private long archivesChecked = 0;

    public FileSaver(String sourceUrl, String targetDir) throws IOException {
        this.fileNameTools = new FileNameTools(sourceUrl, targetDir);
    }

    public void downloadFile(String url) {
        if (url.endsWith(".zip") || url.endsWith(".7z")) {
            saveArchive(url);
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

    private void saveArchive(String url) {
        verbose("Downloading archive " + url);
        File a = null;
        ArchiveInputStream archive = null;
        try {
            if (Plus4WorldDownloaderApplication.getBooleanOption(CommandLineOption.SAVE_ARCHIVES)) {
                String directory = fileNameTools.getDirectoryFor(url);
                String fileName = fileNameTools.getRawFileName(url);
                a = new File(directory, fileName);
                if (a.exists()) {
                    verbose("Skipping existing archive " + url);
                    return;
                }
            } else {
                a = File.createTempFile("p4wd_", null);
            }
            Request.Get(url).execute().saveContent(a);

            archive = new ArchiveStreamFactory(CharEncoding.ISO_8859_1).createArchiveInputStream(new BufferedInputStream(new FileInputStream(a)));
            ArchiveEntry entry;
            boolean wasAnyExtracted = false;
            while ((entry = archive.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    verbose("Ignoring archive directory entry in " + url);
                } else if (fileNameTools.isFileSupported(entry.getName())) {
                    wasAnyExtracted |= extractArchiveEntry(url, archive, entry);
                }
            }
            archive.close();
            archivesChecked++;
            if (wasAnyExtracted) {
                archivesExtracted++;
            }
        } catch (
                Exception e) {
            System.err.println("Error processing archive " + url + ": " + e.getMessage());
        } finally {
            if (archive != null) {
                try {
                    archive.close();
                } catch (IOException e) {
                    System.err.println("Error closing archive " + e.getMessage());
                }
            }
            if (a != null && !Plus4WorldDownloaderApplication.getBooleanOption(CommandLineOption.SAVE_ARCHIVES)) {
                a.delete();
            }
        }

    }

    private boolean extractArchiveEntry(String url, ArchiveInputStream archive, ArchiveEntry entry) throws IOException {
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
                while ((len = archive.read(buffer)) > 0) {
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

    public long getArchivesExtracted() {
        return archivesExtracted;
    }

    public long getArchivesChecked() {
        return archivesChecked;
    }

    public FileNameTools getFileNameTools() {
        return fileNameTools;
    }
}
