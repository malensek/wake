package jake;

import java.util.ArrayList;
import java.util.List;

public class Markdown implements Plugin {

    private List<WakeFile> dependencies;

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
        String name = file.getNameWithoutExtension();
        name = name + ".md";
        WakeFile output = new WakeFile(file.getAbsolutePath() + "/" + name);

        ArrayList<WakeFile> outputs = new ArrayList<>();
        outputs.add(output);
        return outputs;
    }

    @Override
    public void process(WakeFile file) {

    }
}
