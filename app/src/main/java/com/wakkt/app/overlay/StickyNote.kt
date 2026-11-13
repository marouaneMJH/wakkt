package com.wakkt.app.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakkt.app.data.NoteColor
import com.wakkt.app.data.NoteEffects
import com.wakkt.app.data.NoteMotion
import com.wakkt.app.data.NotePattern
import com.wakkt.app.data.NoteImageStore
import com.wakkt.app.data.NoteStyle
import com.wakkt.app.ui.theme.WakktTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The note shown over other apps and in the editor preview. Stateless; follows the system theme.
 * [imagePath] is a file stored by [NoteImageStore]; it is decoded off the main thread.
 */
@Composable
fun StickyNote(
    text: String,
    closable: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    color: NoteColor = NoteColor.YELLOW,
    style: NoteStyle = NoteStyle.CLASSIC,
    imagePath: String? = null,
    maxWidth: Dp = 264.dp,
    effects: NoteEffects = NoteEffects(),
) {
    var image by remember(imagePath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(imagePath) {
        val path = imagePath ?: return@LaunchedEffect
        image = withContext(Dispatchers.IO) { NoteImageStore.load(path)?.asImageBitmap() }
    }
    StickyNoteContent(text, closable, onClose, modifier, color, style, image, maxWidth, effects)
}

@Composable
internal fun StickyNoteContent(
    text: String,
    closable: Boolean,
    onClose: () -> Unit,
    modifier: Modifier,
    color: NoteColor,
    style: NoteStyle,
    image: ImageBitmap?,
    maxWidth: Dp,
    effects: NoteEffects = NoteEffects(),
) {
    val p = color.palette(isSystemInDarkTheme())
    val motion = rememberMotion(effects.motion)
    val glitch = rememberGlitch(effects.glitch)
    val animated = effects.motion != NoteMotion.NONE || effects.glitch
    val surfaceColor = MaterialTheme.colorScheme.surface
    val shape: Shape
    val paper: Color
    val elevation: Dp
    val border: BorderStroke?
    when (style) {
        NoteStyle.CLASSIC -> { shape = RoundedCornerShape(3.dp); paper = p.paper; elevation = 7.dp; border = null }
        NoteStyle.SOFT -> { shape = RoundedCornerShape(22.dp); paper = p.paper; elevation = 10.dp; border = null }
        NoteStyle.OUTLINE -> {
            shape = RoundedCornerShape(14.dp); paper = lerp(surfaceColor, p.paper, 0.35f)
            elevation = 3.dp; border = BorderStroke(1.5.dp, p.accent)
        }
    }
    val ink = if (style == NoteStyle.OUTLINE) MaterialTheme.colorScheme.onSurface else p.ink
    val fold = p.accent.copy(alpha = 0.35f)

    val flashing = effects.motion == NoteMotion.FLASH
    // Outer padding leaves room for the shadow (and motion) so it is not clipped by the window.
    Box(modifier = modifier.padding(if (animated) 14.dp else 10.dp)) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = motion.scale.value
                    scaleY = motion.scale.value
                    rotationZ = motion.rotation.value + if (style == NoteStyle.CLASSIC) -1.5f else 0f
                    translationY = motion.translationY.value
                    translationX = glitch.offsetX
                }
                .then(if (image != null) Modifier.width(maxWidth) else Modifier.widthIn(max = maxWidth))
                .shadow(elevation, shape, ambientColor = p.accent, spotColor = Color.Black.copy(alpha = 0.3f))
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
            color = paper,
            contentColor = ink,
            border = if (flashing) BorderStroke(3.dp, p.accent.copy(alpha = motion.flash.value)) else border,
        ) {
          Box(Modifier.drawWithContent { drawContent(); drawGlitch(glitch.seed) }) {
            Column(
                Modifier.then(
                    if (style == NoteStyle.OUTLINE) Modifier
                        .drawBehind { drawRect(p.accent, size = Size(5.dp.toPx(), size.height)) }
                        .padding(start = 5.dp)
                    else Modifier,
                ).then(
                    if (effects.pattern != NotePattern.NONE) Modifier.drawBehind { drawPattern(effects.pattern, ink.copy(alpha = 0.09f)) }
                    else Modifier,
                ),
            ) {
                    if (image != null) {
                        val inset = if (style == NoteStyle.CLASSIC) 0.dp else 6.dp
                        Image(
                            bitmap = image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(start = inset, top = inset, end = inset)
                                .fillMaxWidth()
                                .heightIn(max = maxWidth * 0.62f)
                                .clip(if (style == NoteStyle.CLASSIC) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp)),
                        )
                    }
                    Row(
                        modifier = Modifier.padding(
                            start = 14.dp,
                            end = if (closable) 4.dp else 14.dp,
                            top = if (closable) 4.dp else 12.dp,
                            bottom = if (closable) 4.dp else 12.dp,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = text,
                            fontSize = if (style == NoteStyle.SOFT) 16.sp else 15.sp,
                            lineHeight = 21.sp,
                            fontWeight = if (style == NoteStyle.SOFT) FontWeight.SemiBold else FontWeight.Medium,
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

@Preview(name = "Styles light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StickyNoteLightPreview() {
    WakktTheme(darkTheme = false) {
        Column {
            StickyNoteContent("Check your budget first", true, {}, Modifier, NoteColor.YELLOW, NoteStyle.CLASSIC, null, 264.dp)
            StickyNoteContent("10 minutes max", false, {}, Modifier, NoteColor.MINT, NoteStyle.SOFT, null, 264.dp)
            StickyNoteContent("Never share your code", true, {}, Modifier, NoteColor.SKY, NoteStyle.OUTLINE, null, 264.dp)
        }
    }
}

@Preview(name = "Styles dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun StickyNoteDarkPreview() {
    WakktTheme(darkTheme = true) {
        Column {
            StickyNoteContent("10 minutes max", false, {}, Modifier, NoteColor.PINK, NoteStyle.CLASSIC, null, 264.dp)
            StickyNoteContent("Breathe first", true, {}, Modifier, NoteColor.LAVENDER, NoteStyle.SOFT, null, 264.dp)
        }
    }
}
