package wake.plugins;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import wake.core.Plugin;
import wake.core.WakeFile;
import wake.util.ImageUtils;
import wake.util.MIME;
import wake.util.SharedDataset;
import wake.util.SharedDatasetAccessor;
import wake.util.YAMLFrontMatter;

public class Gallery implements Plugin {

    private static final String galleryFileName = "index.md";

    private static final String figureTag
        = "<figure itemprop=\"associatedMedia\" itemscope "
        + "itemtype=\"http://schema.org/ImageObject\">";

    private class GalleryImageDescriptor {
        public String fileName;
        public String thumbnail;
        public Dimension dims;

        public String toHTML() {
            StringBuilder sb = new StringBuilder();
            sb.append(figureTag);
            sb.append("<a href=\"" + fileName + "\" itemprop=\"contentUrl\" "
                    + "data-size=\"" + dims.width + "x" + dims.height + "\">");
            sb.append("<img src=\"" + thumbnail + "\" itemprop=\"thumbnail\" "
                    + "alt=\"test\" width=\"200\" height=\"200\">");
            sb.append("</a></figure>");
            return sb.toString();
        }
    }

    private class GalleryDatasetAccessor implements SharedDatasetAccessor {

        private File galleryDir;

        public GalleryDatasetAccessor(File galleryDir) {
            this.galleryDir = galleryDir;
        }

        public Map<?, ?> createDataset() {
            Path galleryPath = new File(galleryDir.getAbsolutePath()
                    + "/" + galleryFileName).toPath();

            String content;
            try {
                content = new String(Files.readAllBytes(galleryPath));
            } catch (IOException e) {
                return new HashMap<>();
            }

            return YAMLFrontMatter.readFrontMatter(content);
        }

        public String getDatasetID() {
            return galleryDir.getAbsolutePath();
        }
    }

    @Override
    public String name() {
        return "Gallery";
    }

    @Override
    public boolean wants(WakeFile file) {
        if (isGalleryFile(file)) {
            return true;
        }

        File fileDir = file.getParentFile();
        File galleryFile = new File(
                fileDir.getAbsolutePath() + "/" + galleryFileName);
        if (galleryFile.exists()) {
            String mimeType = MIME.getMIMEType(file);
            if (mimeType != null && mimeType.startsWith("image")) {
                /* File is an image, we want to process it. */
                return true;
            }
        }

        return false;
    }

    @Override
    public List<WakeFile> requires(WakeFile file) {
        List<WakeFile> dependencies = new ArrayList<>();

        if (isGalleryFile(file)) {
            /* Add the gallery directory as a dependency so that file changes
             * (such as an image being deleted) will be caught */
            dependencies.add(file.getParentFile());
        } else {
            dependencies.add(galleryFile(file));
        }

        return dependencies;
    }

    @Override
    public List<WakeFile> produces(WakeFile file) {
        List<WakeFile> outputs = new ArrayList<>();

        if (isGalleryFile(file)) {
            outputs.add(indexOutputFile(file));
        } else {
            WakeFile image = file.toOutputFile();
            WakeFile thumb = thumbnailOutputFile(file);
            outputs.add(image);
            outputs.add(thumb);
        }

        return outputs;
    }

    @Override
    public List<WakeFile> process(WakeFile file) throws Exception {
        List<WakeFile> outputs = new ArrayList<>();

        if (isGalleryFile(file)) {
            /* Generate gallery html */
            generateIndex(file);
            outputs.add(indexOutputFile(file));
        } else {
            generateImages(file);
        }

        return outputs;
    }

    private void generateIndex(WakeFile file) {
        File galleryDir = file.getParentFile();
        for (File f : galleryDir.listFiles()) {
            String mime = MIME.getMIMEType(f);
            if (mime.startsWith("image") == false) {
                continue;
            }

            GalleryImageDescriptor imgDesc = new GalleryImageDescriptor();
            imgDesc.fileName = f.getName();
            imgDesc.thumbnail = thumbnailOutputFile(file).getName();
            imgDesc.dims = ImageUtils.imageDimensions(f);
            imgDesc.toHTML();
            System.out.println(imgDesc.toHTML());
        }
    }

    private void generateImages(WakeFile file) {
        GalleryDatasetAccessor gda = new GalleryDatasetAccessor(
                file.getParentFile());
        Map<?, ?> galleryParams = SharedDataset.instance().getDataset(gda);

        int maxSize = Integer.parseInt(
                (String) galleryParams.get("imageSize"));
        int thumbSize = Integer.parseInt(
                (String) galleryParams.get("thumbSize"));

        try {
            BufferedImage img = ImageIO.read(file);
            BufferedImage resizedImg = ImageUtils.scaleImage(
                    img, maxSize, maxSize);
            BufferedImage thumbnailImg = ImageUtils.scaleImage(
                    img, thumbSize, thumbSize);
            ImageIO.write(resizedImg, "JPG", file.toOutputFile());
            ImageIO.write(thumbnailImg, "JPG", thumbnailOutputFile(file));

        } catch (IOException e) {
            return;
        }
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

}
