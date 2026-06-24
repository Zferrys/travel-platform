package com.travel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 统一的上传目录解析，FileService 和 WebMvcConfig 共用。
 */
public final class UploadDirResolver {

    private static final Logger log = LoggerFactory.getLogger(UploadDirResolver.class);

    private UploadDirResolver() {}

    public static Path resolve() {
        String env = System.getenv("UPLOAD_DIR");
        String prop = System.getProperty("upload.dir");
        String dirToUse = (prop != null && !prop.isEmpty()) ? prop
                        : (env != null && !env.isEmpty()) ? env
                        : System.getProperty("user.home") + "\\travel_upload";
        Path path = Paths.get(dirToUse).toAbsolutePath().normalize();
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            log.error("Failed to create upload directory: {}", path, e);
        }
        return path;
    }
}
