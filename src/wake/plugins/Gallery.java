package wake.plugins;

import java.util.List;

import wake.core.Plugin;
import wake.core.WakeFile;

public class Gallery implements Plugin {

    @Override
    public String name() {
        return "Gallery";
    }

    @Override
    public boolean wants(WakeFile file) {
        return file.getExtension().equals("md");
    }

    @Override
    public List<WakeFile> requires(WakeFile file) {
        return null;
    }

    @Override
    public List<WakeFile> produces(WakeFile file) {
        return null;
    }

    @Override
    public void process(WakeFile file) throws Exception {

    }
}
