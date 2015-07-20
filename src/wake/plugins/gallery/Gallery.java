package wake.plugins.gallery;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import wake.core.Configuration;
import wake.core.Plugin;
import wake.core.WakeFile;
import wake.util.MIME;
import wake.util.SharedDataset;
import wake.util.YAMLFrontMatter;

public class Gallery implements Plugin {

    protected static final String galleryFileName = "gallery_index.md";

    private VelocityEngine velocityEngine;
    private PegDownProcessor markdownProcessor;
    private Template markdownTemplate;

    public Gallery() {
        Configuration config = Configuration.instance();

        WakeFile template = new WakeFile(
                config.getTemplateDir(), "gallery.vm");
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("file.resource.loader.path",
                config.getTemplateDir().getAbsolutePath());
        velocityEngine.init();
        markdownTemplate = velocityEngine.getTemplate(
                template.getName());

        markdownProcessor = new PegDownProcessor(Extensions.ALL);
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

    private List<WakeFile> generateIndex(WakeFile file) throws IOException {
        File galleryDir = file.getParentFile();
        StringBuilder thumbnailHtml = new StringBuilder();
        for (File image : galleryDir.listFiles()) {
            String mime = MIME.getMIMEType(image);
            if (mime.startsWith("image") == false) {
                continue;
            }
            WakeFile imageFile = new WakeFile(image.getAbsolutePath());

            ImageDescriptor imgDesc = new ImageDescriptor();
            imgDesc.fileName = imageFile.getName();
            imgDesc.thumbnail = thumbnailOutputFile(imageFile).getName();
            imgDesc.dims = ImageUtils.imageDimensions(imageFile);
            thumbnailHtml.append(imgDesc.toHTML());
        }

        String content = "";
        content = new String(Files.readAllBytes(file.toPath()));
        Map<?, ?> yamlData = YAMLFrontMatter.readFrontMatter(content);
        content = YAMLFrontMatter.removeFrontMatter(content);

        VelocityContext context = new VelocityContext(yamlData);
        Configuration.instance().getTitleMaker().makeTitle(context, file);

        String html = markdownProcessor.markdownToHtml(content);
        context.put("markdown_content", html);
        context.put("thumbnail_content", thumbnailHtml.toString());

        WakeFile outputFile = produces(file).get(0);
        outputFile.mkParentDir();
        FileWriter writer = new FileWriter(outputFile);
        markdownTemplate.merge(context, writer);
        writer.close();

        List<WakeFile> outputs = new ArrayList<>();
        outputs.add(outputFile);
        return outputs;
    }

    private List<WakeFile> generateImages(WakeFile file) {
        DatasetAccessor gda = new DatasetAccessor(file.getParentFile());
        Map<?, ?> galleryParams = SharedDataset.instance().getDataset(gda);

        int maxSize = Integer.parseInt(
                (String) galleryParams.get("imageSize"));
        int thumbSize = Integer.parseInt(
                (String) galleryParams.get("thumbSize"));

        List<WakeFile> outputs = new ArrayList<>();
        try {
            BufferedImage img = ImageIO.read(file);
            BufferedImage resizedImg = ImageUtils.scaleImage(
                    img, maxSize, maxSize);
            BufferedImage thumbnailImg = ImageUtils.scaleImage(
                    img, thumbSize, thumbSize);

            WakeFile imageFile = file.toOutputFile();
            WakeFile thumbFile = thumbnailOutputFile(file);

            imageFile.mkParentDir();
            ImageIO.write(resizedImg, "JPG", imageFile);
            ImageIO.write(thumbnailImg, "JPG", thumbnailOutputFile(file));

            outputs.add(imageFile);
            outputs.add(thumbFile);

        } catch (IOException e) {
            return outputs;
        }

        return outputs;
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
