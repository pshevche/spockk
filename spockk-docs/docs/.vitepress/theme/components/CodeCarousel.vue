<script setup lang="ts">
/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { computed, onMounted, onUnmounted, ref, useTemplateRef } from 'vue'

const track = useTemplateRef<HTMLDivElement>('track')
const activeIndex = ref(0)
const slideCount = ref(0)
const trackHeight = ref<number>()
const trackHeightPx = computed(() => (trackHeight.value ? `${trackHeight.value}px` : 'auto'))
const trackOffset = computed(() => `translateX(-${activeIndex.value * 100}%)`)

let slides: HTMLElement[] = []
let resizeObserver: ResizeObserver | undefined

// scrollHeight, not getBoundingClientRect: slides are flex items that
// stretch to the tallest sibling by default, so a slide's own rendered
// height doesn't reflect its actual content height once stretched.
function updateHeight() {
  const active = slides[activeIndex.value]
  if (active) trackHeight.value = active.scrollHeight
}

function goTo(index: number) {
  activeIndex.value = Math.min(Math.max(index, 0), slideCount.value - 1)
  updateHeight()
}

function go(delta: number) {
  goTo(activeIndex.value + delta)
}

onMounted(() => {
  const el = track.value
  if (!el) return

  slides = Array.from(el.children) as HTMLElement[]
  slideCount.value = slides.length
  updateHeight()

  if (typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(updateHeight)
    slides.forEach((slide) => resizeObserver?.observe(slide))
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
})
</script>

<template>
  <div class="carousel">
    <div class="carousel-controls">
      <button
        type="button"
        class="carousel-arrow"
        aria-label="Previous"
        :disabled="activeIndex === 0"
        @click="go(-1)"
      >
        ‹
      </button>

      <span class="carousel-dots">
        <button
          v-for="i in slideCount"
          :key="i"
          type="button"
          class="carousel-dot"
          :class="{ active: activeIndex === i - 1 }"
          :aria-label="`Go to slide ${i}`"
          @click="goTo(i - 1)"
        />
      </span>

      <button
        type="button"
        class="carousel-arrow"
        aria-label="Next"
        :disabled="activeIndex === slideCount - 1"
        @click="go(1)"
      >
        ›
      </button>
    </div>

    <!-- Slides are moved with a CSS transform driven by the controls
         above, not native scrolling: a horizontally-scrollable track
         fights with page scroll (a trackpad user scrolling the page
         over the carousel nudges it sideways) and browsers then
         "snap" it back to a fully-aligned slide, which reads as the
         carousel jumping on its own. A fixed, non-scrolling viewport
         has no scroll position for the browser to correct. -->
    <div
      class="carousel-viewport"
      role="region"
      aria-roledescription="carousel"
      tabindex="0"
      @keydown.left="go(-1)"
      @keydown.right="go(1)"
    >
      <div ref="track" class="carousel-track">
        <slot />
      </div>
    </div>
  </div>
</template>

<style scoped>
.carousel {
  position: relative;
  margin: 2rem 0;
}

/* A soft ambient glow instead of a flat, boxy fill, echoing the same
   glow used behind the home page hero. Fixed px offsets (not
   percentages): see CodeWindow's hero glow for why a percentage bleed
   on a wide box can grow past the viewport and force horizontal
   scroll, which this needs to avoid just as much at 992px content
   width as the hero does. */
.carousel::before {
  content: '';
  position: absolute;
  top: -24px;
  right: -24px;
  bottom: -24px;
  left: -24px;
  background: var(--spockk-gradient);
  opacity: 0.12;
  filter: blur(70px);
  z-index: -1;
  border-radius: 32px;
}

.carousel-viewport {
  overflow: hidden;
  border: 1px solid var(--vp-c-divider);
  border-radius: 16px;
  background: color-mix(in srgb, var(--vp-c-bg-soft) 70%, transparent);
  backdrop-filter: blur(6px);
  height: v-bind(trackHeightPx);
  transition: height 0.3s ease;
}

.carousel-viewport:focus-visible {
  outline: 2px solid var(--spockk-purple);
  outline-offset: 2px;
}

.carousel-track {
  display: flex;
  /* flex-start, not the default stretch: stretch forces every slide
     to share the height of the tallest one, which then makes each
     slide's own scrollHeight report that shared height instead of its
     own content height, defeating the active-slide height fit above. */
  align-items: flex-start;
  transform: v-bind(trackOffset);
  transition: transform 0.4s ease;
}

.carousel-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.carousel-arrow {
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 50%;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg);
  color: var(--vp-c-text-1);
  font-size: 1.25rem;
  line-height: 1;
  cursor: pointer;
  transition:
    border-color 0.2s,
    color 0.2s,
    transform 0.2s;
}

.carousel-arrow:hover:not(:disabled) {
  border-color: var(--spockk-purple);
  color: var(--spockk-purple);
  transform: scale(1.08);
}

.carousel-arrow:disabled {
  opacity: 0.35;
  cursor: default;
}

.carousel-dots {
  display: flex;
  gap: 0.5rem;
}

.carousel-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: none;
  padding: 0;
  background: var(--vp-c-divider);
  cursor: pointer;
  transition:
    background 0.2s,
    transform 0.2s;
}

.carousel-dot.active {
  background: var(--spockk-gradient);
  transform: scale(1.35);
}
</style>
