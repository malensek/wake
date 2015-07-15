package wake.plugins;

import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import com.esotericsoftware.yamlbeans.YamlReader;

import wake.core.Configuration;
import wake.core.Plugin;
import wake.core.WakeFile;

/**
 * Plugin to process Markdown (.md) files. This plugin takes a markdown file as
 * its input, reads its YAML front matter, converts the Markdown to HTML, and
 * then inserts the final markup into a Velocity template.
 *
 * @author malensek
 */
public class Markdown implements Plugin {

    /* Markdown files only have a single dependency other than themselves: the
     * markdown template. We cache this information here instead of creating a
     * new list each time requires() is called. */
    private List<WakeFile> dependencies;

    private VelocityEngine velocityEngine;
    private PegDownProcessor markdownProcessor;
    private Template markdownTemplate;

    public Markdown() {
        Configuration config = Configuration.instance();

        dependencies = new ArrayList<>();
        WakeFile template = new WakeFile(
                config.getTemplateDir(), "markdown.vm");
        dependencies.add(template);

        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("file.resource.loader.path",
                config.getTemplateDir().getAbsolutePath());
        velocityEngine.init();
        markdownTemplate = velocityEngine.getTemplate(
                template.getName());
 
        markdownProcessor = new PegDownProcessor(Extensions.ALL);
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
        return this.dependencies;
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
        String content = "";
        content = new String(Files.readAllBytes(file.toPath()));

        Map<?, ?> yamlData = new HashMap<>();

        if (content.startsWith("---")) {
            /* Read YAML front matter */
            YamlReader yaml = new YamlReader(content);
            yamlData = (Map<?, ?>) yaml.read();

            /* Trim past the 2nd YAML delimiter */
            content = content.substring(content.indexOf("---", 3) + 3);
        }

        VelocityContext context = new VelocityContext(yamlData);

        Configuration.instance().getTitleMaker().makeTitle(context, file);

        String html = markdownProcessor.markdownToHtml(content);
        context.put("markdown_content", html);

        WakeFile outputFile = produces(file).get(0);
        outputFile.mkParentDir();
        FileWriter writer = new FileWriter(outputFile);
        markdownTemplate.merge(context, writer);
        writer.close();

        List<WakeFile> outputs = new ArrayList<>();
        outputs.add(outputFile);
        return outputs;
    }
}
