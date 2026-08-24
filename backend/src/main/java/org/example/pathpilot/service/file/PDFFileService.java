package org.example.pathpilot.service.file;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.pathpilot.service.llm.LLMService;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PDFFileService {

    PDFTextStripper stripper = new PDFTextStripper();
    private final LLMService llmService;

    public FileInfo getPDFFileContent(FileInfo fileInfo) {
        String text;
        try (PDDocument doc = Loader.loadPDF(fileInfo.getFilePath().toFile())) {
            text = stripper.getText(doc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileInfo fileInfoWithContent = fileInfo;
        fileInfoWithContent.setFileContent(text);

        return fileInfoWithContent;
    }
}
