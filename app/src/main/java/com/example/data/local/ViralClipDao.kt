package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ViralClipDao {
    @Query("SELECT * FROM video_projects ORDER BY createdAtMs DESC")
    fun observeAllProjects(): Flow<List<VideoProjectEntity>>

    @Query("SELECT * FROM video_projects WHERE id = :projectId LIMIT 1")
    fun observeProject(projectId: Long): Flow<VideoProjectEntity?>

    @Query("SELECT * FROM video_projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: Long): VideoProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: VideoProjectEntity): Long

    @Update
    suspend fun updateProject(project: VideoProjectEntity)

    @Query("DELETE FROM video_projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: Long)

    @Query("SELECT * FROM generated_clips WHERE projectId = :projectId ORDER BY viralScore DESC, clipIndex ASC")
    fun observeClipsForProject(projectId: Long): Flow<List<GeneratedClipEntity>>

    @Query("SELECT * FROM generated_clips WHERE projectId = :projectId ORDER BY viralScore DESC, clipIndex ASC")
    suspend fun getClipsForProject(projectId: Long): List<GeneratedClipEntity>

    @Query("SELECT * FROM generated_clips ORDER BY updatedAtMs DESC LIMIT 20")
    fun observeRecentClips(): Flow<List<GeneratedClipEntity>>

    @Query("SELECT * FROM generated_clips WHERE id = :clipId LIMIT 1")
    fun observeClip(clipId: Long): Flow<GeneratedClipEntity?>

    @Query("SELECT * FROM generated_clips WHERE id = :clipId LIMIT 1")
    suspend fun getClipById(clipId: Long): GeneratedClipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClips(clips: List<GeneratedClipEntity>)

    @Update
    suspend fun updateClip(clip: GeneratedClipEntity)

    @Query("DELETE FROM generated_clips WHERE id = :clipId")
    suspend fun deleteClip(clipId: Long)

    @Query("SELECT * FROM export_history ORDER BY exportedAtMs DESC")
    fun observeExportHistory(): Flow<List<ExportHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExportHistory(entry: ExportHistoryEntity): Long

    @Query("DELETE FROM export_history WHERE id = :exportId")
    suspend fun deleteExportHistory(exportId: Long)

    @Query("DELETE FROM export_history")
    suspend fun clearAllExports()
}
