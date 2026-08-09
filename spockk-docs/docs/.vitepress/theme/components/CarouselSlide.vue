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

defineProps<{ title: string; description?: string }>()
</script>

<template>
  <div class="carousel-slide">
    <div class="carousel-slide-header">
      <h3>{{ title }}</h3>
      <p v-if="description">{{ description }}</p>
    </div>
    <div class="carousel-slide-body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.carousel-slide {
  flex: 0 0 100%;
  box-sizing: border-box;
  padding: 1.75rem 1.5rem;
}

.carousel-slide-header {
  max-width: 60ch;
  margin: 0 auto 1.5rem;
  text-align: center;
}

.carousel-slide-header h3 {
  font-size: 1.25rem;
  margin: 0 0 0.5rem;
  border: none;
  padding: 0;
}

.carousel-slide-header p {
  color: var(--vp-c-text-2);
  margin: 0;
}

.carousel-slide-body {
  max-width: 560px;
  margin: 0 auto;
}

.carousel-slide-body :deep(div[class*='language-']) {
  margin: 0;
}

.carousel-slide-body > :deep(*:not(:last-child)) {
  margin-bottom: 1rem;
}
</style>
