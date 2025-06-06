<script setup lang="ts">
import HomeChatArea from '@/components/home/HomeChatArea.vue';
import HomeConversationList from '@/components/home/HomeConversationList.vue';
import type { ChatInfo, DisplayConversation, PrivateChatInfo } from '@/dto/chat';
import router from '@/router';
import { chatListApi } from '@/services/chatApi';
import { getUserInfoApi } from '@/services/userApi';
import { initWebSocketService } from '@/services/initWebsocket';
import { useUserDataStore } from '@/stores/userDataStore';
import { onMounted, ref, watch } from 'vue';
import { showFailMessage } from '@/services/api';

// 会话数据
const conversations = ref<DisplayConversation[]>([]);
const chatInfos = ref<ChatInfo<PrivateChatInfo>[]>([])

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

// 加载聊天列表
async function loadChatList() {
  try {
    const response = await chatListApi();
    if (response.data.code === 200) {
      // 将API返回的数据转换为UI显示所需的格式
      const chatList = response.data.data.chats;

      // 先存入chatInfo
      chatInfos.value = chatList
      // 转换为DisplayConversation格式
      conversations.value = chatList.map((chat, index) => {
        // 这里只处理私聊类型，群聊类型可以后续扩展
        if (chat.type === 'private') {
          const privateChatInfo = chat.info;

          // 创建显示用的会话对象，直接使用新的数据结构
          return {
            id: privateChatInfo.chatId || index, // 优先使用chatId，如果为0则使用索引
            name: privateChatInfo.nickname || privateChatInfo.username, // 优先使用昵称，如果没有则使用用户名
            lastMessage: '暂无消息', // 暂无消息历史
            time: new Date(privateChatInfo.updatedAt).toLocaleString(), // 格式化时间
            unread: 0 // 暂无未读消息计数
          };
        }
        return null;
      }).filter(Boolean) as DisplayConversation[];
    }
  } catch (error) {
    console.error('获取聊天列表失败:', error);
    showFailMessage('获取聊天列表失败', error);
  }
}

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

      // 加载聊天列表
      await loadChatList();
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    // 错误处理已由axios拦截器完成，这里不需要额外处理
  }
});

/**
 * 获取当前选中的会话的信息
 */
function getCurrentChatInfo(): ChatInfo<PrivateChatInfo> | null {
  const selectedChat = chatInfos.value.filter((chat) => chat.info.chatId === selectedConversation.value)
  if (selectedChat.length != 1) return null
  return selectedChat[0]
}
</script>

<template>
  <main class="chat-container">
    <!-- 左侧会话列表 -->
    <HomeConversationList :conversations="conversations" :selected-conversation="selectedConversation"
      @select-conversation="handleSelectConversation" />
    <HomeChatArea :selected-conversation="selectedConversation" :chat-info="getCurrentChatInfo()" />
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
