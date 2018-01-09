package io.sigpipe.wake.plugins;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class CodeListing implements Plugin {

    private Parser markdownParser;
    private HtmlRenderer htmlRenderer;
    private Set<String> codeExtensions;
    private WakeFile template;

    public CodeListing() {
        Configuration config = Configuration.instance();

        MutableDataSet options = config.getMarkdownOptions();
        markdownParser = Parser.builder(options).build();
        htmlRenderer = HtmlRenderer.builder(options).build();

        codeExtensions = new HashSet<>(Arrays.asList(
                    "c", "java", "py"
        ));

        template = new WakeFile(config.getTemplateDir(), "codelisting.vm");
    }

    @Override
    public String name() {
        return "Code";
    }

    @Override
    public boolean wants(WakeFile file) {
        return codeExtensions.contains(file.getExtension());
    }

    @Override
    public List<WakeFile> requires(WakeFile file) {
        List<WakeFile> dependencies = new ArrayList<>();
        dependencies.add(this.template);
        dependencies.addAll(
                TemplateUtils.getTemplateDependencies(
                    Configuration.instance(),
                    this.template));
        return dependencies;
    }

    @Override
    public List<WakeFile> produces(WakeFile file) {
        WakeFile out = file.toOutputFile();
        return Arrays.asList(
                out,
                new WakeFile(out.getParentFile(), out.getName() + ".html")
        );
    }

    @Override
    public void process(WakeFile file) throws Exception {
        Configuration config = Configuration.instance();

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("file.resource.loader.path",
                config.getTemplateDir().getAbsolutePath());
        velocityEngine.init();
        Template listingTemplate = velocityEngine.getTemplate(
                this.template.getName());

        VelocityContext context = new VelocityContext();
        List<WakeFile> outputs = this.produces(file);

        String content = "";
        content = new String(Files.readAllBytes(file.toPath()));
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append("```");
        sb.append(System.lineSeparator());
        sb.append(content);
        sb.append(System.lineSeparator());
        sb.append("```");
        sb.append(System.lineSeparator());
        Node document = markdownParser.parse(sb.toString());
        String html = htmlRenderer.render(document);

        context.put("code", html);
        context.put("title", file.getName());
        context.put("file-name", file.getName());
        context.put("raw-file", outputs.get(0).getName());

        /* Generate the code listing page */
        FileWriter writer = new FileWriter(outputs.get(1));
        listingTemplate.merge(context, writer);
        writer.close();

        /* Copy the raw file */
        Files.copy(file.toPath(), outputs.get(0).toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                LinkOption.NOFOLLOW_LINKS);
    }
}
