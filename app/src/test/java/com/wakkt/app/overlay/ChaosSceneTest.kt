package com.wakkt.app.overlay

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ChaosSceneTest {
    /** Photosensitivity guard: no random scene may alternate 3+ times per second anywhere. */
    @Test fun `no random scene strobes`() {
        repeat(5_000) { seed ->
            val scene = ChaosScene.random(Random(seed))
            assertTrue(
                "seed $seed alternates at ${scene.maxCyclesPerSecond} Hz",
                scene.maxCyclesPerSecond <= MAX_ALTERNATION_HZ,
            )
        }
        assertTrue(MAX_ALTERNATION_HZ < 3f)
    }

    @Test fun `scenes are random but reproducible per seed`() {
        val a = ChaosScene.random(Random(1))
        val b = ChaosScene.random(Random(1))
        assertTrue(a.maxCyclesPerSecond == b.maxCyclesPerSecond && a.layerCount == b.layerCount)
        val distinct = (0 until 50).map { ChaosScene.random(Random(it)).maxCyclesPerSecond }.toSet()
        assertTrue("scenes should vary", distinct.size > 25)
        repeat(200) { assertTrue(ChaosScene.random(Random(it)).layerCount in 2..3) }
    }
}
