package org.example.pathpilot.service.image;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.file.FileInfo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ImageFileService {

    public FileInfo getImageContent(FileInfo fileInfo) {

        String imageUrl;

        try {
            byte[] imageBytes = Files.readAllBytes(fileInfo.getFilePath());
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            imageUrl = String.format("data:image/%s;base64,%s", getMimeSubtype(fileInfo.getFileExtension()), base64);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileInfo fileInfoWithContent = fileInfo;
        fileInfoWithContent.setFileContent(imageUrl);

        return fileInfoWithContent;
    }

    private String getMimeSubtype(String fileExtension) {
        String normalizedExtension = fileExtension.toLowerCase(Locale.ROOT);
        return normalizedExtension.equals("jpg") ? "jpeg" : normalizedExtension;
    }
}
