package hu.siz.tools.plus4worlddownloader.utils;

/**
 * Enum containing the available command line options
 */
public enum CommandLineOption {

    HELP("help", false, "Print this help message", null),
    QUIET("quiet", false, "Only error messages are printed", false),
    VERBOSE("verbose", false, "Detailed informational messages are printed", false),
    FORCE_DOWNLOAD("forceDownload", false, "Force re-download of existing files", false),
    MAX_TIMEOUT("maxTimeout=", false, "Maximum time to wait for files to be downloaded", "30"),
    NO_RENAME("noRename", false, "Do not rename downloaded files and directories to fit 16 characters (for emulator warriors)", false),
    SAVE_ARCHIVES("saveArchives", false, "Save archives too (they are always extracted as well)", false),
    DONT_CREATE_ARCHIVE_DIRECTORY("noArchiveDir", false, "Do not create directories for archive contents", false),
    URL("url=", false, "Source URL to start crawling", "http://plus4.othersi.de/plus4"),
    TARGET_DIR("targetDir=", true, "Root directory for downloads", null);

    private final String option;
    private final boolean mandatory;
    private final String description;
    private final Object defaultValue;

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
