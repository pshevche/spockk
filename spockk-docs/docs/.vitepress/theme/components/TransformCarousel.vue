<script setup lang="ts">
import { onMounted, ref, useTemplateRef } from 'vue'

const track = useTemplateRef<HTMLDivElement>('track')
const activeIndex = ref(0)
const slideCount = ref(0)

function scrollToIndex(index: number) {
  const el = track.value
  if (!el) return
  const target = el.children[index] as HTMLElement | undefined
  target?.scrollIntoView({ behavior: 'smooth', inline: 'start', block: 'nearest' })
}

function go(delta: number) {
  const next = Math.min(Math.max(activeIndex.value + delta, 0), slideCount.value - 1)
  scrollToIndex(next)
}

onMounted(() => {
  const el = track.value
  if (!el) return

  const slides = Array.from(el.children) as HTMLElement[]
  slideCount.value = slides.length

  if (typeof IntersectionObserver === 'undefined') return

  const observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          activeIndex.value = slides.indexOf(entry.target as HTMLElement)
        }
      }
    },
    { root: el, threshold: 0.6 },
  )
  slides.forEach((slide) => observer.observe(slide))
})
</script>

<template>
  <div class="transform-carousel">
    <div ref="track" class="transform-carousel-track">
      <slot />
    </div>

    <div class="transform-carousel-controls">
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
          @click="scrollToIndex(i - 1)"
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
  </div>
</template>

<style scoped>
.transform-carousel {
  margin: 2rem 0;
}

.transform-carousel-track {
  display: flex;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
  border: 1px solid var(--vp-c-divider);
  border-radius: 16px;
  background: var(--vp-c-bg-soft);
}

.transform-carousel-track::-webkit-scrollbar {
  display: none;
}

.transform-carousel-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-top: 1.25rem;
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
  transition: border-color 0.2s, color 0.2s, transform 0.2s;
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
  transition: background 0.2s, transform 0.2s;
}

.carousel-dot.active {
  background: var(--spockk-gradient);
  transform: scale(1.35);
}
</style>
