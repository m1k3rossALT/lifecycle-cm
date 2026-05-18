package main.java.com.ecm.resource.controller;

import com.ecm.resource.entity.ContentObject;
import com.ecm.resource.service.ContentStoreService;
import com.ecm.resource.service.StoreContentCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for binary content storage and retrieval.
 *
 * <p>This controller is an internal API called only by ecm-library-service.
 * It is not exposed to end users directly. No authentication is performed
 * here — the library service authenticates the user and is trusted to call
 * this service only for authorized operations.</p>
 *
 * <p>Not responsible for: document lifecycle, access control, or PHI audit
 * logging — those are the library service's responsibilities.</p>
 */
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Content Storage", description = "Internal API for binary content upload, download, and deletion")
public class ContentObjectController {

    private final ContentStoreService contentStoreService;

    /**
     * Upload binary content. Returns the content_object ID which the library
     * service stores as document.content_reference_id.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload binary content and return content object ID")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentClassCode", required = false) String documentClassCode,
            @RequestParam(value = "uploadedByUserId", required = false) String uploadedByUserId
    ) throws Exception {

        StoreContentCommand command = new StoreContentCommand(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                documentClassCode,
                uploadedByUserId
        );

        ContentObject stored = contentStoreService.store(command);

        return ResponseEntity.ok(Map.of(
                "contentReferenceId", stored.getId(),
                "storageKey",         stored.getStorageKey(),
                "sha256Checksum",     stored.getSha256Checksum(),
                "fileSizeBytes",      stored.getFileSizeBytes()
        ));
    }

    /**
     * Download content by content object ID. Streams the binary directly
     * without loading the whole file into memory.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Download content by content object ID")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable UUID id) {
        // Retrieve metadata first to set the Content-Disposition header
        // before opening the stream — avoids header-already-sent errors.
        InputStream stream = contentStoreService.retrieve(id);

        StreamingResponseBody body = outputStream -> {
            try (InputStream in = stream) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"content-" + id + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    /**
     * Delete content from MinIO. Called by the library service during document
     * disposition. The metadata record is soft-deleted (preserved).
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete content from MinIO (soft-deletes metadata record)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contentStoreService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verify content integrity by recomputing its SHA-256 checksum.
     */
    @GetMapping("/{id}/checksum")
    @Operation(summary = "Verify content integrity against recorded SHA-256 checksum")
    public ResponseEntity<Map<String, Boolean>> verifyChecksum(@PathVariable UUID id) {
        boolean valid = contentStoreService.verifyChecksum(id);
        return ResponseEntity.ok(Map.of("checksumValid", valid));
    }
}
