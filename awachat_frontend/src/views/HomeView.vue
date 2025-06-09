<script setup lang="ts">
import type { ChatInfo, DisplayChat, PrivateChatInfo } from '@/dto/chat';
import router from '@/router';
import { chatListApi } from '@/services/chatApi';
import { deleteFriendApi, getUserInfoApi } from '@/services/userApi';
import { initWebSocketService } from '@/services/initWebsocket';
import { useUserDataStore } from '@/stores/userDataStore';
import { onMounted, ref, watch } from 'vue';
import { showFailMessage, showSuccessfulMessage } from '@/services/api';
import { previewChatMessageData, type ChatMessageData } from '@/dto/websocket';
import HomeChatArea from '@/components/home/HomeChatArea.vue';
import HomeChatList from '@/components/home/HomeChatList.vue';
import type { UserData } from '@/dto/userDto';
import dayjs from 'dayjs';
import { ElButton, ElMessage, ElMessageBox } from 'element-plus';

// 会话数据
const chats = ref<DisplayChat[]>([]);
const chatInfos = ref<ChatInfo<PrivateChatInfo>[]>([])

// 当前选中的会话
const selectedChatId = ref<number | null>(null);

// 个人信息对话框是否显示，以及显示的用户ID
const showUserInfoDialog = ref(false)
const userInfoDialogUser = ref<UserData | null>(null)

/**
 * 处理消息列表中点击打开会话的逻辑。
 */
function handleSelectChat(select: number) {
  // 选择会话
  selectedChatId.value = select;
  // 清空该会话的未读
  chats.value.filter(chat => chat.id === select).forEach(chat => chat.unread = 0)
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
      // 转换为DisplayChat格式
      chats.value = chatList.map((chat, index) => {
        // 这里只处理私聊类型，群聊类型可以后续扩展
        if (chat.type === 'private') {
          const privateChatInfo = chat.info;

          // 创建显示用的会话对象，直接使用新的数据结构
          return {
            id: privateChatInfo.chatId || index, // 优先使用chatId，如果为0则使用索引
            userId: privateChatInfo.userId,
            name: privateChatInfo.nickname || privateChatInfo.username, // 优先使用昵称，如果没有则使用用户名
            lastMessage: privateChatInfo.lastMessageContent, // 暂无消息历史
            time: new Date(privateChatInfo.updatedAt).toLocaleString(), // 格式化时间
            unread: privateChatInfo.unreadCount
          };
        }
        return null;
      }).filter(Boolean) as DisplayChat[];
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
  const selectedChatArray = chatInfos.value.filter((chat) => chat.info.chatId === selectedChatId.value)
  if (selectedChatArray.length != 1) return null
  return selectedChatArray[0]
}

/**
 * 负责处理HomeChatArea中通过WebSocket接收到的聊天消息。
 * 用于根据新消息更新会话列表的最新消息预览和未读数量
 */
async function onReceivedNewChatMessage(data: ChatMessageData) {
  // 在会话列表找到与收到消息data相对应的会话
  const chatsToUpdate = chats.value.filter(chat => chat.id === data.chatId)
  // 如果没找到对应会话，说明创建了新会话，需要从后端重新拉取会话列表
  // 这可以修复用户收到好友请求时不会实时显示出来的问题
  if (chatsToUpdate.length === 0) {
    await loadChatList()
    return
  }
  // 1. 首先更新最新消息预览
  chatsToUpdate.forEach(chat => chat.lastMessage = previewChatMessageData(data))
  // 2. 如果不是当前打开的会话，则其未读消息数量加一
  chatsToUpdate.forEach(chat => {
    if (chat.id !== selectedChatId.value) {
      chat.unread++
    }
  })
  // 上面用forEach可以方便地规避找不到匹配项或找到多个匹配项的问题
}

/**
 * 打开用户信息展示对话框
 * @param userId 要展示的用户ID
 */
async function openUserInfoDialog(userId: number) {
  try {
    const resp = await getUserInfoApi(userId)
    if (resp.data.code === 200) {
      userInfoDialogUser.value = resp.data.data
      showUserInfoDialog.value = true
    } else {
      showFailMessage('', resp.data.msg)
    }
  } catch (e) {
    showFailMessage('', e)
  }
}

/**
 * 删除好友
 * @param userId 要删除的用户ID
 */
async function deleteFriend(userId: number) {
  try {
    // 等待用户确认
    await ElMessageBox.confirm(
      '确定要删除该好友吗？',
      '删除好友',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    // 执行请求
    const resp = await deleteFriendApi({ userId: userId })
    if (resp.data.code === 200) {
      showSuccessfulMessage('删除成功')
      showUserInfoDialog.value = false
    } else {
      showFailMessage('', resp.data.msg)
    }
  } catch (e) {
    // 用户取消
    if (e === 'cancel') {
      ElMessage({
        message: '你取消了操作',
        type: 'info'
      })
    } else {
      showFailMessage('', e)
    }
  }
}
</script>

<template>
  <main class="chat-container">
    <!-- 左侧会话列表 -->
    <HomeChatList :chats="chats" :selected-chat-id="selectedChatId" @select-chat="handleSelectChat"
      @update-chatlist="loadChatList()" @show-user-info-dialog="openUserInfoDialog" />
    <HomeChatArea :selectedChat="selectedChatId" :chat-info="getCurrentChatInfo()"
      @update_chatlist="onReceivedNewChatMessage" />
    <el-dialog v-model="showUserInfoDialog" title="个人信息" width="300px" center>
      <div class="user-detail">
        <p><strong>用户名：</strong>{{ userInfoDialogUser?.username }}</p>
        <p><strong>昵称：</strong>{{ userInfoDialogUser?.nickname || '暂无昵称' }}</p>
        <!-- <p><strong>个人描述：</strong>{{ userInfoDialogUser?.description.trim().length === 0 ? '这个人很懒，什么也没留下' :
          userInfoDialogUser?.description.trim() }}</p> -->
        <p><strong>注册时间：</strong>{{ dayjs(userInfoDialogUser?.createdAt).format('L') }}</p>
        <p v-if="userInfoDialogUser?.userId !== userData.value?.userId"><strong>{{ userInfoDialogUser?.isFriend ?
          '这个人是你的好友' : '这个人还不是你的好友' }}</strong></p>
        <p v-if="userInfoDialogUser?.isFriend">
          <ElButton type="danger" @click="deleteFriend(userInfoDialogUser?.userId)">删除好友</ElButton>
        </p>
      </div>
    </el-dialog>
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
