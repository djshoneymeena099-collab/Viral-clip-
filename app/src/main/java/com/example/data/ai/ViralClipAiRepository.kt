package com.example.data.ai

import com.example.data.model.CaptionLanguage
import com.example.data.model.MomentType
import com.example.data.model.SubtitleCue
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

data class AiClipRequest(
    val videoTitle: String,
    val channelOrAuthor: String,
    val totalDurationMs: Long,
    val requestedClipCount: Int,
    val targetClipDurationSec: Int,
    val preferredMoments: Set<MomentType>,
    val primaryLanguage: CaptionLanguage
)

data class AiGeneratedClipDraft(
    val clipIndex: Int,
    val title: String,
    val hookLine: String,
    val momentType: MomentType,
    val viralScore: Int,
    val viralReason: String,
    val startMs: Long,
    val endMs: Long,
    val introText: String,
    val outroText: String,
    val subtitles: List<SubtitleCue>,
    val isSampleOutput: Boolean
)

/**
 * Single AI repository abstraction for all viral highlight detection and
 * multilingual caption generation. Currently backed by [SamplePlaceholderAiRepository]
 * because Firebase AI Logic was not enabled during setup.
 */
interface ViralClipAiRepository {
    val isSampleMode: Boolean
    val modeNotice: String

    suspend fun analyzeAndGenerateClips(
        request: AiClipRequest,
        onStageUpdate: (step: Int, title: String, detail: String, fraction: Float) -> Unit
    ): List<AiGeneratedClipDraft>
}

class SamplePlaceholderAiRepository : ViralClipAiRepository {

    override val isSampleMode: Boolean = true
    override val modeNotice: String =
        "Sample AI Preview — Live Gemini highlight detection & speech-to-text require Firebase AI Logic."

    override suspend fun analyzeAndGenerateClips(
        request: AiClipRequest,
        onStageUpdate: (step: Int, title: String, detail: String, fraction: Float) -> Unit
    ): List<AiGeneratedClipDraft> {
        onStageUpdate(1, "Scanning Video Timeline", "Reading scene cuts, pacing & audio peaks…", 0.22f)
        delay(550)

        onStageUpdate(
            2,
            "Detecting Viral Moments",
            "Scoring funny, emotional, motivational & hook segments…",
            0.52f
        )
        delay(650)

        onStageUpdate(
            3,
            "Syncing Multilingual Captions",
            "Building English, Hindi & Hinglish animated subtitle cues…",
            0.78f
        )
        delay(550)

        onStageUpdate(
            4,
            "Framing 9:16 Vertical Reels",
            "Preparing ${request.requestedClipCount} ready-to-edit viral shorts…",
            0.96f
        )
        delay(350)

        val safeTotalMs = max(request.totalDurationMs, 25_000L)
        val desiredClipMs = (request.targetClipDurationSec.coerceIn(10, 90)) * 1000L
        val actualClipMs = min(desiredClipMs, max(8_000L, safeTotalMs))
        val count = request.requestedClipCount.coerceIn(1, 20)
        val activeMoments = request.preferredMoments.ifEmpty { MomentType.entries.toSet() }.toList()

        val topicKeywords = extractTopicPhrase(request.videoTitle)
        val maxStartMs = max(0L, safeTotalMs - actualClipMs)

        return List(count) { idx ->
            val moment = activeMoments[idx % activeMoments.size]
            val ratio = if (count <= 1) 0.15f else (idx.toFloat() / (count - 1).toFloat())
            val startMs = ((maxStartMs * ratio).toLong() / 1000L) * 1000L
            val endMs = min(safeTotalMs, startMs + actualClipMs)
            val clipDurationMs = max(6_000L, endMs - startMs)
            val viralScore = (98 - (idx * 2) % 19).coerceIn(76, 99)

            val template = buildSampleTemplate(
                index = idx,
                moment = moment,
                topic = topicKeywords,
                author = request.channelOrAuthor
            )

            val cues = buildSynchronizedCues(
                durationMs = clipDurationMs,
                moment = moment,
                topic = topicKeywords,
                hookLine = template.hookLine
            )

            AiGeneratedClipDraft(
                clipIndex = idx + 1,
                title = template.title,
                hookLine = template.hookLine,
                momentType = moment,
                viralScore = viralScore,
                viralReason = template.reason,
                startMs = startMs,
                endMs = endMs,
                introText = template.introText,
                outroText = template.outroText,
                subtitles = cues,
                isSampleOutput = true
            )
        }
    }

    private fun extractTopicPhrase(rawTitle: String): String {
        val cleaned = rawTitle
            .replace(Regex("""\[.*?]|\(.*?\)"""), "")
            .replace(Regex("""[^A-Za-z0-9\s'-]"""), " ")
            .trim()
            .split(Regex("""\s+"""))
            .filter { it.length > 2 }
            .take(4)
            .joinToString(" ")
        return cleaned.ifBlank { "this creator breakdown" }
    }

    private data class ClipTemplate(
        val title: String,
        val hookLine: String,
        val reason: String,
        val introText: String,
        val outroText: String
    )

    private fun buildSampleTemplate(
        index: Int,
        moment: MomentType,
        topic: String,
        author: String
    ): ClipTemplate {
        val n = index + 1
        return when (moment) {
            MomentType.VIRAL_HOOK -> ClipTemplate(
                title = "Clip #$n • The Unspoken Secret of $topic",
                hookLine = "99% of people completely miss this part!",
                reason = "Strong 2-second pattern interrupt with high retention potential (Sample AI)",
                introText = "WAIT FOR THE TWIST 🔥",
                outroText = "Save this Reel & follow $author 🚀"
            )
            MomentType.FUNNY -> ClipTemplate(
                title = "Clip #$n • Unexpected Reaction Moment",
                hookLine = "Nobody expected what happened right here!",
                reason = "High laugh-spike energy and shareable comedic timing (Sample AI)",
                introText = "YOU WON'T STOP LAUGHING 😂",
                outroText = "Tag a friend who needs to see this 😂"
            )
            MomentType.EMOTIONAL -> ClipTemplate(
                title = "Clip #$n • Real Story Behind $topic",
                hookLine = "This honest confession hits straight to the heart.",
                reason = "Authentic emotional peak ideal for saves & comments (Sample AI)",
                introText = "THIS HIT DIFFERENT 🥹",
                outroText = "Drop a ❤️ if you felt this moment"
            )
            MomentType.MOTIVATIONAL -> ClipTemplate(
                title = "Clip #$n • Zero-Excuse Mindset Shift",
                hookLine = "One decision changed everything overnight.",
                reason = "High-conviction motivational takeaway with strong replay value (Sample AI)",
                introText = "READ THAT AGAIN ⚡",
                outroText = "Bookmark this for your daily grind ⚡"
            )
            MomentType.HIGH_ENERGY -> ClipTemplate(
                title = "Clip #$n • Peak Action Highlight",
                hookLine = "Watch how fast everything escalates here!",
                reason = "Fast-paced climax optimized for 9:16 Reels & Shorts loops (Sample AI)",
                introText = "PURE ADRENALINE 🚀",
                outroText = "Follow for Part 2 🔥"
            )
        }
    }

    private fun buildSynchronizedCues(
        durationMs: Long,
        moment: MomentType,
        topic: String,
        hookLine: String
    ): List<SubtitleCue> {
        val rawLines = when (moment) {
            MomentType.VIRAL_HOOK -> listOf(
                Triple(
                    hookLine,
                    "99% लोग इस सबसे बड़े राज़ को मिस कर देते हैं!",
                    "99% log is sabse bade secret ko miss kar dete hain!"
                ) to "🔥",
                Triple(
                    "Look closely at how $topic actually works.",
                    "ध्यान से देखो $topic असल में कैसे काम करता है।",
                    "Dhyan se dekho $topic actually kaise kaam karta hai."
                ) to "👀",
                Triple(
                    "Most creators overcomplicate the very first step.",
                    "ज़्यादातर क्रिएटर्स पहले ही स्टेप को मुश्किल बना देते हैं।",
                    "Zyadatar creators pehle step ko hi complicate kar dete hain."
                ) to "💡",
                Triple(
                    "Apply this one framework and watch your reach explode!",
                    "बस यह एक तरीका अपनाओ और रीच बढ़ते हुए देखो!",
                    "Bas yeh ek framework try karo aur reach explode hote dekho!"
                ) to "🚀"
            )
            MomentType.FUNNY -> listOf(
                Triple(
                    "Wait for the expression right after this line!",
                    "इस लाइन के तुरंत बाद वाला रिएक्शन देखो!",
                    "Is line ke turant baad wala reaction dekho!"
                ) to "😂",
                Triple(
                    "Everything was going smoothly until this twist hit.",
                    "सब कुछ ठीक चल रहा था जब तक यह ट्विस्ट नहीं आया।",
                    "Sab kuch smooth chal raha tha jab tak yeh twist nahi aaya."
                ) to "🤯",
                Triple(
                    "Even the studio couldn't hold it together!",
                    "स्टूडियो में भी कोई हंसी नहीं रोक पाया!",
                    "Studio mein bhi koi hasi nahi rok paaya!"
                ) to "🤣",
                Triple(
                    "Send this clip to your funniest friend right now.",
                    "यह क्लिप अभी अपने सबसे मज़ाकिया दोस्त को भेजो।",
                    "Yeh clip abhi apne sabse funny dost ko bhejo."
                ) to "📲"
            )
            MomentType.EMOTIONAL -> listOf(
                Triple(
                    "Nobody talks about the silent struggle behind $topic.",
                    "$topic के पीछे के संघर्ष के बारे में कोई बात नहीं करता।",
                    "$topic ke peeche ke silent struggle par koi baat nahi karta."
                ) to "🥹",
                Triple(
                    "There were days when quitting felt like the only choice.",
                    "ऐसे दिन भी थे जब हार मान लेना आसान लगता था।",
                    "Aise din bhi the jab give up karna sabse easy lagta tha."
                ) to "💔",
                Triple(
                    "Showing up one more time changed the entire journey.",
                    "बस एक बार और कोशिश करने से पूरा सफ़र बदल गया।",
                    "Bas ek baar aur try karne se poori journey badal gayi."
                ) to "✨",
                Triple(
                    "Keep going—your breakthrough is closer than you think.",
                    "चलते रहो—आपकी सफलता आपके बहुत करीब है।",
                    "Chalte raho—tumhara breakthrough bilkul paas hai."
                ) to "❤️"
            )
            MomentType.MOTIVATIONAL -> listOf(
                Triple(
                    "Stop waiting for the perfect moment to start.",
                    "सही समय का इंतज़ार करना आज ही बंद करो।",
                    "Perfect moment ka wait karna aaj hi band karo."
                ) to "⚡",
                Triple(
                    "Consistency beats talent every single week.",
                    "लगातार मेहनत हर हफ्ते टैलेंट को हरा देती है।",
                    "Consistency har hafte talent ko beat kar deti hai."
                ) to "💪",
                Triple(
                    "Master $topic by taking daily micro-actions.",
                    "रोज़ छोटे कदम उठाकर $topic में महारत हासिल करो।",
                    "Roz chhote steps lekar $topic ko master karo."
                ) to "🎯",
                Triple(
                    "Save this reminder and execute today!",
                    "इस रिमाइंडर को सेव करो और आज से शुरू करो!",
                    "Is reminder ko save karo aur aaj hi start karo!"
                ) to "🔥"
            )
            MomentType.HIGH_ENERGY -> listOf(
                Triple(
                    "Right here is where the energy goes to 100%!",
                    "यहीं से एनर्जी सीधे 100% पर पहुंच जाती है!",
                    "Yahin se energy seedhe 100% par chali jaati hai!"
                ) to "🚀",
                Triple(
                    "Watch the pacing shift in real time on $topic.",
                    "देखो $topic में रफ़्तार कैसे बदलती है।",
                    "Dekho $topic mein pacing kaise shift hoti hai."
                ) to "⚡",
                Triple(
                    "Zero fluff—pure highlight reel action.",
                    "कोई बोरिंग हिस्सा नहीं—सिर्फ़ दमदार हाइलाइट।",
                    "Zero boring parts—sirf pure viral highlight!"
                ) to "🎬",
                Triple(
                    "Drop a fire emoji if you want the full breakdown!",
                    "अगर पूरा वीडियो चाहिए तो कमेंट में 🔥 लिखो!",
                    "Full breakdown chahiye toh comments mein 🔥 drop karo!"
                ) to "🔥"
            )
        }

        val segmentCount = rawLines.size
        val stepMs = max(1_500L, durationMs / segmentCount)
        return rawLines.mapIndexed { idx, (triple, emoji) ->
            val cueStart = idx * stepMs
            val cueEnd = if (idx == segmentCount - 1) durationMs else (idx + 1) * stepMs - 150L
            SubtitleCue(
                id = idx + 1,
                startOffsetMs = cueStart,
                endOffsetMs = max(cueStart + 800L, cueEnd),
                textEnglish = triple.first,
                textHindi = triple.second,
                textHinglish = triple.third,
                emoji = emoji
            )
        }
    }
}
