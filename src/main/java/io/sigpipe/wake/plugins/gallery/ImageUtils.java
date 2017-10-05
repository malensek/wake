/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.plugins.gallery;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

public class ImageUtils {

    private ImageUtils() {

    }

    public static boolean isImage(File image) {
        try {
            FileType fileType = FileTypeDetector.detectFileType(
                    new BufferedInputStream(new FileInputStream(image)));
            return fileType != FileType.Unknown;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Determines the dimensions of an image without reading the entire file.
     *
     * @return Dimensions if found, otherwise null.
     */
    public static Dimension imageDimensions(File image) {
        try (ImageInputStream in = ImageIO.createImageInputStream(image)) {
            Iterator<ImageReader> it = ImageIO.getImageReaders(in);
            while (it.hasNext()) {
                ImageReader reader = it.next();
                try {
                    reader.setInput(in);
                    return new Dimension(
                            reader.getWidth(reader.getMinIndex()),
                            reader.getHeight(reader.getMinIndex()));
                } finally {
                    reader.dispose();
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public static BufferedImage scaleImage(
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

    public static BufferedImage resizeImage(
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

    /**
     * Retrieves the ImageDescription EXIF information from the main image
     * (IFD0).
     *
     * @param file Image file to retrieve EXIF data from
     *
     * @return String containing the image description
     */
    public static String getEXIFImageDescription(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifIFD0Directory directory
                = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            String comment = directory.getDescription(
                    ExifIFD0Directory.TAG_IMAGE_DESCRIPTION);
            if (comment != null) {
                return comment;
            }
        } catch (Exception e) {
            /* If the user comment couldn't be read, fail silently and go with
             * the default description (empty string) */
        }

        return "";
    }

}
