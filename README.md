# plus4worlddownloader

## What's this
A tool to download the entire plus/4 world archive and rename directories and files to be maximum 16 characters in length so they can be used directly on an SD2IEC drive.

## What does it do?
It will parse through the HTML listing of an FTP site and downloads everything in the same directory structure to the specified target directory. By default it will rename everything to fit into the 16 character file name limit of Commodore computers and it will skip files that already present. Only files with extensions .prg, .tap and .d64 will be downloaded (and archives but they are extracted - with the same rules of renaming and filtering by extension).

## How to use
You need to have a Java runtime installed with minimum version 11.

Starting the program:
java -jar < jarname > [-forcedownload] [-quiet] [-verbose] [-norename] [-savearchives] [-url=< download source url >]-targetdir=< download target dir >

## Options
### -forcedownload
This will instruct the program to download all programs whether they exist or not.

### -quiet
Reduces screen output by printing downloaded program names and error messages only.

### -verbose
Increases screen output by printing names of parsed URLs and skipped programs as well.

### -norename
Don't rename anything (keep the original directory and file names): useful for the majority of the users who use silicon dolls (=emulators) instead of the real thing.

### -savearchives
Save archives too (normally archives are downloaded to the temp directory and deleted after extracting and only the contents are checked for existence) resulting in less download because existing archives won't be downloaded and extracted again.

### -url
Specify the source URL of the mirror. Default is `http://plus4.othersi.de/plus4` . Currently I'm not aware of any other mirrors.

### -targetdir
Specify the base directory for download. This is the only mandatory parameter.

## Planned features
* A pre-defined rename mapping instead of the algorithm so different programs resulting in the same name can be differentiated.

## Known issues
* Currently programs and directories use the same algorithm to get the Commodore file name. In the end it's a truncate so often programs with long names will result in the same truncated name and only one of them will be downloaded
