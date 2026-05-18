package com.example.qlbds.shared.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileUploadService {

    private final Cloudinary cloudinary;

    public FileUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        String publicId = UUID.randomUUID().toString();
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folderName,
                "public_id", publicId
        ));
        return uploadResult.get("secure_url").toString();
    }

    public List<String> uploadMultipleFiles(List<MultipartFile> files, String folderName) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file, folderName));
        }
        return urls;
    }

    public void deleteFile(String imageUrl) throws IOException {
        // Extract public ID from URL if necessary or pass public ID directly
        // Cloudinary deletion is based on public_id
        // This is a simplified approach, usually we should parse the publicId from the url
        String publicId = extractPublicId(imageUrl);
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    private String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("/")) {
            return null;
        }
        try {
            int lastSlashIndex = imageUrl.lastIndexOf('/');
            int dotIndex = imageUrl.lastIndexOf('.');
            if (lastSlashIndex != -1 && dotIndex != -1 && lastSlashIndex < dotIndex) {
                // If there's a folder, it's better to store publicId in the DB.
                // For now, this is a basic extraction which might not include folder name
                return imageUrl.substring(lastSlashIndex + 1, dotIndex);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
