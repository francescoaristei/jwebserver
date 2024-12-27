package org.example.http.util;

import org.example.http.errors.InvalidHttpRequestLineException;

import java.nio.file.Paths;
import java.util.regex.Pattern;

public class PathValidator {
    private static final Pattern VALID_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9/_.-]+$");

    public static String validatePath(String httpPath) throws InvalidHttpRequestLineException {
        String normalizedPath = Paths.get(httpPath).normalize().toString();
        normalizedPath = normalizedPath.replaceAll("^[/\\\\]+", "");

        if (normalizedPath.contains("..")) {
            throw new InvalidHttpRequestLineException("Cannot access resources outside allowed folder.");
        }

        if (!VALID_PATH_PATTERN.matcher(normalizedPath).matches()) {
            throw new InvalidHttpRequestLineException("Invalid characters in path");
        }

        return normalizedPath;
    }
}