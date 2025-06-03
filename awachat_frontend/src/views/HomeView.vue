<script setup lang="ts">
import HomeChatArea from '@/components/home/HomeChatArea.vue';
import HomeConversationList from '@/components/home/HomeConversationList.vue';
import type { Conversation } from '@/dto/base';
import { ref } from 'vue';

// 模拟会话数据
const conversations = ref<Conversation[]>([
  { id: 1, name: '张三', lastMessage: '你好，最近怎么样？', time: '14:30', unread: 2 },
  { id: 2, name: '李四', lastMessage: '明天我们讨论一下项目进度', time: '昨天', unread: 0 },
  { id: 3, name: '王五', lastMessage: '好的，没问题', time: '周一', unread: 0 },
  { id: 4, name: '开发小组', lastMessage: '[群消息] 小明: 已经提交代码了', time: '周日', unread: 5 },
]);

// 当前选中的会话，添加类型声明
const selectedConversation = ref<number | null>(null);

function handleSelectConversation(select: number) {
  selectedConversation.value = select;
}

</script>

<template>
  <main class="chat-container">
    <!-- 左侧会话列表 -->
    <HomeConversationList :conversations="conversations" :selected-conversation="selectedConversation"
      @select-conversation="handleSelectConversation" />
    <HomeChatArea :selected-conversation="selectedConversation" />

  </main>
</template>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
  color: #333;
}
</style>
