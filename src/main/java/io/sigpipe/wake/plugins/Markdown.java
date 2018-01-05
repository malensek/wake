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

package io.sigpipe.wake.plugins;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
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
        MutableDataSet options = Configuration.instance().getMarkdownOptions();
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
        List<WakeFile> dependencies = new ArrayList<>();
        WakeFile template = determineTemplate(file);
        dependencies.add(template);
        dependencies.addAll(
                TemplateUtils.getTemplateDependencies(
                    Configuration.instance(), template));
        return dependencies;
    }

    private WakeFile determineTemplate(WakeFile file) {
        Configuration config = Configuration.instance();
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
            //TODO warning log?
        }

        /* If the custom template is not found, we use the default. */
        WakeFile defaultTemplate = new WakeFile(
                config.getTemplateDir(), "markdown.vm");
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
    public void process(WakeFile file) throws Exception {
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

        config.getTitleMaker().makeTitle(context, file);

        Node document = markdownParser.parse(content);
        String html = htmlRenderer.render(document);
        context.put("markdown_content", html);

        WakeFile outputFile = produces(file).get(0);
        FileWriter writer = new FileWriter(outputFile);
        markdownTemplate.merge(context, writer);
        writer.close();
    }
}
