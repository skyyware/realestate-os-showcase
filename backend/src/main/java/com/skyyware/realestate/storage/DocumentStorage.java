package com.skyyware.realestate.storage;

import com.skyyware.realestate.config.RealEstateProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

@Service
public class DocumentStorage {
    private static final long DEFAULT_MAX_SIZE = 25L * 1024L * 1024L;

    private final Path rootPath;
    private final long maxFileSizeBytes;

    public DocumentStorage(RealEstateProperties properties) {
        this.rootPath = Path.of(properties.documentStorage().rootPath()).toAbsolutePath().normalize();
        this.maxFileSizeBytes = properties.documentStorage().maxFileSizeBytes() > 0
                ? properties.documentStorage().maxFileSizeBytes()
                : DEFAULT_MAX_SIZE;
    }

    public StoredDocument store(UUID documentId, String originalFileName, String contentType, long declaredSize, InputStream inputStream) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Bitte eine Datei auswählen.");
        }
        if (declaredSize > maxFileSizeBytes) {
            throw new IllegalArgumentException("Die Datei ist größer als erlaubt.");
        }
        try {
            Files.createDirectories(rootPath);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            Path tempFile = Files.createTempFile(rootPath, "document-", ".tmp");
            long bytesWritten = 0;
            try (DigestInputStream digestInput = new DigestInputStream(inputStream, digest);
                 var output = Files.newOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = digestInput.read(buffer)) != -1) {
                    bytesWritten += read;
                    if (bytesWritten > maxFileSizeBytes) {
                        throw new IllegalArgumentException("Die Datei ist größer als erlaubt.");
                    }
                    output.write(buffer, 0, read);
                }
            } catch (IOException | RuntimeException exception) {
                Files.deleteIfExists(tempFile);
                throw exception;
            }
            if (bytesWritten == 0) {
                Files.deleteIfExists(tempFile);
                throw new IllegalArgumentException("Die Datei ist leer.");
            }
            String safeFileName = sanitize(originalFileName);
            Path documentDirectory = rootPath.resolve(documentId.toString()).normalize();
            Files.createDirectories(documentDirectory);
            Path target = documentDirectory.resolve(safeFileName).normalize();
            if (!target.startsWith(rootPath)) {
                throw new IllegalArgumentException("Dateiname ist ungültig.");
            }
            Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
            String storageKey = rootPath.relativize(target).toString().replace('\\', '/');
            String resolvedContentType = contentType == null || contentType.isBlank()
                    ? "application/octet-stream"
                    : contentType;
            return new StoredDocument(storageKey, resolvedContentType, bytesWritten, HexFormat.of().formatHex(digest.digest()));
        } catch (NoSuchAlgorithmException | IOException exception) {
            throw new IllegalStateException("Dokument konnte nicht gespeichert werden.", exception);
        }
    }

    public StoredDocumentFile load(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("Dokumentdatei ist nicht verfügbar.");
        }
        Path file = rootPath.resolve(storageKey).normalize();
        if (!file.startsWith(rootPath) || !Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Dokumentdatei ist nicht verfügbar.");
        }
        try {
            return new StoredDocumentFile(new FileSystemResource(file), Files.size(file));
        } catch (IOException exception) {
            throw new IllegalStateException("Dokument konnte nicht gelesen werden.", exception);
        }
    }

    private static String sanitize(String fileName) {
        String normalized = Path.of(fileName).getFileName().toString();
        String safe = normalized.replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        return safe.isBlank() ? "dokument.bin" : safe;
    }

    public record StoredDocument(String storageKey, String contentType, long fileSizeBytes, String sha256Checksum) {
    }

    public record StoredDocumentFile(FileSystemResource resource, long fileSizeBytes) {
    }
}
