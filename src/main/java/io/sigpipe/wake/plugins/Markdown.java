/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.plugins;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.vladsch.flexmark.ast.Node;
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

import io.sigpipe.wake.core.Configuration;
import io.sigpipe.wake.core.Plugin;
import io.sigpipe.wake.core.TemplateUtils;
import io.sigpipe.wake.core.WakeFile;
import io.sigpipe.wake.util.YAMLFrontMatter;

/**
 * Plugin to process Markdown (.md) files. This plugin takes a markdown file as
 * its input, reads its YAML front matter, converts the Markdown to HTML, and
 * then inserts the final markup into a Velocity template.
 */
public class Markdown implements Plugin {

    private Parser markdownParser;
    private HtmlRenderer htmlRenderer;

    public Markdown() {
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

        markdownParser = Parser.builder(options).build();
        htmlRenderer = HtmlRenderer.builder(options).build();
    }

    @Override
    public String name() {
        return "Markdown";
    }

    @Override
    public boolean wants(WakeFile file) {
        return file.getExtension().equals("md");
    }

    @Override
    public List<WakeFile> requires(WakeFile file) {
        Configuration config = Configuration.instance();
        List<WakeFile> dependencies = new ArrayList<>();
        WakeFile template = determineTemplate(file);
        dependencies.add(template);
        dependencies.addAll(
                TemplateUtils.getTemplateDependencies(config, template));
        return dependencies;
    }

    private WakeFile determineTemplate(WakeFile file) {
        Configuration config = Configuration.instance();
        WakeFile defaultTemplate = new WakeFile(
                config.getTemplateDir(), "markdown.vm");
        try {
            FileReader fr = new FileReader(file);
            Map<?, ?> yamlData = YAMLFrontMatter.readFrontMatter(fr);
            if (yamlData.containsKey("template")) {
                String templateName = (String) yamlData.get("template");
                WakeFile template = new WakeFile(
                        config.getTemplateDir(), templateName);
                if (template.exists()) {
                    return template;
                }
            }
        } catch (FileNotFoundException e) {
            /* If the custom template is not found, we use the default. */
            //TODO warning log?
        }

        return defaultTemplate;
    }

    @Override
    public List<WakeFile> produces(WakeFile file) {
        WakeFile out = file.toOutputFile();
        String name = out.getNameWithoutExtension();
        name = name + ".html";
        WakeFile output = new WakeFile(out.getParent() + "/" + name);

        ArrayList<WakeFile> outputs = new ArrayList<>();
        outputs.add(output);
        return outputs;
    }

    @Override
    public List<WakeFile> process(WakeFile file) throws Exception {
        Configuration config = Configuration.instance();
        WakeFile template = determineTemplate(file);
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("file.resource.loader.path",
                config.getTemplateDir().getAbsolutePath());
        velocityEngine.init();
        Template markdownTemplate = velocityEngine.getTemplate(
                template.getName());

        String content = "";
        content = new String(Files.readAllBytes(file.toPath()));
        Map<?, ?> yamlData = YAMLFrontMatter.readFrontMatter(content);
        content = YAMLFrontMatter.removeFrontMatter(content);

        VelocityContext context = new VelocityContext(yamlData);

        Configuration.instance().getTitleMaker().makeTitle(context, file);

        Node document = markdownParser.parse(content);
        String html = htmlRenderer.render(document);
        context.put("markdown_content", html);

        WakeFile outputFile = produces(file).get(0);
        FileWriter writer = new FileWriter(outputFile);
        markdownTemplate.merge(context, writer);
        writer.close();

        List<WakeFile> outputs = new ArrayList<>();
        outputs.add(outputFile);
        return outputs;
    }
}
