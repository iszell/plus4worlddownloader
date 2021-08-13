package hu.siz.tools.plus4worlddownloader.web;

import hu.siz.tools.plus4worlddownloader.file.FileSaver;
import hu.siz.tools.plus4worlddownloader.utils.AbstractLoggingUtility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class WebCrawler extends AbstractLoggingUtility {

    private FileSaver fileSaver = new FileSaver();

    public void crawl(String url) {
        verbose("Reading from source URL " + url);

        try {
            Document doc = Jsoup.connect(url).get();
            doc.select("a").forEach(this::processPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processPage(Element e) {
        verbose("Processing element " + e.html());
        String href = e.attributes().get("href");
        if (href != null) {
            String absUrl = e.absUrl("href");
            if (href.length() == 1 || href.startsWith("/") || href.startsWith("?")) {
                verbose("Skipping " + href);
            } else if (href.endsWith("/")) {
                crawl(absUrl);
            } else {
                fileSaver.downloadFile(absUrl);
            }
        }
    }

    public FileSaver getFileSaver() {
        return this.fileSaver;
    }
}