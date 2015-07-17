package wake.util;

import java.io.File;
import java.nio.file.Files;

import javax.activation.MimetypesFileTypeMap;

/**
 * Handles MIME functionality.
 *
 * @author malensek
 */
public class MIME {

    /**
     * Retrieves the MIME type of the specified file.
     * @param file The File to retrieve the MIME type of.
     * @return MIME type String, or null if no MIME type could be determined.
     */
    public static String getMIMEType(File file) {
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

    /**
     * Retrieves the MIME type of the specified file name.
     * @param fileName Name of the file to retrieve the MIME type from.
     * @return MIME type String, or null if no MIME type could be determined.
     */
    public static String getMIMEType(String fileName) {
        return getMIMEType(new File(fileName));
    }

}
