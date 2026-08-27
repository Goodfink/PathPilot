package org.example.pathpilot.service.duplicate;

import io.github.zabuzard.fastcdc4j.external.chunking.Chunk;
import io.github.zabuzard.fastcdc4j.external.chunking.Chunker;
import io.github.zabuzard.fastcdc4j.external.chunking.ChunkerBuilder;
import org.example.pathpilot.model.pendingMove.OperationType;
import org.example.pathpilot.helpers.FileHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class DuplicateService {

    Chunker chunker = new ChunkerBuilder().build();
    private final FileHelpers fileHelpers = new FileHelpers();

    private static final double IS_DUPLICATE_LEVEL = 0.95;

    private static final Logger log = LoggerFactory.getLogger(DuplicateService.class);


    public OperationType checkAndHandleDuplicate(Path toPath, Path fromPath) {
        if (toPath == null) {
            return OperationType.NO_MOVE;
        }

        Path fileName = fromPath.getFileName();
        Path targetPath = toPath.resolve(fileName);
        boolean fileNameExistsAtDestination = fileHelpers.fileExists(targetPath);
        OperationType operationType = OperationType.MOVE;

        if (fileNameExistsAtDestination) {
            boolean isDuplicate = isDuplicate(fromPath, targetPath);
            if (isDuplicate) {
                log.info("File already exists at destination: fileName={}", fileName);
                operationType = OperationType.TRASH;
            } else {
                operationType = OperationType.NO_MOVE;
            }
        } 

        return operationType;
    }

    private boolean isDuplicate(Path existingPath, Path newPath) {
        boolean isDuplicate = false;

        double similarity = getSimilarity(existingPath, newPath);
        log.info("Similarity rating: similarity={}", similarity);

        if (similarity > IS_DUPLICATE_LEVEL) {
            isDuplicate =  true;
        }

        return isDuplicate;
    }

    private double getSimilarity(Path existingPath, Path newPath) {
        HashSet<String> chunks = new HashSet<>();
        List<Chunk> existingPathHashes = StreamSupport
                .stream(chunker.chunk(existingPath).spliterator(), false)
                .toList();
        List<Chunk> newPathHashes = StreamSupport
                .stream(chunker.chunk(newPath).spliterator(), false)
                .toList();;

        log.info("hashes: existingPathHashes={}", existingPathHashes);
        log.info("hashes: newPathHashes={}", newPathHashes);

        double matches = 0;
        double denominator = Math.max(existingPathHashes.size(), newPathHashes.size());

        for (Chunk chunk : existingPathHashes) {
            chunks.add(chunk.getHexHash());
        }

        for (Chunk chunk : newPathHashes) {
            if (chunks.contains(chunk.getHexHash())) {
                matches += 1;
            }
        }

        if (denominator == 0) {
            return 1;
        }

        return matches / denominator;
    }
}
