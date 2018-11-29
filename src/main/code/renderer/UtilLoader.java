package main.code.renderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Implements a helper used to load a file
 *
 * @author Matei Vicovan-Hantascu
 */
public class UtilLoader {

    public static final String path = "src/main/resources/shaders";

    /**
     * loads a given file
     * @param fileName
     *      the given file
     * @return
     *      the data from file
     * @throws Exception
     */
    public static String loadResource(String fileName) throws Exception {

        String result;
        File file = new File(path + fileName);
        InputStream input = new FileInputStream(file);
        try (Scanner scanner = new Scanner(input, "UTF-8")) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }
}
