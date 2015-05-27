package jake.plugins;

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

import jake.core.Configuration;
import jake.core.Plugin;
import jake.core.WakeFile;

public class Markdown implements Plugin {

    /* Markdown files only have a single dependency other than themselves: the
     * markdown template. We cache this information here instead of creating a
     * new list each time requires() is called. */
    private List<WakeFile> dependencies;

    private VelocityEngine velocityEngine;
    private PegDownProcessor markdownProcessor;
    private Template markdownTemplate;

    public Markdown() {
        dependencies = new ArrayList<>();
        WakeFile template = new WakeFile(
                Configuration.getTemplateDir(), "markdown.vm");
        dependencies.add(template);

        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("file.resource.loader.path",
                Configuration.getTemplateDir().getAbsolutePath());
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
        WakeFile out = file.getOutputFile();
        String name = out.getNameWithoutExtension();
        name = name + ".html";
        WakeFile output = new WakeFile(out.getParent() + "/" + name);

        ArrayList<WakeFile> outputs = new ArrayList<>();
        outputs.add(output);
        return outputs;
    }

    @Override
    public void process(WakeFile file) throws Exception {
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

        String html = markdownProcessor.markdownToHtml(content);
        context.put("markdown_content", html);

        WakeFile outputFile = produces(file).get(0);
        outputFile.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(outputFile);
        markdownTemplate.merge(context, writer);
        writer.close();
    }
}
