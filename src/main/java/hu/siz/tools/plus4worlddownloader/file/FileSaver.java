package hu.siz.tools.plus4worlddownloader.file;

import hu.siz.tools.plus4worlddownloader.Plus4WorldDownloaderApplication;
import hu.siz.tools.plus4worlddownloader.utils.AbstractLoggingUtility;
import org.apache.http.client.fluent.Request;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileSaver extends AbstractLoggingUtility {

    private final FileNameTools fileNameTools = new FileNameTools();

    private long filesSaved = 0;
    private long zipsExtracted = 0;

    public void downloadFile(String url) {
        if (fileNameTools.isFileSupported(url)) {
            saveFile(url);
        } else if (url.endsWith(".zip")) {
            saveZip(url);
        } else {
            verbose("Skipping " + url);
        }
    }

    private void saveFile(String url) {
        String directory = fileNameTools.getDirectoryFor(url);
        File d = new File(directory);
        if (!d.exists()) {
            if (!d.mkdirs()) {
                System.err.println("Error creating directory " + d);
                return;
            }
        }
        String fileName = fileNameTools.convertFileName(fileNameTools.getRawFileName(url), false);
        File f = new File(directory, fileName);
        if (f.exists() && !Plus4WorldDownloaderApplication.forceDownload) {
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
        ZipInputStream zip = null;
        try {
            if (Plus4WorldDownloaderApplication.saveZips) {
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
            zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(z)));
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                if (fileNameTools.isFileSupported(entry.getName())) {
                    if (entry.isDirectory()) {
                        System.err.println("Zip directory in " + url);
                    } else {
                        extractZipEntry(url, zip, entry);
                    }
                }

                zip.closeEntry();
                entry = zip.getNextEntry();
            }
            zip.close();
            zipsExtracted++;
        } catch (Exception e) {
            System.err.println("Error processing zip " + url + ": " + e.getMessage());
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    System.err.println("Error closing zip " + e.getMessage());
                }
            }
            if (z != null && !Plus4WorldDownloaderApplication.saveZips) {
                z.delete();
            }
        }
    }

    private void extractZipEntry(String url, ZipInputStream zip, ZipEntry entry) throws IOException {
        String directory = fileNameTools.getDirectoryFor(url);
        File d = new File(directory);
        if (!d.exists()) {
            d.mkdirs();
        }
        String fileName = fileNameTools.convertFileName(fileNameTools.getRawFileName(entry.getName()), false);
        File extracted = new File(directory, fileName);
        if (extracted.exists() && !Plus4WorldDownloaderApplication.forceDownload) {
            verbose("Skipping existing file " + extracted);
        } else {
            log("Extracting file " + extracted);
            try (FileOutputStream fos = new FileOutputStream(extracted)) {
                int len;
                byte[] buffer = new byte[16384];
                while ((len = zip.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            } catch (IOException e) {
                extracted.delete();
                throw new IOException(e);
            }
        }
    }

    public long getFilesSaved() {
        return filesSaved;
    }

    public long getZipsExtracted() {
        return zipsExtracted;
    }

}
