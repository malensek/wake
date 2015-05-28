package jake.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

public class Configuration {

    private static File sourceDir;
    private static File outputDir;
    private static File templateDir;

    private static TitleMaker titleMaker = new DashedTitle("Matthew Malensek");

    static {
        String settingsData = "";
        File settingsFile = new File("Wakefile.yml");
        try {
            settingsData = new String(
                    Files.readAllBytes(settingsFile.toPath()));
        } catch (IOException e) {
            System.out.println("Error reading configuration");
            e.printStackTrace();
        }

        Map<?, ?> yamlData = new HashMap<>();
        try {
            YamlReader yaml = new YamlReader(settingsData);
            yamlData = (Map<?, ?>) yaml.read();
        } catch (YamlException e) {
            System.out.println("Error parsing YAML");
            e.printStackTrace();
        }

        sourceDir = readDirConfig("source", yamlData);
        outputDir = readDirConfig("output", yamlData);
        templateDir = readDirConfig("templates", yamlData);

    }

    public static File getSourceDir() {
        return sourceDir;
    }

    public static File getOutputDir() {
        return outputDir;
    }

    public static File getTemplateDir() {
        return templateDir;
    }

    public static TitleMaker getTitleMaker() {
        return titleMaker;
    }

    private static File readDirConfig(String defaultName, Map<?, ?> dirData) {
        Map<?, ?> dirs = (Map<?, ?>) dirData.get("directories");
        if (dirs != null) {
            String customDir = (String) dirs.get(defaultName);
            if (customDir != null) {
                return new File(customDir);
            }
        }

        return new File(defaultName);
    }
}
