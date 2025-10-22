package com.bunkermuseum.membermanagement.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Contract for MinIO object storage service operations.
 *
 * <p>This interface defines the standard operations for managing binary data
 * in MinIO object storage. It provides a unified API for file upload,
 * download, deletion, and URL generation, abstracting away the underlying
 * storage implementation details.</p>
 *
 * <h3>Implementation Requirements:</h3>
 * <ul>
 *     <li>Implementations must ensure thread-safety for all operations</li>
 *     <li>All methods must handle null inputs gracefully</li>
 *     <li>Failed operations must throw appropriate runtime exceptions</li>
 *     <li>Implementations should log all operations for audit trails</li>
 *     <li>Resource cleanup (streams, connections) must be handled properly</li>
 * </ul>
 *
 * <h3>Storage Organization:</h3>
 * <ul>
 *     <li><strong>Bucket/Container:</strong> Top-level storage namespace</li>
 *     <li><strong>Folders:</strong> Logical grouping of objects (e.g., "profile-pictures")</li>
 *     <li><strong>Object Name:</strong> Full path to the stored file (folder + unique filename)</li>
 * </ul>
 *
 * <h3>Error Handling:</h3>
 * <p>All methods may throw RuntimeException with descriptive messages when
 * operations fail. Callers should catch and handle these exceptions appropriately.</p>
 *
 * @see MinioService
 */
public interface MinioServiceContract {

    /**
     * Uploads a file to the object storage system.
     *
     * <p>This method stores the provided file in the specified folder within
     * the storage bucket. The file is assigned a unique identifier to prevent
     * name collisions. The original file extension is preserved.</p>
     *
     * <h3>Upload Process:</h3>
     * <ol>
     *     <li>Validates that the file is not null or empty</li>
     *     <li>Extracts the file extension from the original filename</li>
     *     <li>Generates a unique filename using UUID</li>
     *     <li>Constructs the full object name: folder/uuid.extension</li>
     *     <li>Uploads the file with appropriate content type</li>
     *     <li>Returns the object name for future reference</li>
     * </ol>
     *
     * <h3>Transaction Handling:</h3>
     * <p>Implementations should ensure atomicity - either the file is fully uploaded
     * or the operation fails completely without partial state.</p>
     *
     * @param file The multipart file to upload. Must not be null or empty.
     *             Should contain valid binary data and a filename with extension.
     * @param folder The logical folder (prefix) within the storage bucket where
     *               the file should be stored. Common values: "profile-pictures",
     *               "documents", "attachments". Must not be null.
     *
     * @return The complete object name (path) of the uploaded file in the format
     *         "folder/uuid.extension". This identifier should be stored in the
     *         database for future retrieval operations.
     *
     * @throws IllegalArgumentException if file is null, empty, or folder is null/blank
     * @throws IOException if reading the file input stream fails
     * @throws RuntimeException if the upload operation fails due to storage system errors,
     *                          network issues, or insufficient permissions
     *
     * @see #downloadFile(String)
     * @see #deleteFile(String)
     */
    String uploadFile(MultipartFile file, String folder) throws IOException;

    /**
     * Generates a presigned URL for temporary file access.
     *
     * <p>Creates a time-limited URL that allows direct access to the stored file
     * without requiring authentication. This is useful for serving files directly
     * to clients (browsers, mobile apps) without proxying through the application.</p>
     *
     * <h3>URL Characteristics:</h3>
     * <ul>
     *     <li><strong>Time-Limited:</strong> URL expires after configured duration (default: 1 hour)</li>
     *     <li><strong>Signed:</strong> Contains cryptographic signature for verification</li>
     *     <li><strong>One-Time Use:</strong> URLs should be generated per-request for security</li>
     *     <li><strong>Direct Access:</strong> Clients can download directly from storage</li>
     * </ul>
     *
     * @param objectName The complete object name (path) as returned by uploadFile().
     *                   Must not be null or blank. Example: "profile-pictures/uuid.jpg"
     *
     * @return A presigned URL string that provides temporary direct access to the file.
     *         The URL includes authentication parameters and expires after the
     *         configured time period (typically 1 hour).
     *
     * @throws IllegalArgumentException if objectName is null or blank
     * @throws RuntimeException if URL generation fails due to cryptographic errors,
     *                          invalid object name, or storage system errors
     *
     * @see #downloadFile(String)
     */
    String getPresignedUrl(String objectName);

    /**
     * Deletes a file from the object storage system.
     *
     * <p>Permanently removes the specified file from storage. This operation is
     * irreversible and should be used with caution. If the object does not exist,
     * the operation completes successfully without error (idempotent behavior).</p>
     *
     * <h3>Deletion Behavior:</h3>
     * <ul>
     *     <li><strong>Permanent:</strong> Deleted files cannot be recovered</li>
     *     <li><strong>Immediate:</strong> File becomes inaccessible immediately</li>
     *     <li><strong>Idempotent:</strong> Deleting non-existent objects succeeds</li>
     *     <li><strong>No Cascading:</strong> Only the specified object is deleted</li>
     * </ul>
     *
     * @param objectName The complete object name (path) of the file to delete.
     *                   If null or blank, the operation is a no-op (safe to call).
     *                   Example: "profile-pictures/uuid.jpg"
     *
     * @throws RuntimeException if deletion fails due to storage system errors,
     *                          network issues, or insufficient permissions. Does
     *                          NOT throw an exception if the object doesn't exist.
     *
     * @see #uploadFile(MultipartFile, String)
     */
    void deleteFile(String objectName);

    /**
     * Downloads a file from the object storage system as an InputStream.
     *
     * <p>Retrieves the binary content of the specified file as a stream, allowing
     * efficient processing of large files without loading the entire content into
     * memory. The caller is responsible for closing the stream after use.</p>
     *
     * <h3>Stream Characteristics:</h3>
     * <ul>
     *     <li><strong>Lazy Loading:</strong> Data is fetched as it's read from the stream</li>
     *     <li><strong>Memory Efficient:</strong> Suitable for files of any size</li>
     *     <li><strong>Single Use:</strong> Stream cannot be reset or rewound</li>
     *     <li><strong>Must Close:</strong> Caller must close stream to release resources</li>
     * </ul>
     *
     * <h3>Performance Considerations:</h3>
     * <ul>
     *     <li>Streaming is more efficient than loading entire files into memory</li>
     *     <li>Consider caching frequently accessed files</li>
     *     <li>Use presigned URLs for direct browser access when possible</li>
     *     <li>Implement HTTP range requests for large file downloads</li>
     * </ul>
     *
     * <h3>Error Scenarios:</h3>
     * <ul>
     *     <li>File not found: Throws RuntimeException</li>
     *     <li>Network timeout: Throws RuntimeException</li>
     *     <li>Insufficient permissions: Throws RuntimeException</li>
     *     <li>Stream read errors: IOException thrown during read operations</li>
     * </ul>
     *
     * @param objectName The complete object name (path) of the file to download.
     *                   Must not be null or blank. The file must exist in storage.
     *                   Example: "profile-pictures/uuid.jpg"
     *
     * @return An InputStream providing access to the file's binary content.
     *         The stream must be closed by the caller after use. The stream
     *         reads data directly from the storage system as needed.
     *
     * @throws IllegalArgumentException if objectName is null or blank
     * @throws RuntimeException if the file doesn't exist, network errors occur,
     *                          insufficient permissions, or storage system errors
     *
     * @see #uploadFile(MultipartFile, String)
     * @see #getPresignedUrl(String)
     */
    InputStream downloadFile(String objectName);
}
