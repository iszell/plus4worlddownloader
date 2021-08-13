package hu.siz.tools.plus4worlddownloader.utils;

import hu.siz.tools.plus4worlddownloader.Plus4WorldDownloaderApplication;

public abstract class AbstractLoggingUtility {
    protected void quiet(String msg) {
        if (!Plus4WorldDownloaderApplication.quiet) {
            System.out.println(msg);
        }
    }

    protected void log(String msg) {
        System.out.println(msg);
    }

    protected void verbose(String msg) {
        if (Plus4WorldDownloaderApplication.verbose) {
            System.out.println(msg);
        }
    }
}
