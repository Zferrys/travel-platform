package com.travel.service;

import com.travel.config.UploadDirResolver;
import com.travel.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOW_TYPES = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    private Path uploadDir;

    @PostConstruct
    public void init() {
        uploadDir = UploadDirResolver.resolve();
        log.info("Using upload directory: {}", uploadDir);
    }

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException(400, "Empty file");
        if (file.getSize() > MAX_SIZE) throw new BusinessException(400, "File too large");

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) throw new BusinessException(400, "Invalid filename");
        String ext = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
        if (!Arrays.asList(ALLOW_TYPES).contains(ext)) throw new BusinessException(400, "Unsupported file type");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException(400, "Content-Type is not an image");
        }

        // Read bytes to validate image content using ImageIO
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read uploaded file bytes", e);
            throw new BusinessException(500, "Failed to read file");
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            BufferedImage img = ImageIO.read(bais);
            if (img == null) {
                log.warn("Uploaded file rejected: not a valid image: {}", originalName);
                throw new BusinessException(400, "Uploaded file is not a valid image");
            }
        } catch (IOException e) {
            log.error("Image validation failed", e);
            throw new BusinessException(400, "Invalid image file");
        }

        String newName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = uploadDir.resolve(newName);
        try {
            Files.write(target, data);
            log.info("Stored uploaded image: {} (size={} bytes)", target, data.length);
            // Return URL path accessible via /uploads/ resource mapping
            return "/uploads/" + newName;
        } catch (IOException e) {
            log.error("Failed to store uploaded file", e);
            throw new BusinessException(500, "Failed to store file");
        }
    }
}
