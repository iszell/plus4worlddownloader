package hu.siz.tools.plus4worlddownloader.file;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileNameToolsTest {

    @Test
    void getRawFileName() {
    }

    @Test
    void convertFileName() {
    }

    @Test
    void getDirectoryFor() {
    }

    @Test
    void isFileSupported() throws IOException {
        var fileNameTools = new FileNameTools(null, null);

        assertTrue(fileNameTools.isFileSupported("proba/proba"));
        assertTrue(fileNameTools.isFileSupported("proba/proba/akarmi.PrG"));
        assertTrue(fileNameTools.isFileSupported("proba/proba.Seq"));
        assertTrue(fileNameTools.isFileSupported("proba/proba.D64"));
        assertTrue(fileNameTools.isFileSupported("proba/proba.d71"));
        assertTrue(fileNameTools.isFileSupported("proba/proba.d81"));
        assertFalse(fileNameTools.isFileSupported("proba/proba.d80"));
        assertTrue(fileNameTools.isFileSupported("proba/proba.tap"));
    }

    @Test
    void getExtensionsFound() {
    }
}