package io.reticent.eevee.rss;

import io.reticent.eevee.configuration.GlobalConfiguration;
import io.reticent.eevee.util.NetUtil;
import io.reticent.eevee.rss.model.HorribleSubsReleaseItem;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.text.html.Option;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class HorribleSubsReleaseReader {
    public Optional<List<HorribleSubsReleaseItem>> readFeed() {
        List<HorribleSubsReleaseItem> releases = new LinkedList<>();

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(NetUtil.streamPage(GlobalConfiguration.HORRIBLE_SUBS_RELEASE_FEED_RUL));
            NodeList items = document.getElementsByTagName("item");

            for (int n = 0; n < items.getLength(); n++) {
                Element item = (Element) items.item(n);

                releases.add(
                    HorribleSubsReleaseItem.builder()
                                           .title(getValue(item, "title"))
                                           .guid(getValue(item, "guid"))
                                           .link(getValue(item, "link"))
                                           .pubDate(getValue(item, "pubDate"))
                                           .build()
                );
            }

            return Optional.of(releases);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            log.error("Failed to create XML parser.", e);
        } catch (SAXException e) {
            e.printStackTrace();
            log.error("Failed to parse release XML.", e);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to get release XML", e);
        }

        return Optional.empty();
    }

    private String getValue(Element parent, String nodeName) {
        return parent.getElementsByTagName(nodeName).item(0).getFirstChild().getTextContent();
    }
}