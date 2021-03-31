package org.codefreak.codefreak.service.file

import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.jvm.Throws
import org.springframework.util.DigestUtils
import org.springframework.util.StreamUtils

interface FileService {
  fun readCollectionTar(collectionId: UUID): InputStream
  fun writeCollectionTar(collectionId: UUID): OutputStream
  fun collectionExists(collectionId: UUID): Boolean
  fun deleteCollection(collectionId: UUID)

  fun copyCollection(oldId: UUID, newId: UUID) {
    readCollectionTar(oldId).use {
      writeCollectionTar(newId).use { out -> StreamUtils.copy(it, out) }
    }
  }

  fun getCollectionMd5Digest(collectionId: UUID): ByteArray {
    return readCollectionTar(collectionId).use { DigestUtils.md5Digest(it) }
  }

  /**
   * Walk over every file or directory in the given collection.
   * Returns a sequence with ALL files from this collection.
   */
  fun walkFileTree(collectionId: UUID): Sequence<FileMetaData>

  /**
   * List all files and directories that are direct descendants of path
   * Throws an IllegalArgumentException if path does not exist
   */
  @Throws(IllegalArgumentException::class)
  fun listFiles(collectionId: UUID, path: String): Sequence<FileMetaData>

  /**
   * Create empty files in places specified by path.
   * Throws an IllegalArgumentException if the parent directory does not exist or existing path is a directory.
   */
  @Throws(IllegalArgumentException::class)
  fun createFiles(collectionId: UUID, paths: Set<String>)

  /**
   * Create directories places specified by path.
   * This will create all parent directories in case the do not exist.
   * Ignores silently if a directory already exists.
   */
  fun createDirectories(collectionId: UUID, paths: Set<String>)

  /**
   * Denotes if path exists and is a file
   */
  fun containsFile(collectionId: UUID, path: String): Boolean

  /**
   * Denotes if path exists and is a directory
   */
  fun containsDirectory(collectionId: UUID, path: String): Boolean

  /**
   * Delete files specified by path.
   * Directories are always deleted recursively!
   * Throws an exception if one of the paths did not exist.
   */
  @Throws(IllegalArgumentException::class)
  fun deleteFiles(collectionId: UUID, paths: Set<String>)

  /**
   * Move one or multiple source files to target.
   * The behaviour depends on the type of target.
   * - Always:
   *   - All sources must be existing files or directories
   * - If target does not exist (renaming a single file/directory):
   *   - Only a single source is allowed
   * - If target does exist (moving multiple files/dir to a folder):
   *   - Target must be an existing directory
   *   - One or multiple files are allowed
   *   - Target must not be one of the descendants of source (moving something to itself)
   *   - Basename of each source must not be existent in target
   *   - Moving directories preserves the directory structure
   *
   * If one of the conditions above does not match an IllegalArgumentException is thrown.
   */
  @Throws(IllegalArgumentException::class)
  fun moveFile(collectionId: UUID, sources: Set<String>, target: String)

  /**
   * Write to file specified by path. Will create the file if it does not exist and truncate existing content.
   * The output stream must be closed properly after writing!
   * Throws an IllegalArgumentException if path does not exist or is a directory.
   */
  @Throws(IllegalArgumentException::class)
  fun writeFile(collectionId: UUID, path: String): OutputStream

  /**
   * Read content from file.
   * The output stream must be closed properly after reading!
   * Throws an IllegalArgumentException if path does not exist or is a directory.
   */
  @Throws(IllegalArgumentException::class)
  fun readFile(collectionId: UUID, path: String): InputStream
}
