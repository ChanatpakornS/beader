package com.beader.core.data.repository

import com.beader.core.common.result.DataResult
import com.beader.core.database.dao.ImportedPdfDao
import com.beader.core.database.entity.ImportedPdfEntity
import com.beader.core.domain.model.PdfLibraryItem
import com.beader.core.domain.repository.PdfLibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfLibraryRepositoryImpl
    @Inject
    constructor(
        private val importedPdfDao: ImportedPdfDao,
    ) : PdfLibraryRepository {
        override fun observeLibrary(): Flow<DataResult<List<PdfLibraryItem>>> =
            importedPdfDao
                .observeAll()
                .map<List<ImportedPdfEntity>, DataResult<List<PdfLibraryItem>>> { entities ->
                    DataResult.Success(entities.map { it.toDomain() })
                }.catch { throwable ->
                    Timber.e(throwable, "Failed to observe PDF library")
                    emit(DataResult.Error(throwable))
                }

        override suspend fun saveImportedDocument(
            uri: String,
            fileName: String,
            pageCount: Int,
            thumbnailBytes: ByteArray,
        ): DataResult<PdfLibraryItem> =
            runCatching {
                val importedAt = System.currentTimeMillis()
                val entity =
                    ImportedPdfEntity(
                        uri = uri,
                        fileName = fileName,
                        pageCount = pageCount,
                        thumbnailBytes = thumbnailBytes,
                        importedAtEpochMillis = importedAt,
                    )
                val id = importedPdfDao.insert(entity)
                entity.copy(id = id).toDomain()
            }.fold(
                onSuccess = { DataResult.Success(it) },
                onFailure = { throwable ->
                    Timber.e(throwable, "Failed to save imported PDF")
                    DataResult.Error(throwable)
                },
            )

        override suspend fun deleteDocument(id: Long) {
            importedPdfDao.deleteById(id)
        }
    }

private fun ImportedPdfEntity.toDomain() =
    PdfLibraryItem(
        id = id,
        uri = uri,
        fileName = fileName,
        pageCount = pageCount,
        thumbnailBytes = thumbnailBytes,
        importedAtEpochMillis = importedAtEpochMillis,
    )
