package org.example.pathpilot.llm;


import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponseCreateParams;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.folder.FolderInfo;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.example.pathpilot.model.llm.LLMResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LLMClient {

    private static final String MODEL = "gpt-5-mini";
    private final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    private static final Logger log = LoggerFactory.getLogger(LLMClient.class);

    public ClassificationResult classifyDestinationFolder(FileInfo fileInfo, List<FolderInfo> availableFolders) {


        log.info("here");
        String folderList = availableFolders.stream()
                .map(folder -> "%d | %s".formatted(folder.getId(), folder.getPath()))
                .collect(Collectors.joining("\n"));
        log.info("here");

        StructuredResponseCreateParams<LLMResponse> params =
                ResponseCreateParams.builder()
                        .model(MODEL)
                        .input(buildPrompt(fileInfo, folderList))
                        .text(LLMResponse.class)
                        .build();
        log.info("here");

        var response = client.responses().create(params);
        log.info("here");

        LLMResponse llmResponse = response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No LLM response returned"));
        log.info("here");

        if (llmResponse.getFolderId() == -1) {
            return ClassificationResult.builder()
                    .path(null)
                    .confidence(llmResponse.getConfidence())
                    .build();
        }
        log.info("here");

        FolderInfo selectedFolder = availableFolders.stream()
                .filter(folder ->
                        folder.getId().equals((long) llmResponse.getFolderId()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "LLM returned invalid folder ID: "
                                + llmResponse.getFolderId()
                        ));
        log.info("here");

        ClassificationResult result = ClassificationResult.builder()
                .path(selectedFolder.getPath())
                .confidence(llmResponse.getConfidence())
                .build();
        log.info("here");

        log.info("LLM classification complete: result = {}", result);

        return result;
    }

    private String buildPrompt(FileInfo fileInfo, String folderList) {
        return """
                Classify and return which folder the given file best belongs too accounting.
                
                If you do not believe the file makes sense to put in any folders, return folderId set to -1.
                
                confidence must be between 0.0 and 1.0.
                
                File Name:
                  %s
                  
                File Content:
                  %s
                  
                Available Folders:
                  %s
                """.formatted(fileInfo.getFileName(), fileInfo.getFileContent(), folderList);
    }
}
