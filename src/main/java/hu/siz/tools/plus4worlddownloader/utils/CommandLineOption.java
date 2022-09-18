package hu.siz.tools.plus4worlddownloader.utils;

/**
 * Enum containing the available command line options
 */
public enum CommandLineOption {

    HELP("-help", false, "Print this help message", null),
    QUIET("-quiet", false, "Only error messages are printed", false),
    VERBOSE("-verbose", false, "Detailed informational messages are printed", false),
    FORCE_DOWNLOAD("-forceDownload", false, "Force re-download of existing files", true),
    NO_RENAME("-noRename", false, "Do not rename downloaded files and directories to fit 16 characters (for emulator fighters)", false),
    SAVE_ZIPS("-saveZips", false, "Save zips too (they are always extracted as well)", false),
    ZIP_AS_DIRECTORY("-noZipDir", false, "Do not create directories for zip contents", false),
    URL("-url=", false, "Source URL to start crawling", "http://plus4.othersi.de/plus4"),
    TARGET_DIR("-targetDir=", true, "Root directory for downloads", null);

    private String option;
    private boolean mandatory;
    private String description;
    private Object defaultValue;

    CommandLineOption(String option, boolean mandatory, String description, Object defaultValue) {
        this.option = option;
        this.mandatory = mandatory;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public String getOption() {
        return this.option;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public String getDescription() {
        return this.description;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }
}
