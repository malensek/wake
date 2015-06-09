package wake.plugins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import wake.core.Plugin;
import wake.core.WakeFile;

public class Gallery implements Plugin {

    static {
        /* Disable the Mac OS X AWT dock icon: */
        System.setProperty("apple.awt.UIElement", "true");
    }

    private static final String galleryFileName = "gallery.yaml";

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

            BufferedImage img = null;
            int type;
            try {
                img = ImageIO.read(file);
                type = img.getType();
            } catch (Exception e) {
                System.out.println("Gallery: '" + file.getRelativePath() + "' "
                        + "is not an image or cannot be loaded.");
            }

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
