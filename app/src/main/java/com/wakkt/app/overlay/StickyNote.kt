package com.wakkt.app.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakkt.app.data.NoteEffects
import com.wakkt.app.data.NoteImageStore
import com.wakkt.app.data.NoteMotion
import com.wakkt.app.data.NoteStyle
import com.wakkt.app.ui.theme.WakktTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * The note shown over other apps and in the editor preview. Its paper is a [ChaosCanvas]: a new
 * random, deliberately unpleasant scene for every [chaosSeed]. The text sits on a plate so the
 * reminder stays readable. [imagePath] is a file stored by [NoteImageStore], decoded off the main thread.
 */
@Composable
fun StickyNote(
    text: String,
    closable: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    style: NoteStyle = NoteStyle.CLASSIC,
    imagePath: String? = null,
    maxWidth: Dp = 264.dp,
    effects: NoteEffects = NoteEffects(),
    chaosSeed: Long = 0L,
) {
    var image by remember(imagePath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(imagePath) {
        val path = imagePath ?: return@LaunchedEffect
        image = withContext(Dispatchers.IO) { NoteImageStore.load(path)?.asImageBitmap() }
    }
    StickyNoteContent(text, closable, onClose, modifier, style, image, maxWidth, effects, chaosSeed)
}

/** Random typography and text plate for one appearance. */
private class NoteLook(seed: Long) {
    private val r = Random(seed xor 0x5DEECE66DL)
    val font: FontFamily = listOf(FontFamily.SansSerif, FontFamily.Serif, FontFamily.Monospace, FontFamily.Cursive)[r.nextInt(4)]
    val italic: Boolean = r.nextFloat() < 0.35f
    val weight: FontWeight = listOf(FontWeight.Medium, FontWeight.Bold, FontWeight.Black)[r.nextInt(3)]
    private val darkPlate: Boolean = r.nextBoolean()
    val plate: Color = if (darkPlate) Color.Black.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.82f)
    val ink: Color = if (darkPlate) Color.White else Color.Black
    val tilt: Float = r.nextFloat() * 6f - 3f
}

@Composable
internal fun StickyNoteContent(
    text: String,
    closable: Boolean,
    onClose: () -> Unit,
    modifier: Modifier,
    style: NoteStyle,
    image: ImageBitmap?,
    maxWidth: Dp,
    effects: NoteEffects = NoteEffects(),
    chaosSeed: Long = 0L,
) {
    val look = remember(chaosSeed) { NoteLook(chaosSeed) }
    val motion = rememberMotion(effects.motion)
    val glitch = rememberGlitch(effects.glitch)
    val animated = effects.motion != NoteMotion.NONE || effects.glitch

    val shape: Shape
    val elevation: Dp
    val border: BorderStroke?
    when (style) {
        NoteStyle.CLASSIC -> { shape = RoundedCornerShape(3.dp); elevation = 7.dp; border = null }
        NoteStyle.SOFT -> { shape = RoundedCornerShape(22.dp); elevation = 10.dp; border = null }
        NoteStyle.OUTLINE -> { shape = RoundedCornerShape(14.dp); elevation = 3.dp; border = BorderStroke(3.dp, look.ink) }
    }
    val flashing = effects.motion == NoteMotion.FLASH
    val fold = look.ink.copy(alpha = 0.35f)

    // Outer padding leaves room for the shadow (and motion) so it is not clipped by the window.
    Box(modifier = modifier.padding(if (animated) 14.dp else 10.dp)) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = motion.scale.value
                    scaleY = motion.scale.value
                    rotationZ = motion.rotation.value + look.tilt
                    translationY = motion.translationY.value
                    translationX = glitch.offsetX
                }
                .then(if (image != null) Modifier.width(maxWidth) else Modifier.widthIn(max = maxWidth))
                .shadow(elevation, shape, spotColor = Color.Black.copy(alpha = 0.4f))
                .then(
                    if (style == NoteStyle.CLASSIC) Modifier.drawWithContent {
                        drawContent()
                        // Folded bottom-right corner.
                        val s = 18.dp.toPx()
                        val path = Path().apply {
                            moveTo(size.width - s, size.height)
                            lineTo(size.width, size.height - s)
                            lineTo(size.width - s, size.height - s)
                            close()
                        }
                        drawPath(path, fold)
                        drawLine(fold, Offset(size.width - s, size.height), Offset(size.width, size.height - s), 1.dp.toPx())
                    } else Modifier,
                ),
            shape = shape,
            color = Color.Transparent,
            contentColor = look.ink,
            border = if (flashing) BorderStroke(3.dp, look.ink.copy(alpha = motion.flash.value)) else border,
        ) {
            Box(Modifier.drawWithContent { drawContent(); drawGlitch(glitch.seed) }) {
                ChaosCanvas(chaosSeed, Modifier.matchParentSize())
                Column(Modifier.padding(8.dp)) {
                    if (image != null) {
                        Image(
                            bitmap = image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                                .heightIn(max = maxWidth * 0.62f)
                                .clip(RoundedCornerShape(if (style == NoteStyle.CLASSIC) 2.dp else 14.dp)),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(look.plate)
                            .padding(
                                start = 12.dp,
                                end = if (closable) 2.dp else 12.dp,
                                top = if (closable) 2.dp else 10.dp,
                                bottom = if (closable) 2.dp else 10.dp,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = text,
                            fontSize = 16.sp,
                            lineHeight = 21.sp,
                            fontFamily = look.font,
                            fontWeight = look.weight,
                            fontStyle = if (look.italic) FontStyle.Italic else FontStyle.Normal,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 8,
                            modifier = Modifier.weight(1f, fill = image != null),
                        )
                        if (closable) {
                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                if (effects.grain) TvStaticLayer(alpha = 0.16f, modifier = Modifier.matchParentSize())
            }
        }
    }
}

@Preview(name = "Chaos notes", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StickyNotePreview() {
    WakktTheme(darkTheme = false) {
        Column {
            StickyNoteContent("Check your budget first", true, {}, Modifier, NoteStyle.CLASSIC, null, 264.dp, chaosSeed = 1)
            StickyNoteContent("10 minutes max", false, {}, Modifier, NoteStyle.SOFT, null, 264.dp, chaosSeed = 2)
            StickyNoteContent("Never share your code", true, {}, Modifier, NoteStyle.OUTLINE, null, 264.dp, chaosSeed = 3)
        }
    }
}
