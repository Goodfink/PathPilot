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

    public ClassificationResult handlePDFFile(FileInfo fileInfo) {
        try (PDDocument doc = Loader.loadPDF(fileInfo.getFilePath().toFile())) {
            String text = stripper.getText(doc);
            fileInfo.setFileContent(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClassificationResult classificationResult = llmService.callClassifier(fileInfo);
        return classificationResult;
    }
}
