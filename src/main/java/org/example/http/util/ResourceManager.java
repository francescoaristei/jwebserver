package org.example.http.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.http.errors.InvalidHttpRequestLineException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.example.http.util.PathValidator.validatePath;

public class ResourceManager {
    private static final Logger logger = LogManager.getLogger(ResourceManager.class);

    public static void createResource(String path, StringBuilder body) throws IOException, InvalidHttpRequestLineException {
        String sanitizedHttpPath = validatePath(path);
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(String.format("user-uploads/%s", sanitizedHttpPath)))) {
            bufferedWriter.write(String.valueOf(body));
        }
    }

    public static BufferedReader getResourceContent(String httpPath) throws IOException, InvalidHttpRequestLineException {
        String sanitizedHttpPath = httpPath.equals("/") ? "index.html" : validatePath(httpPath);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(String.format("www/%s", sanitizedHttpPath));
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            return new BufferedReader(inputStreamReader);
        } else {
            logger.error("Resource not found: {}", sanitizedHttpPath);
            throw new IOException("No such file in the resources folder.");
        }
    }

    public static String getFileExtensions(String path) {
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex >= 0) {
            return path.substring(dotIndex + 1);
        }
        return "";
    }
}
