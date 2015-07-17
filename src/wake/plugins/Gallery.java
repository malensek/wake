package wake.plugins;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import wake.core.Plugin;
import wake.core.WakeFile;

public class Gallery implements Plugin {

    static {
        /* Disable the Mac OS X AWT dock icon: */
        System.setProperty("apple.awt.UIElement", "true");
    }

    private static final String galleryFileName = "index.md";

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
            String mimeType = mimeType(file);
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
    private BufferedImage scaleImage(
            BufferedImage source, int width, int height) {

        int scaledWidth;
        int scaledHeight;

        int sw = source.getWidth();
        int sh = source.getHeight();

        if (width > sw && height > sh) {
            /* Don't increase the size of images that are too small */
            return source;
        }

        if (sw == sh) {
            /* If dimensions are equal, we don't need to worry about scaling */
            return resizeImage(source, width, height);
        }

        if (sw > sh) {
            scaledWidth = width;
            scaledHeight = Math.round(sh * ((float) width / (float) sw));
        } else {
            scaledHeight = height;
            scaledWidth = Math.round(sw * ((float) height / (float) sh));
        }

        return resizeImage(source, scaledWidth, scaledHeight);
    }

    private BufferedImage resizeImage(
            BufferedImage source, int width, int height) {

        BufferedImage resizedImg = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = resizedImg.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();

        return resizedImg;
    }

    private boolean isGalleryFile(File file) {
        return file.getName().equals(galleryFileName);
    }

    /**
     * Retrieves the gallery definition file (gallery.yaml) associated with the
     * given file.
     */
    private WakeFile galleryFile(File file) {
        File parent = file.getParentFile();
        return new WakeFile(parent.getAbsolutePath() + "/" + galleryFileName);
    }

    /**
     * Retrieves the location of the gallery HTML output file (the gallery
     * index).
     */
    private WakeFile indexOutputFile(WakeFile file) {
        WakeFile outputDir = file.getParentFile().toOutputFile();
        return new WakeFile(outputDir.getAbsolutePath() + "/index.html");
    }

    private WakeFile thumbnailOutputFile(WakeFile file) {
        WakeFile out = file.toOutputFile();
        WakeFile thumb = new WakeFile(out.getAbsolutePath() + ".thumb.jpg");
        return thumb;
    }

    /**
     * Determines the dimensions of an image without reading the entire file.
     *
     * @return Dimensions if found, otherwise null.
     */
    private Dimension imageDimensions(File image) {
        String mime = mimeType(image);
        Iterator<ImageReader> it = ImageIO.getImageReadersByMIMEType(mime);
        while (it.hasNext()) {
            ImageReader reader = it.next();
            try (ImageInputStream iis = new FileImageInputStream(image)) {
                reader.setInput(iis);
                int width = reader.getWidth(reader.getMinIndex());
                int height = reader.getHeight(reader.getMinIndex());
                return new Dimension(width, height);
            } catch (IOException e) {
                continue;
            }
        }
        return null;
    }

    /**
     * Retrieves the MIME type of the specified file.
     */
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
