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

defineProps<{
  hasTitle: string
  has: string[]
  lacksTitle: string
  lacks: string[]
}>()
</script>

<template>
  <div class="comparison-panels">
    <div v-reveal class="comparison-panel panel-has">
      <h3><span class="panel-icon">✓</span>{{ hasTitle }}</h3>
      <ul>
        <!-- eslint-disable-next-line vue/no-v-html -- static strings from limitations.md's own script setup, not user input -->
        <li v-for="(item, i) in has" :key="i" :style="{ '--i': i }" v-html="item" />
      </ul>
    </div>
    <div v-reveal class="comparison-panel panel-lacks">
      <h3><span class="panel-icon">–</span>{{ lacksTitle }}</h3>
      <ul>
        <!-- eslint-disable-next-line vue/no-v-html -- static strings from limitations.md's own script setup, not user input -->
        <li v-for="(item, i) in lacks" :key="i" :style="{ '--i': i }" v-html="item" />
      </ul>
    </div>
  </div>
</template>

<style scoped>
.comparison-panels {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 1.5rem;
  margin: 2rem 0;
}

.comparison-panel {
  border-radius: 16px;
  padding: 1.75rem 1.75rem 1.5rem;
  border: 1px solid var(--vp-c-divider);
  opacity: 0;
  transform: translateY(10px);
  transition:
    opacity 0.5s ease,
    transform 0.5s ease;
}

.comparison-panel.in-view {
  opacity: 1;
  transform: translateY(0);
}

.panel-has {
  background: color-mix(in srgb, var(--spockk-purple) 7%, transparent);
  border-color: color-mix(in srgb, var(--spockk-purple) 30%, var(--vp-c-divider));
}

.panel-lacks {
  background: var(--vp-c-bg-soft);
}

.comparison-panel h3 {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin: 0 0 1.1rem;
  font-size: 1.05rem;
  border: none;
  padding: 0;
}

.panel-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.6rem;
  height: 1.6rem;
  border-radius: 50%;
  font-size: 0.85rem;
  flex: none;
}

.panel-has .panel-icon {
  background: var(--spockk-gradient);
  color: white;
}

.panel-lacks .panel-icon {
  background: var(--vp-c-divider);
  color: var(--vp-c-text-2);
}

.comparison-panel ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.comparison-panel li {
  padding-left: 1.5rem;
  position: relative;
  line-height: 1.5;
  overflow-wrap: break-word;
  opacity: 0;
  transform: translateX(-6px);
}

.comparison-panel.in-view li {
  animation: item-in 0.4s ease forwards;
  animation-delay: calc(var(--i, 0) * 90ms);
}

.panel-has li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--spockk-purple);
  font-weight: 700;
}

.panel-lacks li::before {
  content: '×';
  position: absolute;
  left: 0.15rem;
  color: var(--vp-c-text-3);
  font-weight: 700;
}

@keyframes item-in {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@media (max-width: 640px) {
  .comparison-panels {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
