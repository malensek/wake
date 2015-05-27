package jake;

import java.io.File;

public class FileUtil {

    public static String extension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return name.substring(dotIndex + 1);
    }

    public static String nameWithoutExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex == -1) {
            return name;
        }
        return name.substring(0, dotIndex);
    }
}
