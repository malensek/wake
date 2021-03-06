package io.sigpipe.wake.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import io.sigpipe.wake.core.Plugin;
import io.sigpipe.wake.core.WakeFile;

public class Symlink implements Plugin {

    /* No dependencies: */
    private List<WakeFile> dependencies = new ArrayList<>();

    public Symlink() {

    }

    @Override
    public String name() {
        return "Symlink";
    }

    @Override
    public boolean wants(WakeFile file) {
        return file.getExtension().equals("symlink");
    }

    @Override
    public List<WakeFile> requires(WakeFile file) {
        return this.dependencies;
    }

    @Override
    public List<WakeFile> produces(WakeFile file) {
        /* Produces a symlink file without the .symlink extension */
        WakeFile out = file.toOutputFile();
        WakeFile output = new WakeFile(
                out.getParent() + "/" + out.getNameWithoutExtension());

        ArrayList<WakeFile> outputs = new ArrayList<>();
        outputs.add(output);
        return outputs;
    }

    @Override public void process(WakeFile file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String linkDest = br.readLine();
        br.close();

        File destFile = new File(linkDest);
        WakeFile output = this.produces(file).get(0);
        if (Files.isSymbolicLink(output.toPath())) {
            System.out.println("exists");
            output.delete();
        }

        Files.createSymbolicLink(output.toPath(), destFile.toPath());
    }
}
