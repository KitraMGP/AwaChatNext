<script setup lang="ts">
import HomeChatArea from '@/components/home/HomeChatArea.vue';
import HomeConversationList from '@/components/home/HomeConversationList.vue';
import type { Conversation } from '@/dto/base';
import router from '@/router';
import { getUserInfoApi } from '@/services/userApi';
import { initWebSocketService } from '@/services/initWebsocket';
import { useUserDataStore } from '@/stores/userDataStore';
import { onMounted, ref, watch } from 'vue';

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

const userData = useUserDataStore()
if (userData.value == null) {
  router.push('/login')
}
watch(userData, (newValue) => {
  if (newValue.value == null) {
    router.push('/login')
  }
})

// 在组件挂载后调用getUserInfoApi，并在成功时初始化WebSocket连接
onMounted(async () => {
  try {
    // 调用getUserInfoApi检查用户是否已登录
    // 如果未登录，axios拦截器会自动跳转到登录页面
    const response = await getUserInfoApi();

    // 如果API调用成功，初始化WebSocket连接
    if (response.data.code === 200) {
      userData.set(response.data.data)

      console.log('用户已登录，初始化WebSocket连接');
      // 初始化WebSocket服务
      initWebSocketService();
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    // 错误处理已由axios拦截器完成，这里不需要额外处理
  }
});
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
