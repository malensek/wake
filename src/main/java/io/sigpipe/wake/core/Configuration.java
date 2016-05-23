package wake.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

public class Configuration {

    private File sourceDir = new File("source");
    private File outputDir = new File("output");
    private File templateDir = new File("templates");

    private TitleMaker titleMaker;

    private Set<PosixFilePermission> filePerm = null;
    private Set<PosixFilePermission> dirPerm = null;

    private static Configuration instance;

    private Configuration() {
        String settingsData = "";
        File settingsFile = new File("Wakefile.yaml");
        if (settingsFile.exists() == false) {
            settingsFile = new File("Wakefile.yml");
            if (settingsFile.exists() == false) {
                return;
            }
        }

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

        String baseTitle = readSetting("basetitle", yamlData);
        if (baseTitle != null) {
            titleMaker = new DashedTitle(baseTitle);
        } else {
            titleMaker = new BasicTitle();
        }

        String filePermStr = readPermission("file", yamlData);
        if (filePermStr != null) {
            filePerm = PosixFilePermissions.fromString(filePermStr);
        }
        String dirPermStr = readPermission("dir", yamlData);
        if (dirPermStr != null) {
            dirPerm = PosixFilePermissions.fromString(dirPermStr);
        }
    }

    public static Configuration instance() {
        if (Configuration.instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public File getSourceDir() {
        return sourceDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public File getTemplateDir() {
        return templateDir;
    }

    public TitleMaker getTitleMaker() {
        return titleMaker;
    }

    public Set<PosixFilePermission> getFilePermissions() {
        return filePerm;
    }

    public Set<PosixFilePermission> getDirPermissions() {
        return dirPerm;
    }

    private File readDirConfig(String defaultName, Map<?, ?> config) {
        if (config == null) {
            return new File(defaultName);
        }

        Map<?, ?> dirs = (Map<?, ?>) config.get("directories");
        if (dirs != null) {
            String customDir = (String) dirs.get(defaultName);
            if (customDir != null) {
                return new File(customDir);
            }
        }

        return new File(defaultName);
    }

    private String readSetting(String settingName, Map<?, ?> config) {
        if (config == null) {
            return null;
        }
        Map<?, ?> settings = (Map<?, ?>) config.get("settings");
        if (settings != null) {
            return (String) settings.get(settingName);
        } else {
            return null;
        }
    }

    private String readPermission(String permType, Map<?, ?> config) {
        if (config == null) {
            return null;
        }

        Map<?, ?> permissions = (Map<?, ?>) config.get("permissions");
        if (permissions != null) {
            return (String) permissions.get(permType);
        } else {
            return null;
        }
    }
}
