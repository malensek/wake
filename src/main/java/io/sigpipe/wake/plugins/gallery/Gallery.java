/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.plugins.gallery;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.KeepType;
import com.vladsch.flexmark.util.options.MutableDataSet;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import io.sigpipe.wake.core.Configuration;
import io.sigpipe.wake.core.Plugin;
import io.sigpipe.wake.core.PluginInitializationException;
import io.sigpipe.wake.core.TemplateUtils;
import io.sigpipe.wake.core.WakeFile;
import io.sigpipe.wake.util.Dataset;
import io.sigpipe.wake.util.MIME;
import io.sigpipe.wake.util.SharedDataset;
import io.sigpipe.wake.util.YAMLFrontMatter;

import net.coobird.thumbnailator.Thumbnails;

/**
 * Plugin to create a gallery of images.
 */
public class Gallery implements Plugin {

    protected static final String galleryFileName = "gallery_index.md";
    protected static final String galleryTemplate = "gallery.vm";

    private HtmlRenderer htmlRenderer;
    private Parser markdownParser;
    private WakeFile templateFile;
    private Template template;

    public Gallery()
    throws PluginInitializationException {
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.GITHUB_DOC);
        options.set(Parser.EXTENSIONS, Arrays.asList(
                AutolinkExtension.create(),
                AnchorLinkExtension.create(),
                StrikethroughSubscriptExtension.create(),
                TablesExtension.create(),
                TaskListExtension.create(),
                TocExtension.create(),
                TypographicExtension.create()
        ));

        /* GFM table parsing options */
        options.set(TablesExtension.COLUMN_SPANS, false)
            .set(TablesExtension.MIN_HEADER_ROWS, 1)
            .set(TablesExtension.MAX_HEADER_ROWS, 1)
            .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
            .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
            .set(TablesExtension.WITH_CAPTION, false)
            .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true);

        options
            .set(HtmlRenderer.INDENT_SIZE, 4)
            .set(HtmlRenderer.OBFUSCATE_EMAIL, true)
            .set(Parser.REFERENCES_KEEP, KeepType.LAST);

        Configuration config = Configuration.instance();

        templateFile = new WakeFile(config.getTemplateDir(), galleryTemplate);
        if (templateFile.exists() == false) {
            throw new PluginInitializationException(
                    "Could not locate gallery template");
        }
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("file.resource.loader.path",
                config.getTemplateDir().getAbsolutePath());
        velocityEngine.init();
        template = velocityEngine.getTemplate(templateFile.getName());

        markdownParser = Parser.builder(options).build();
        htmlRenderer = HtmlRenderer.builder(options).build();
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
            dependencies.add(this.templateFile);

            Configuration config = Configuration.instance();
            dependencies.addAll(
                TemplateUtils.getTemplateDependencies(
                    config, this.templateFile));
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

            DatasetAccessor gda = new DatasetAccessor(file.getParentFile());
            Dataset galleryParams = SharedDataset.instance().getDataset(gda);
            if (galleryParams.parseBoolean("retina", false) == true) {
                WakeFile thumb2x = thumbnailOutputFile(file, true);
                outputs.add(thumb2x);
            }
        }

        return outputs;
    }

    @Override
    public List<WakeFile> process(WakeFile file) throws Exception {
        List<WakeFile> outputs;

        if (isGalleryFile(file)) {
            /* Generate gallery html */
            outputs = generateIndex(file);
        } else {
            outputs = generateImages(file);
        }

        return outputs;
    }

    private List<WakeFile> generateIndex(WakeFile file) throws IOException {
        DatasetAccessor gda = new DatasetAccessor(file.getParentFile());
        Dataset galleryParams = SharedDataset.instance().getDataset(gda);
        boolean retina = galleryParams.parseBoolean("retina", false);

        File galleryDir = file.getParentFile();
        StringBuilder thumbnailHtml = new StringBuilder();
        File[] images = galleryDir.listFiles();
        Arrays.sort(images, Collections.reverseOrder());
        for (File image : images) {
            String mime = MIME.getMIMEType(image);
            if (mime.startsWith("image") == false) {
                continue;
            }
            WakeFile imageFile = new WakeFile(image.getAbsolutePath());

            ImageDescriptor imgDesc = new ImageDescriptor();
            imgDesc.description = ImageUtils.getEXIFImageDescription(imageFile);
            imgDesc.dims = ImageUtils.imageDimensions(imageFile);
            imgDesc.fileName = imageFile.getName();
            imgDesc.thumbnail = thumbnailOutputFile(imageFile).getName();
            if (retina == true) {
                imgDesc.thumbnail2x
                    = thumbnailOutputFile(imageFile, true).getName();
            }
            thumbnailHtml.append(imgDesc.toHTML());
        }

        String content = "";
        content = new String(Files.readAllBytes(file.toPath()));
        Map<?, ?> yamlData = YAMLFrontMatter.readFrontMatter(content);
        content = YAMLFrontMatter.removeFrontMatter(content);

        VelocityContext context = new VelocityContext(yamlData);
        Configuration.instance().getTitleMaker().makeTitle(context, file);

        Node document = markdownParser.parse(content);
        String html = htmlRenderer.render(document);
        context.put("markdown_content", html);
        context.put("thumbnail_content", thumbnailHtml.toString());

        WakeFile outputFile = produces(file).get(0);
        FileWriter writer = new FileWriter(outputFile);
        template.merge(context, writer);
        writer.close();

        List<WakeFile> outputs = new ArrayList<>();
        outputs.add(outputFile);
        return outputs;
    }

    private List<WakeFile> generateImages(WakeFile file) {
        DatasetAccessor gda = new DatasetAccessor(file.getParentFile());
        Dataset galleryParams = SharedDataset.instance().getDataset(gda);

        int maxSize = galleryParams.parseInt("imageSize", 600);
        int thumbSize = galleryParams.parseInt("thumbSize", 200);
        boolean retina = galleryParams.parseBoolean("retina", false);

        if (retina == true) {
            maxSize = maxSize * 2;
        }

        List<WakeFile> outputs = new ArrayList<>();
        try {
            WakeFile imageFile = file.toOutputFile();
            WakeFile thumbFile = thumbnailOutputFile(file);

            Thumbnails.of(file).size(maxSize, maxSize).toFile(imageFile);
            Thumbnails.of(file).size(thumbSize, thumbSize).toFile(thumbFile);

            outputs.add(imageFile);
            outputs.add(thumbFile);

            if (retina == true) {
                WakeFile thumbFile2x = thumbnailOutputFile(file, true);
                Thumbnails.of(file)
                    .size(thumbSize * 2, thumbSize * 2)
                    .toFile(thumbFile2x);
                outputs.add(thumbFile2x);
            }
        } catch (IOException e) {
            return outputs;
        }

        return outputs;
    }

    private boolean isGalleryFile(File file) {
        return file.getName().equals(galleryFileName);
    }

    /**
     * Retrieves the gallery definition file associated with the given file.
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
        return thumbnailOutputFile(file, false);
    }

    private WakeFile thumbnailOutputFile(WakeFile file, boolean retina) {
        String suffix = ".thumb";
        if (retina == true) {
            suffix += "2x";
        }

        WakeFile out = file.toOutputFile();
        WakeFile thumb = new WakeFile(out.getAbsolutePath() + suffix + ".jpg");

        return thumb;
    }

}
