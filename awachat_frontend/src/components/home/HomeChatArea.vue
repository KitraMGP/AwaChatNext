<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue'
import { useUserDataStore } from '@/stores/userDataStore'
import { websocketService } from '@/services/websocketService'
import { ChatType, ChatMessageType, type ChatMessageData, type WebSocketMessage, type RequestChatHistoryData, type ReadAcknowledgeData, MESSAGE_TYPE, type ChatHistoryData, type CompoundMessageContent, type FriendRequestMessageContent, type TextMessageContent } from '@/dto/websocket'
import type { ChatInfo, PrivateChatInfo } from '@/dto/chat';
import Loading from 'element-plus/es/components/loading/src/service.mjs';
import { acceptFriendRequestApi } from '@/services/userApi';
import { dayjs, ElMessage } from 'element-plus';

const props = defineProps<{
  selectedConversation: number | null,
  chatInfo: ChatInfo<PrivateChatInfo> | null
}>()

const userData = useUserDataStore()
const messages = ref<ChatMessageData[]>([])
const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const isLoadingHistory = ref(false)
const noMoreMessages = ref(false)
const oldestMessageId = ref<number | null>(null)

// 监听选中的会话变化
watch(() => props.selectedConversation, async (newChatId) => {
  if (newChatId !== null) {
    // 清空消息列表和状态
    messages.value = []
    oldestMessageId.value = null
    noMoreMessages.value = false

    // 加载最新消息
    await loadLatestMessages(newChatId)
  }
}, { immediate: true })

// 加载最新消息
async function loadLatestMessages(chatId: number) {
  if (!userData.value || !props.selectedConversation) return

  const requestData: RequestChatHistoryData = {
    chatType: ChatType.PRIVATE,
    chatId: chatId
  }

  // 发送请求获取历史消息
  const message: WebSocketMessage<RequestChatHistoryData> = {
    type: MESSAGE_TYPE.REQUEST_CHAT_HISTORY,
    data: requestData
  }

  websocketService.sendMessage(message)
}

// 加载更早的消息
async function loadEarlierMessages() {
  if (!props.selectedConversation || isLoadingHistory.value || noMoreMessages.value) return

  isLoadingHistory.value = true

  const requestData: RequestChatHistoryData = {
    chatType: ChatType.PRIVATE,
    chatId: props.selectedConversation,
    lastMessageId: oldestMessageId.value || undefined
  }

  // 发送请求获取历史消息
  const message: WebSocketMessage<RequestChatHistoryData> = {
    type: MESSAGE_TYPE.REQUEST_CHAT_HISTORY,
    data: requestData
  }

  websocketService.sendMessage(message)

  // 5秒超时保护
  setTimeout(() => {
    isLoadingHistory.value = false
  }, 5000)
}

// 处理滚动事件，检测是否需要加载更多消息
function handleScroll(event: Event) {
  const target = event.target as HTMLElement
  if (target.scrollTop <= 50 && !isLoadingHistory.value && !noMoreMessages.value) {
    loadEarlierMessages()
  }
}

// 发送消息
function sendMessage() {
  const recepientId = getRecipientId()
  if (!messageInput.value.trim() || !props.selectedConversation || !userData.value || !recepientId) return

  const chatMessage: ChatMessageData = {
    chatType: ChatType.PRIVATE,
    msgType: ChatMessageType.TEXT,
    chatId: props.selectedConversation,
    from: userData.value.userId,
    to: recepientId,
    content: { content: messageInput.value.trim() }
  }

  websocketService.sendChatMessage(chatMessage)
  messageInput.value = ''
}

// 获取接收者ID
function getRecipientId(): number | null {
  if (!props.chatInfo) return null
  return props.chatInfo?.info.userId
}

// 注册消息处理器
onMounted(() => {
  // 注册历史消息处理器
  // TODO 处理这里的问题
  websocketService.registerMessageHandler(MESSAGE_TYPE.REQUEST_CHAT_HISTORY, (data: ChatHistoryData) => {
    if (data.history.length === 0) {
      noMoreMessages.value = true
      isLoadingHistory.value = false
      return
    }

    // 保存最早消息的ID（遍历然后取最小值）
    const earliestMessage = data.history.reduce((prev, curr) =>
      (curr.id && prev.id && curr.id < prev.id) ? curr : prev, data.history[0])

    if (earliestMessage && earliestMessage.id) {
      oldestMessageId.value = earliestMessage.id
    }

    // 将新消息添加到列表开头
    const scrollHeight = messagesContainer.value?.scrollHeight
    const scrollTop = messagesContainer.value?.scrollTop

    messages.value = [...data.history, ...messages.value]

    // 维持滚动位置
    nextTick(() => {
      if (messagesContainer.value && scrollHeight && scrollTop) {
        const newScrollHeight = messagesContainer.value.scrollHeight
        messagesContainer.value.scrollTop = scrollTop + (newScrollHeight - scrollHeight)
      }
      isLoadingHistory.value = false
    })

    // 发送已读回执
    sendReadAcknowledge()
  })

  // 注册聊天消息处理器
  websocketService.registerMessageHandler(MESSAGE_TYPE.CHAT, (data: ChatMessageData) => {
    // 只处理当前会话的消息
    if (data.chatId === props.selectedConversation) {
      messages.value.push(data)

      // 滚动到底部
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })

      // 发送已读回执
      sendReadAcknowledge()
    }
  })
})

// 发送已读回执
function sendReadAcknowledge() {
  if (!props.selectedConversation || messages.value.length === 0) return

  // 获取最新消息的ID
  const latestMessage = messages.value[messages.value.length - 1]
  if (!latestMessage.id) return

  const ackMessage: WebSocketMessage<ReadAcknowledgeData> = {
    type: MESSAGE_TYPE.ACK,
    data: {
      chatType: ChatType.PRIVATE,
      chatId: props.selectedConversation,
      lastMessageId: latestMessage.id
    }
  }

  websocketService.sendMessage(ackMessage)
}

// 判断消息是否由当前用户发送
function isOwnMessage(message: ChatMessageData): boolean {
  return message.from === userData.value?.userId
}

// 接受好友请求
async function acceptFriendRequest(from: number) {
  try {
    const resp = await acceptFriendRequestApi({ originUserId: from })
    if (resp.data.code == 200) {
      ElMessage({
        message: '已接受好友请求',
        type: 'success'
      })
    } else {
      ElMessage({
        message: resp.data.msg,
        type: 'warning'
      })
      console.error(resp.data.msg)
    }
  } catch (e) {
    console.error(e)
    ElMessage({
      message: '请求错误',
      type: 'warning'
    })
  }
}

// 格式化消息时间
function formatMessageTime(date: string | Date): string {
  if (!date) return ''
  //const messageDate = typeof date === 'string' ? new Date(date) : date
  return dayjs(date).format('L LT')
}
</script>

<template>
  <!-- 右侧聊天界面 -->
  <div class="chat-area">
    <template v-if="props.selectedConversation !== null">
      <!-- 聊天消息区域 -->
      <div class="chat-messages" ref="messagesContainer" @scroll="handleScroll">
        <div v-if="isLoadingHistory" class="loading-history">
          <el-icon class="is-loading">
            <Loading />
          </el-icon> 加载更多消息...
        </div>
        <div v-if="noMoreMessages" class="no-more-messages">
          没有更多消息了
        </div>

        <!-- 消息列表 -->
        <template v-for="(message, index) in messages" :key="message.id || index">
          <!-- 消息气泡 -->
          <div class="message" :class="{ 'own-message': isOwnMessage(message) }">
            <div class="message-content">
              <!-- 文本消息 -->
              <div v-if="message.msgType === ChatMessageType.TEXT" class="text-message">
                {{ (message.content as TextMessageContent).content }}
              </div>

              <!-- 好友请求消息 -->
              <div v-else-if="message.msgType === ChatMessageType.FRIEND_REQUEST" class="friend-request-message">
                <template v-if="isOwnMessage(message)">
                  <div v-if="(message.content as FriendRequestMessageContent).isAccepted">
                    你们已经成为好友
                  </div>
                  <div v-else>
                    你发送了好友请求
                  </div>
                </template>
                <template v-else>
                  <div v-if="(message.content as FriendRequestMessageContent).isAccepted">
                    你们已经成为好友
                  </div>
                  <div v-else>
                    <div>收到好友请求</div>
                    <el-button size="small" type="primary" @click="acceptFriendRequest(message.from)">
                      接受
                    </el-button>
                  </div>
                </template>
              </div>

              <!-- 复合消息 -->
              <div v-else-if="message.msgType === ChatMessageType.COMPOUND" class="compound-message">
                <div v-for="(part, partIndex) in (message.content as CompoundMessageContent).parts" :key="partIndex">
                  <div v-if="part.type === 'text'" class="text-part">
                    {{ part.content }}
                  </div>
                  <div v-else-if="part.type === 'image'" class="image-part">
                    <img :src="part.content" alt="图片" />
                  </div>
                </div>
              </div>
            </div>
            <div class="message-time">
              {{ formatMessageTime(message.sentAt || new Date()) }}
            </div>
          </div>
        </template>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <el-input v-model="messageInput" type="textarea" :rows="3" placeholder="请输入消息..."
          @keyup.enter.ctrl="sendMessage" />
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </template>
    <div v-else class="empty-chat">
      <p>选择一个会话，开始聊天</p>
    </div>
  </div>
</template>

<style lang="css" scoped>
/* 右侧聊天区域样式 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.empty-chat {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 16px;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.chat-input {
  padding: 10px;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: flex-end;
}

.chat-input .el-textarea {
  flex: 1;
  margin-right: 10px;
}

/* 消息样式 */
.message {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}

.message-content {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 10px;
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 自己发送的消息 */
.own-message {
  align-items: flex-end;
}

.own-message .message-content {
  background-color: #409eff;
  color: white;
}

/* 他人发送的消息 */
.message:not(.own-message) {
  align-items: flex-start;
}

.message:not(.own-message) .message-content {
  background-color: #dee4e4;
  color: #303133;
}

/* 加载历史消息提示 */
.loading-history,
.no-more-messages {
  text-align: center;
  padding: 10px;
  color: #909399;
  font-size: 14px;
}

/* 好友请求消息样式 */
.friend-request-message {
  background-color: transparent;
  /* 将背景色设置为透明 */
  padding: 5px;
  border-radius: 5px;
  margin-top: 5px;
}
</style>
