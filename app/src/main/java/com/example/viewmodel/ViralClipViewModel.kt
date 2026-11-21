package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiClipRequest
import com.example.data.ai.SamplePlaceholderAiRepository
import com.example.data.ai.ViralClipAiRepository
import com.example.data.local.ExportHistoryEntity
import com.example.data.local.GeneratedClipEntity
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.VideoProjectEntity
import com.example.data.local.ViralClipDatabase
import com.example.data.model.BackgroundMusic
import com.example.data.model.CaptionLanguage
import com.example.data.model.CaptionStyle
import com.example.data.model.ExportPlatform
import com.example.data.model.ExportProgressState
import com.example.data.model.ImportSourceType
import com.example.data.model.LocalVideoInfo
import com.example.data.model.MomentType
import com.example.data.model.ProcessingProgress
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SubtitleCodec
import com.example.data.model.SubtitleCue
import com.example.data.model.UserProfileSettings
import com.example.data.model.YouTubeMetadata
import com.example.data.network.YouTubeMetadataService
import com.example.data.video.LocalVideoInspector
import com.example.data.video.VideoExportEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ImportFormState(
    val sourceType: ImportSourceType = ImportSourceType.YOUTUBE_URL,
    val youtubeUrlInput: String = "",
    val isFetchingMetadata: Boolean = false,
    val fetchedYouTube: YouTubeMetadata? = null,
    val selectedLocalVideo: LocalVideoInfo? = null,
    val requestedClipCount: Int = 5,
    val targetClipDurationSec: Int = 30,
    val selectedMoments: Set<MomentType> = setOf(
        MomentType.VIRAL_HOOK,
        MomentType.FUNNY,
        MomentType.MOTIVATIONAL
    ),
    val captionLanguage: CaptionLanguage = CaptionLanguage.HINGLISH,
    val targetPlatform: ExportPlatform = ExportPlatform.INSTAGRAM_REELS,
    val errorMessage: String? = null
)

class ViralClipViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ViralClipDatabase.getInstance(application).viralClipDao()
    private val prefsRepo = UserPreferencesRepository(application)
    private val youtubeService = YouTubeMetadataService()
    private val localVideoInspector = LocalVideoInspector(application)
    private val videoExportEngine = VideoExportEngine(application)
    private val aiRepository: ViralClipAiRepository = SamplePlaceholderAiRepository()

    val isSampleAiMode: Boolean = aiRepository.isSampleMode
    val aiModeNotice: String = aiRepository.modeNotice

    val userSettings: StateFlow<UserProfileSettings> = prefsRepo.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfileSettings())

    val projects: StateFlow<List<VideoProjectEntity>> = dao.observeAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentClips: StateFlow<List<GeneratedClipEntity>> = dao.observeRecentClips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val exportHistory: StateFlow<List<ExportHistoryEntity>> = dao.observeExportHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId.asStateFlow()

    @Suppress("OPT_IN_USAGE")
    val selectedProject: StateFlow<VideoProjectEntity?> = _selectedProjectId
        .flatMapLatest { id -> if (id == null) flowOf(null) else dao.observeProject(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @Suppress("OPT_IN_USAGE")
    val selectedProjectClips: StateFlow<List<GeneratedClipEntity>> = _selectedProjectId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else dao.observeClipsForProject(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editingClipId = MutableStateFlow<Long?>(null)

    @Suppress("OPT_IN_USAGE")
    val editingClip: StateFlow<GeneratedClipEntity?> = _editingClipId
        .flatMapLatest { id -> if (id == null) flowOf(null) else dao.observeClip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _importState = MutableStateFlow(ImportFormState())
    val importState: StateFlow<ImportFormState> = _importState.asStateFlow()

    private val _processingProgress = MutableStateFlow(ProcessingProgress())
    val processingProgress: StateFlow<ProcessingProgress> = _processingProgress.asStateFlow()

    private val _exportProgress = MutableStateFlow(ExportProgressState())
    val exportProgress: StateFlow<ExportProgressState> = _exportProgress.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun selectSourceType(type: ImportSourceType) {
        _importState.update { it.copy(sourceType = type, errorMessage = null) }
    }

    fun updateYouTubeUrl(url: String) {
        _importState.update { it.copy(youtubeUrlInput = url, errorMessage = null) }
    }

    fun updateRequestedClipCount(count: Int) {
        val settings = userSettings.value
        if (count > settings.plan.maxClipsPerGeneration) {
            _snackbarMessage.value =
                "${settings.plan.title} supports up to ${settings.plan.maxClipsPerGeneration} clips per run. Upgrade in Account -> Plans for $count clips."
            return
        }
        _importState.update { it.copy(requestedClipCount = count, errorMessage = null) }
    }

    fun updateTargetDurationSec(seconds: Int) {
        _importState.update { it.copy(targetClipDurationSec = seconds.coerceIn(10, 90)) }
    }

    fun toggleMomentType(type: MomentType) {
        _importState.update { state ->
            val updated = if (type in state.selectedMoments) {
                if (state.selectedMoments.size > 1) state.selectedMoments - type else state.selectedMoments
            } else {
                state.selectedMoments + type
            }
            state.copy(selectedMoments = updated)
        }
    }

    fun updateImportCaptionLanguage(language: CaptionLanguage) {
        _importState.update { it.copy(captionLanguage = language) }
    }

    fun updateImportPlatform(platform: ExportPlatform) {
        _importState.update { it.copy(targetPlatform = platform) }
    }

    fun fetchYouTubeMetadata() {
        val rawUrl = _importState.value.youtubeUrlInput.trim()
        if (rawUrl.isBlank()) {
            _importState.update { it.copy(errorMessage = "Paste a YouTube video link first.") }
            return
        }
        viewModelScope.launch {
            _importState.update { it.copy(isFetchingMetadata = true, errorMessage = null) }
            val result = youtubeService.fetchMetadata(rawUrl)
            result.fold(
                onSuccess = { meta ->
                    _importState.update {
                        it.copy(
                            isFetchingMetadata = false,
                            fetchedYouTube = meta,
                            errorMessage = null
                        )
                    }
                    _snackbarMessage.value = "Fetched: ${meta.title}"
                },
                onFailure = { error ->
                    _importState.update {
                        it.copy(
                            isFetchingMetadata = false,
                            errorMessage = error.localizedMessage ?: "Failed to fetch YouTube metadata."
                        )
                    }
                }
            )
        }
    }

    fun onLocalVideoPicked(uri: Uri) {
        viewModelScope.launch {
            _importState.update { it.copy(isFetchingMetadata = true, errorMessage = null) }
            val result = localVideoInspector.inspectVideo(uri)
            result.fold(
                onSuccess = { info ->
                    _importState.update {
                        it.copy(
                            isFetchingMetadata = false,
                            selectedLocalVideo = info,
                            errorMessage = null
                        )
                    }
                    _snackbarMessage.value = "Video loaded: ${info.displayName}"
                },
                onFailure = { error ->
                    _importState.update {
                        it.copy(
                            isFetchingMetadata = false,
                            errorMessage = error.localizedMessage ?: "Unable to read video file."
                        )
                    }
                }
            )
        }
    }

    fun attachLocalVideoToProject(projectId: Long, uri: Uri) {
        viewModelScope.launch {
            val project = dao.getProjectById(projectId) ?: return@launch
            val result = localVideoInspector.inspectVideo(uri)
            result.fold(
                onSuccess = { info ->
                    dao.updateProject(
                        project.copy(
                            localVideoUri = info.uriString,
                            durationMs = info.durationMs,
                            thumbnailUrlOrPath = project.thumbnailUrlOrPath ?: info.thumbnailPath
                        )
                    )
                    _snackbarMessage.value = "Attached source video (${info.displayName}) for 9:16 MP4 clipping!"
                },
                onFailure = { error ->
                    _snackbarMessage.value = error.localizedMessage ?: "Could not attach video file."
                }
            )
        }
    }

    fun generateViralClips(onProjectCreated: (Long) -> Unit) {
        val state = _importState.value
        val settings = userSettings.value

        val remainingQuota = (settings.plan.monthlyClipLimit - settings.clipsGeneratedThisMonth)
            .coerceAtLeast(0)
        if (remainingQuota < state.requestedClipCount) {
            _importState.update {
                it.copy(
                    errorMessage = "Monthly quota reached ($remainingQuota clips left on ${settings.plan.title}). Upgrade in Account -> Plans or lower clip count."
                )
            }
            return
        }

        val yt = state.fetchedYouTube
        val local = state.selectedLocalVideo

        if (state.sourceType == ImportSourceType.YOUTUBE_URL && yt == null && state.youtubeUrlInput.isBlank()) {
            _importState.update { it.copy(errorMessage = "Paste a YouTube link or switch to Device Video.") }
            return
        }
        if (state.sourceType == ImportSourceType.LOCAL_VIDEO && local == null) {
            _importState.update { it.copy(errorMessage = "Pick a video from your device first.") }
            return
        }

        viewModelScope.launch {
            try {
                // Auto-fetch YouTube metadata if user pasted URL and tapped Generate directly
                var resolvedYt = yt
                if (state.sourceType == ImportSourceType.YOUTUBE_URL && resolvedYt == null) {
                    _processingProgress.value = ProcessingProgress(
                        isActive = true,
                        currentStep = 1,
                        totalSteps = 4,
                        stageTitle = "Fetching YouTube Metadata",
                        stageDetail = "Loading title, creator, and thumbnail via oEmbed…",
                        fraction = 0.12f
                    )
                    val fetched = youtubeService.fetchMetadata(state.youtubeUrlInput)
                    resolvedYt = fetched.getOrElse { err ->
                        _processingProgress.value = ProcessingProgress()
                        _importState.update {
                            it.copy(errorMessage = err.localizedMessage ?: "Invalid YouTube URL.")
                        }
                        return@launch
                    }
                    _importState.update { it.copy(fetchedYouTube = resolvedYt) }
                }

                val title = when (state.sourceType) {
                    ImportSourceType.YOUTUBE_URL -> resolvedYt?.title ?: "YouTube Studio Project"
                    ImportSourceType.LOCAL_VIDEO -> local?.displayName?.substringBeforeLast('.') ?: "Local Video Project"
                }
                val author = when (state.sourceType) {
                    ImportSourceType.YOUTUBE_URL -> resolvedYt?.authorName ?: "YouTube Creator"
                    ImportSourceType.LOCAL_VIDEO -> settings.displayName
                }
                val durationMs = when {
                    local != null -> local.durationMs
                    resolvedYt != null -> resolvedYt.estimatedDurationSec * 1000L
                    else -> 240_000L
                }
                val thumb = when (state.sourceType) {
                    ImportSourceType.YOUTUBE_URL -> resolvedYt?.thumbnailUrl ?: local?.thumbnailPath
                    ImportSourceType.LOCAL_VIDEO -> local?.thumbnailPath
                }

                val drafts = aiRepository.analyzeAndGenerateClips(
                    request = AiClipRequest(
                        videoTitle = title,
                        channelOrAuthor = author,
                        totalDurationMs = durationMs,
                        requestedClipCount = state.requestedClipCount,
                        targetClipDurationSec = state.targetClipDurationSec,
                        preferredMoments = state.selectedMoments,
                        primaryLanguage = state.captionLanguage
                    ),
                    onStageUpdate = { step, stageTitle, detail, fraction ->
                        _processingProgress.value = ProcessingProgress(
                            isActive = true,
                            currentStep = step,
                            totalSteps = 4,
                            stageTitle = stageTitle,
                            stageDetail = detail,
                            fraction = fraction
                        )
                    }
                )

                val projectId = dao.insertProject(
                    VideoProjectEntity(
                        title = title,
                        channelOrAuthor = author,
                        sourceType = state.sourceType.name,
                        sourceUrl = resolvedYt?.originalUrl ?: "",
                        localVideoUri = local?.uriString,
                        thumbnailUrlOrPath = thumb,
                        durationMs = durationMs,
                        requestedClipCount = drafts.size,
                        targetClipDurationSec = state.targetClipDurationSec,
                        captionLanguage = state.captionLanguage.name,
                        targetPlatform = state.targetPlatform.name,
                        isSampleAi = aiRepository.isSampleMode
                    )
                )

                val clipEntities = drafts.map { draft ->
                    GeneratedClipEntity(
                        projectId = projectId,
                        clipIndex = draft.clipIndex,
                        title = draft.title,
                        hookLine = draft.hookLine,
                        momentType = draft.momentType.name,
                        viralScore = draft.viralScore,
                        viralReason = draft.viralReason,
                        startMs = draft.startMs,
                        endMs = draft.endMs,
                        captionLanguage = state.captionLanguage.name,
                        captionStyle = CaptionStyle.NEON_POP.name,
                        emojiCaptionsEnabled = settings.defaultEmojiCaptions,
                        animatedCaptionsEnabled = true,
                        smartCropBiasX = 0.5f,
                        removeSilenceEnabled = settings.autoRemoveSilence,
                        backgroundMusic = BackgroundMusic.NONE.name,
                        musicVolume = 0.25f,
                        introEnabled = true,
                        introText = draft.introText,
                        outroEnabled = true,
                        outroText = draft.outroText,
                        exportPlatform = state.targetPlatform.name,
                        subtitlesJson = SubtitleCodec.encode(draft.subtitles),
                        isSampleAi = draft.isSampleOutput
                    )
                }

                dao.insertClips(clipEntities)
                prefsRepo.incrementClipsGenerated(clipEntities.size)

                _selectedProjectId.value = projectId
                _processingProgress.value = ProcessingProgress()
                _snackbarMessage.value = "Generated ${clipEntities.size} viral clips ready to edit & export!"
                onProjectCreated(projectId)
            } catch (e: CancellationException) {
                _processingProgress.value = ProcessingProgress()
                throw e
            } catch (e: Exception) {
                _processingProgress.value = ProcessingProgress()
                _importState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Clip generation failed.")
                }
            }
        }
    }

    fun selectProject(projectId: Long?) {
        _selectedProjectId.value = projectId
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            dao.deleteProject(projectId)
            if (_selectedProjectId.value == projectId) {
                _selectedProjectId.value = null
            }
            _snackbarMessage.value = "Project deleted."
        }
    }

    fun openClipEditor(projectId: Long, clipId: Long) {
        _selectedProjectId.value = projectId
        _editingClipId.value = clipId
    }

    fun updateClip(transform: (GeneratedClipEntity) -> GeneratedClipEntity) {
        val current = editingClip.value ?: return
        val updated = transform(current).copy(updatedAtMs = System.currentTimeMillis())
        viewModelScope.launch {
            dao.updateClip(updated)
        }
    }

    fun updateSubtitleCueText(cueId: Int, language: CaptionLanguage, newText: String) {
        val current = editingClip.value ?: return
        val cues = SubtitleCodec.decode(current.subtitlesJson).map { cue ->
            if (cue.id == cueId) {
                when (language) {
                    CaptionLanguage.ENGLISH -> cue.copy(textEnglish = newText)
                    CaptionLanguage.HINDI -> cue.copy(textHindi = newText)
                    CaptionLanguage.HINGLISH -> cue.copy(textHinglish = newText)
                }
            } else cue
        }
        updateClip { it.copy(subtitlesJson = SubtitleCodec.encode(cues)) }
    }

    fun exportSingleClip(clip: GeneratedClipEntity, overridePlatform: ExportPlatform? = null) {
        viewModelScope.launch {
            val project = dao.getProjectById(clip.projectId) ?: return@launch
            val clipToExport = if (overridePlatform != null) {
                clip.copy(exportPlatform = overridePlatform.name).also { dao.updateClip(it) }
            } else clip

            val fullHd = userSettings.value.fullHdExport
            _exportProgress.value = ExportProgressState(
                isExporting = true,
                clipTitle = clipToExport.title,
                currentClipIndex = 1,
                totalClips = 1,
                progressPercent = 2,
                statusText = "Encoding 9:16 ${if (fullHd) "1080×1920" else "720×1280"} MP4…"
            )

            val result = videoExportEngine.exportClipToMp4(
                project = project,
                clip = clipToExport,
                fullHd = fullHd,
                onProgress = { pct ->
                    _exportProgress.update {
                        it.copy(
                            progressPercent = pct,
                            statusText = "Rendering video & animated captions ($pct%)…"
                        )
                    }
                }
            )

            result.fold(
                onSuccess = { exported ->
                    dao.insertExportHistory(
                        ExportHistoryEntity(
                            projectId = project.id,
                            clipId = clipToExport.id,
                            clipTitle = clipToExport.title,
                            projectTitle = project.title,
                            platform = clipToExport.exportPlatform,
                            resolution = exported.resolution,
                            durationMs = exported.durationMs,
                            fileSizeBytes = exported.fileSizeBytes,
                            filePath = exported.filePath,
                            mediaStoreUri = exported.mediaStoreUri,
                            thumbnailUrlOrPath = project.thumbnailUrlOrPath
                        )
                    )
                    _exportProgress.value = ExportProgressState(
                        isExporting = false,
                        clipTitle = clipToExport.title,
                        progressPercent = 100,
                        statusText = "MP4 saved to Movies/ViralClipAI!",
                        lastExportedUri = exported.mediaStoreUri,
                        lastExportedPath = exported.filePath
                    )
                    _snackbarMessage.value = "Exported ${exported.resolution} MP4 successfully!"
                },
                onFailure = { err ->
                    _exportProgress.value = ExportProgressState(
                        isExporting = false,
                        errorMessage = err.localizedMessage ?: "Export failed."
                    )
                    _snackbarMessage.value = err.localizedMessage ?: "Export failed."
                }
            )
        }
    }

    fun exportAllClipsForProject(projectId: Long) {
        viewModelScope.launch {
            val project = dao.getProjectById(projectId) ?: return@launch
            val clips = dao.getClipsForProject(projectId)
            if (clips.isEmpty()) return@launch

            val fullHd = userSettings.value.fullHdExport
            var successCount = 0
            var lastPath: String? = null
            var lastUri: String? = null

            clips.forEachIndexed { idx, clip ->
                _exportProgress.value = ExportProgressState(
                    isExporting = true,
                    clipTitle = clip.title,
                    currentClipIndex = idx + 1,
                    totalClips = clips.size,
                    progressPercent = 0,
                    statusText = "Batch exporting clip ${idx + 1} of ${clips.size}…"
                )
                val res = videoExportEngine.exportClipToMp4(
                    project = project,
                    clip = clip,
                    fullHd = fullHd,
                    onProgress = { pct ->
                        _exportProgress.update {
                            it.copy(
                                progressPercent = pct,
                                statusText = "Clip ${idx + 1}/${clips.size} • Rendering ($pct%)…"
                            )
                        }
                    }
                )
                res.onSuccess { exported ->
                    successCount++
                    lastPath = exported.filePath
                    lastUri = exported.mediaStoreUri
                    dao.insertExportHistory(
                        ExportHistoryEntity(
                            projectId = project.id,
                            clipId = clip.id,
                            clipTitle = clip.title,
                            projectTitle = project.title,
                            platform = clip.exportPlatform,
                            resolution = exported.resolution,
                            durationMs = exported.durationMs,
                            fileSizeBytes = exported.fileSizeBytes,
                            filePath = exported.filePath,
                            mediaStoreUri = exported.mediaStoreUri,
                            thumbnailUrlOrPath = project.thumbnailUrlOrPath
                        )
                    )
                }
            }

            _exportProgress.value = ExportProgressState(
                isExporting = false,
                clipTitle = "${successCount}/${clips.size} clips exported",
                currentClipIndex = clips.size,
                totalClips = clips.size,
                progressPercent = 100,
                statusText = "Batch export finished ($successCount MP4 files saved)",
                lastExportedPath = lastPath,
                lastExportedUri = lastUri
            )
            _snackbarMessage.value = "Batch exported $successCount MP4 clips to Exports & Gallery!"
        }
    }

    fun dismissExportBanner() {
        _exportProgress.value = ExportProgressState()
    }

    fun deleteExportItem(item: ExportHistoryEntity) {
        viewModelScope.launch {
            runCatching { File(item.filePath).delete() }
            dao.deleteExportHistory(item.id)
            _snackbarMessage.value = "Removed from export history."
        }
    }

    fun clearAllExports() {
        viewModelScope.launch {
            exportHistory.value.forEach { runCatching { File(it.filePath).delete() } }
            dao.clearAllExports()
            _snackbarMessage.value = "Cleared all export history."
        }
    }

    // Account, Plans & Settings mutations
    fun updateUserProfile(displayName: String, handle: String) {
        viewModelScope.launch {
            prefsRepo.updateProfile(displayName, handle)
            _snackbarMessage.value = "Profile updated."
        }
    }

    fun switchSubscriptionPlan(plan: SubscriptionPlan) {
        viewModelScope.launch {
            prefsRepo.updatePlan(plan)
            if (_importState.value.requestedClipCount > plan.maxClipsPerGeneration) {
                _importState.update { it.copy(requestedClipCount = plan.maxClipsPerGeneration) }
            }
            _snackbarMessage.value = "Switched to ${plan.title} plan!"
        }
    }

    fun resetMonthlyUsage() {
        viewModelScope.launch {
            prefsRepo.resetMonthlyUsage()
            _snackbarMessage.value = "Monthly clip counter reset."
        }
    }

    fun updateDefaultLanguage(language: CaptionLanguage) {
        viewModelScope.launch {
            prefsRepo.updateDefaultLanguage(language)
            _importState.update { it.copy(captionLanguage = language) }
        }
    }

    fun updateDefaultPlatform(platform: ExportPlatform) {
        viewModelScope.launch {
            prefsRepo.updateDefaultPlatform(platform)
            _importState.update { it.copy(targetPlatform = platform) }
        }
    }

    fun updateFullHdExport(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.updateFullHdExport(enabled) }
    }

    fun updateAutoRemoveSilence(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.updateAutoRemoveSilence(enabled) }
    }

    fun updateDefaultEmojiCaptions(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.updateDefaultEmojiCaptions(enabled) }
    }
}
