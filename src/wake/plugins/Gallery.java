package wake.plugins;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
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
            String mimeType = null;
            try {
                mimeType = Files.probeContentType(file.toPath());
            } catch (Exception e) {
                /* Exceptions are ignored here; the fallback method will be used
                 * instead. */
            }

            /* Fallback method */
            if (mimeType == null) {
                mimeType = new MimetypesFileTypeMap().getContentType(file);
            }

            if (mimeType != null && mimeType.startsWith("image")) {
                /* File is an image, we want to process it. */
                return true;
            }
        }

        return false;
    }

    @Override
    public List<WakeFile> requires(WakeFile file) {
        return new ArrayList<WakeFile>();
    }

    @Override
    public List<WakeFile> produces(WakeFile file) {
        return new ArrayList<WakeFile>();
    }

    @Override
    public List<WakeFile> process(WakeFile file) throws Exception {
        System.out.println("Gallery!: " + file.toString());

        List<WakeFile> outputs = new ArrayList<>();
        return outputs;
    }
    }

    private String mimeType(File file) {
        String mimeType = null;
        try {
            mimeType = Files.probeContentType(file.toPath());
        } catch (Exception e) {
            /* Exceptions are ignored here; the fallback method will be used
             * instead. */
        }

        /* Fallback method */
        if (mimeType == null) {
            mimeType = new MimetypesFileTypeMap().getContentType(file);
        }

        return mimeType;
    }
}
