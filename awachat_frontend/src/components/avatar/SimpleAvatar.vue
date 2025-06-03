<script setup lang="ts">
import { computed } from 'vue';

type Size = "small" | "medium" | "large"

// 通过指定 Size 来设定头像的显示大小
const props = defineProps<{
  avatarUrl?: string | null,
  text?: string | null,
  size: Size
}>()

const avatarClass = computed(() => ({
  small: props.size == "small",
  medium: props.size == "medium",
  large: props.size == "large"
}))
</script>

<template>
  <div v-if="props.text != null" class="avatar avatar-text" :class="avatarClass">
    {{ props.text.charAt(0) }}
  </div>
  <div v-if="props.avatarUrl != null" class="avatar avatar-image" :class="avatarClass">
    <img :src="props.avatarUrl" width="100%" height="100%">
  </div>
</template>

<style lang="css" scoped>
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.avatar-text {
  border: 1px solid var(--el-border-color-darker);
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  margin-right: 12px;
}

.avatar-image {
  border: 1px solid var(--el-border-color-darker);
  border-radius: 50%;
  overflow: hidden;
}

.small {
  height: 20px;
  width: 20px;
}

.medium {
  height: 40px;
  width: 40px;
}

.large {
  height: 60px;
  width: 60px;
}
</style>
