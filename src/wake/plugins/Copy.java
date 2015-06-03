package wake.plugins;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import wake.core.Plugin;
import wake.core.WakeFile;

public class Copy implements Plugin {

    /* File copy has no dependencies: */
    private List<WakeFile> dependencies = new ArrayList<>();

    public Copy() {

    }

    @Override
    public String name() {
        return "Copy";
    }

    @Override
    public boolean wants(WakeFile file) {
        /* We'll copy anything we can get our hands on! */
        return true;
    }

    @Override
    public List<WakeFile> requires(WakeFile file) {
        return this.dependencies;
    }

    @Override
    public List<WakeFile> produces(WakeFile file) {
        WakeFile output = file.getOutputFile();
        ArrayList<WakeFile> outputs = new ArrayList<>();
        outputs.add(output);
        return outputs;
    }

    @Override
    public void process(WakeFile file) throws Exception {
        WakeFile output = file.getOutputFile();
        output.mkParentDir();
        Files.copy(file.toPath(), output.toPath());
        System.out.println(this.name() + ": "
                + file.getRelativePath() + " -> "
                + output.getRelativePath());
    }

}
