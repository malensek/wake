package wake.plugins;

import java.io.File;
import java.util.List;

import wake.core.Plugin;
import wake.core.WakeFile;

public class Gallery implements Plugin {

    private static final String galleryFileName = "gallery.json";

    @Override
    public String name() {
        return "Gallery";
    }

    @Override
    public boolean wants(WakeFile file) {
        if (file.getName().equals(galleryFileName)) {
            return true;
        }

        File fileDir = file.getParentFile();
        File galleryFile = new File(
                fileDir.getAbsolutePath() + "/" + galleryFileName);
        if (galleryFile.exists()) {
            /* TODO: is this file an image? */

        }

        return false;
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
