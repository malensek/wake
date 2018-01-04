/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.KeepType;
import com.vladsch.flexmark.util.options.MutableDataSet;

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

    public MutableDataSet getMarkdownOptions() {
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.GITHUB_DOC);
        options.set(Parser.EXTENSIONS, Arrays.asList(
                AutolinkExtension.create(),
                AnchorLinkExtension.create(),
                StrikethroughSubscriptExtension.create(),
                TablesExtension.create(),
                TaskListExtension.create(),
                TocExtension.create(),
                TypographicExtension.create()
        ));

        /* GFM table parsing options */
        options.set(TablesExtension.COLUMN_SPANS, false)
            .set(TablesExtension.MIN_HEADER_ROWS, 1)
            .set(TablesExtension.MAX_HEADER_ROWS, 1)
            .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
            .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
            .set(TablesExtension.WITH_CAPTION, false)
            .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true);

        options
            .set(HtmlRenderer.INDENT_SIZE, 4)
            .set(HtmlRenderer.OBFUSCATE_EMAIL, true)
            .set(Parser.REFERENCES_KEEP, KeepType.LAST);

        return options;
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
