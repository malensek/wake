package jake;

import java.util.ArrayList;
import java.util.List;

import org.pegdown.PegDownProcessor;

public class Markdown implements Plugin {

    /* Markdown files only have a single dependency other than themselves: the
     * markdown template. We cache this information here instead of creating a
     * new list each time requires() is called. */
    private List<WakeFile> dependencies;

    private PegDownProcessor pdp = new PegDownProcessor();

    public Markdown() {
        dependencies = new ArrayList<>();
        WakeFile tempDir = Configuration.getTemplateDir();
        WakeFile template = new WakeFile(tempDir, "markdown.vm");
        dependencies.add(template);
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
    public void process(WakeFile file) {

    }
}
