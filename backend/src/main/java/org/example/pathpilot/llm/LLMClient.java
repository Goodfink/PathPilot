package org.example.pathpilot.llm;


import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponseCreateParams;
import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.folder.FolderInfo;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.example.pathpilot.repository.FoldersRepository;
import org.example.pathpilot.model.llm.LLMResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LLMClient {

    private static final int FOLDER_NOT_FOUND = -1;
    private static final String MODEL = "gpt-5-mini";
    private final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    private static final Logger log = LoggerFactory.getLogger(LLMClient.class);
    private final FoldersRepository foldersRepository;

    public LLMResponse requestClassification(String prompt) {

        try {
            StructuredResponseCreateParams<LLMResponse> params =
                    ResponseCreateParams.builder()
                            .model(MODEL)
                            .input(prompt)
                            .text(LLMResponse.class)
                            .build();

            var response = client.responses().create(params);

            return response.output().stream()
                    .flatMap(item -> item.message().stream())
                    .flatMap(message -> message.content().stream())
                    .flatMap(content -> content.outputText().stream())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No LLM response returned"));
        } catch (Exception e) {
            log.error("Error in LLM Call: llmError={}", e.getMessage());
            throw e;
        }
    }

    public ClassificationResult prepareFolderAndFindDestinationFolder(List<FileInfo> fileInfoList, String folderName) {
        List<FolderInfo> availableFolders = foldersRepository.getFolders();
        String availableFoldersString = getAvailableFoldersString(availableFolders);
        String prompt = buildFolderPrompt(fileInfoList, availableFoldersString, folderName);

        LLMResponse llmResponse = requestClassification(prompt);
        return processLLMResponse(llmResponse, availableFolders);
    }

    public ClassificationResult prepareFileAndFindDestinationFolder(FileInfo fileInfo) {
        List<FolderInfo> availableFolders = foldersRepository.getFolders();
        String availableFoldersString = getAvailableFoldersString(availableFolders);
        String prompt = buildFilePrompt(fileInfo, availableFoldersString);
        LLMResponse llmResponse = requestClassification(prompt);
        return processLLMResponse(llmResponse, availableFolders);
    }

    private ClassificationResult processLLMResponse(LLMResponse llmResponse, List<FolderInfo> availableFolders) {

        if (llmResponse.getFolderId() == FOLDER_NOT_FOUND) {
            return ClassificationResult.builder()
                    .path(null)
                    .confidence(0.0)
                    .build();
        }

        FolderInfo selectedFolder = availableFolders.stream()
                .filter(folder ->
                        folder.getId().equals((long) llmResponse.getFolderId()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "LLM returned invalid folder ID: "
                                        + llmResponse.getFolderId()
                        ));

        return ClassificationResult.builder()
                .path(selectedFolder.getPath())
                .confidence(llmResponse.getConfidence())
                .build();
    }

    private String getAvailableFoldersString(List<FolderInfo> folderInfoList) {
        return folderInfoList.stream()
                .map(folder -> "%d | %s".formatted(folder.getId(), folder.getPath()))
                .collect(Collectors.joining("\n"));
    }


    private String buildFilePrompt(FileInfo fileInfo, String folderList) {
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

    private String buildFolderPrompt(List<FileInfo> fileInfoList, String folderList, String folderName) {
        String fileSummaries = fileInfoList.stream()
                .map(file -> """
                    File Name:
                      %s

                    File Content:
                      %s
                    """.formatted(file.getFileName(), file.getFileContent()))
                .collect(Collectors.joining("\n---\n"));

        return """
            Classify which destination folder the downloaded folder best belongs in.

            Treat the downloaded folder as one package. Do not classify the sampled files
            separately; use them only as evidence for where the whole folder should go.

            If the downloaded folder does not clearly belong in any available destination
            folder, return folderId set to -1.

            confidence must be between 0.0 and 1.0.

            Downloaded Folder Name:
              %s

            Sampled Files In Downloaded Folder:
            %s

            Available Destination Folders:
            %s
            """.formatted(folderName, fileSummaries, folderList);
    }

}
