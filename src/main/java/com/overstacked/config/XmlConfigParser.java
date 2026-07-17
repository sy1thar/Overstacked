package com.overstacked.config;

import com.overstacked.Overstacked;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;

public final class XmlConfigParser {

    private XmlConfigParser() {}

    public static boolean isOldFormat(Path path) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(path.toFile());
            doc.getDocumentElement().normalize();

            NodeList globalNodes = doc.getElementsByTagName("global");
            if (globalNodes.getLength() == 0) return false;

            Element global = (Element) globalNodes.item(0);
            return global.getElementsByTagName("normalMaxStack").getLength() > 0
                    || global.getElementsByTagName("specialMaxStack").getLength() > 0
                    || global.getElementsByTagName("uniqueMaxStack").getLength() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static void parse(Path path, OverstackedConfig config) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(path.toFile());
            doc.getDocumentElement().normalize();

            parseRules(doc, config);

            Overstacked.LOGGER.info("[Overstacked] Parsed XML rules from {}", path.getFileName());
        } catch (Exception e) {
            Overstacked.LOGGER.error("[Overstacked] Failed to parse XML config: {}", path, e);
        }
    }

    private static void parseRules(Document doc, OverstackedConfig config) {
        NodeList ruleNodes = doc.getElementsByTagName("rule");
        for (int i = 0; i < ruleNodes.getLength(); i++) {
            Element rule = (Element) ruleNodes.item(i);

            String target = childText(rule, "target");
            String maxStr = childText(rule, "maxStack");

            if (target == null || maxStr == null) {
                Overstacked.LOGGER.warn("[Overstacked] Skipping malformed <rule> at index {}", i);
                continue;
            }

            int maxStack = safeInt(maxStr, -1);
            if (maxStack <= 0) {
                Overstacked.LOGGER.warn("[Overstacked] Invalid maxStack '{}' in rule at index {}", maxStr, i);
                continue;
            }

            if (target.startsWith("#")) {
                config.addTagOverride(target, maxStack);
            } else {
                config.addItemOverride(target, maxStack);
            }
        }
    }

    private static String childText(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        return list.getLength() > 0 ? list.item(0).getTextContent().trim() : null;
    }

    private static int safeInt(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
