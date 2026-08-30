package org.example.pathpilot.service.file;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.file.FileHandleType;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.service.image.ImageFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileContentService {

    private final DOCXFileService docxFileService;
    private final PDFFileService pdfFileService;
    private final PlainTextFileService plainTextFileService;
    private final ImageFileService imageFileService;

    private static final Logger log = LoggerFactory.getLogger(FileContentService.class);

    public FileInfo handleExtensionCase(FileInfo fileInfo) throws IOException {
        FileInfo result =  FileInfo.builder().build();
        try {
            switch (fileInfo.getFileHandleType()) {
                case FileHandleType.DOCX:
                    log.info("Handling DOCX file: {}", fileInfo.getFileName());
                    result = docxFileService.getDOCXFileContent(fileInfo);
                    break;
                case FileHandleType.PDF:
                    log.info("Handling PDF file: {}", fileInfo.getFileName());
                    result = pdfFileService.getPDFFileContent(fileInfo);
                    break;
                case FileHandleType.IMAGE:
                    log.info("Handling image file: {}", fileInfo.getFileName());
                    result = imageFileService.getImageContent(fileInfo);
                    break;
                case FileHandleType.PLAIN_TEXT:
                    log.info("Handling plain text file: {}", fileInfo.getFileName());
                    result = plainTextFileService.getPlainTextFileContent(fileInfo);
                    break;
            }
        } catch (RuntimeException e) {
            log.error("There was an issue processing the file: errorMessage={}", e.getMessage());
        }

        log.info("file processing complete: result={}", result);
        return result;
    }
}
