package org.example.pathpilot.service.file;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.springframework.stereotype.Service;
import org.example.pathpilot.service.llm.LLMService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class DOCXFileService {

    private final LLMService llmService;

    public FileInfo getDOCXFileContent(FileInfo fileInfo) {

        String fileContent;

        try (InputStream stream = Files.newInputStream(fileInfo.getFilePath())){
            XWPFDocument document = new XWPFDocument(stream);

             fileContent = document.getParagraphs()
                    .stream()
                    .map(XWPFParagraph::getText)
                    .toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileInfo fileInfoWithContent = fileInfo;
        fileInfoWithContent.setFileContent(fileContent);

        return fileInfoWithContent;
    }
}
