/*
Copyright (c) 2018, Matthew Malensek
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

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
import com.vladsch.flexmark.ext.xwiki.macros.MacroExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.KeepType;
import com.vladsch.flexmark.util.options.MutableDataSet;

public class Configuration {

    private File sourceDir = new File("source");
    private File outputDir = new File("output");
    private File templateDir = new File("templates");

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

    public Set<PosixFilePermission> getFilePermissions() {
        return filePerm;
    }

    public Set<PosixFilePermission> getDirPermissions() {
        return dirPerm;
    }

    public MutableDataSet getMarkdownOptions() {
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.COMMONMARK);
        options.set(Parser.EXTENSIONS, Arrays.asList(
                AnchorLinkExtension.create(),
                AutolinkExtension.create(),
                StrikethroughSubscriptExtension.create(),
                TablesExtension.create(),
                TaskListExtension.create(),
                TocExtension.create(),
                MacroExtension.create(),
                TypographicExtension.create()
        ));

        /* Disable id creation by the Anchorlinks plugin. This is already
         * accomplished by turning on the Toc plugin (which sets
         * GENERATE_HEADER_ID to true) */
        options.set(AnchorLinkExtension.ANCHORLINKS_SET_ID, false);

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
