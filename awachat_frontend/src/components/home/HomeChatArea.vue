<script setup lang="ts">
import { ref, watch, onMounted, nextTick, reactive } from 'vue'
import { useUserDataStore } from '@/stores/userDataStore'
import { websocketService } from '@/services/websocketService'
import { ChatType, ChatMessageType, type ChatMessageData, type WebSocketMessage, type RequestChatHistoryData, type ReadAcknowledgeData, MESSAGE_TYPE, type ChatHistoryData, type CompoundMessageContent, type FriendRequestMessageContent, type TextMessageContent, type CompoundMessagePart, CompoundMessagePartType } from '@/dto/websocket'
import type { ChatInfo, PrivateChatInfo } from '@/dto/chat';
import { acceptFriendRequestApi } from '@/services/userApi';
import { dayjs, ElMessage } from 'element-plus';
import { getImageUploadUrlApi, getImageDownloadUrlApi } from '@/services/imageApi';

const props = defineProps<{
  selectedChat: number | null,
  chatInfo: ChatInfo<PrivateChatInfo> | null
}>()
const emit = defineEmits(['update_chatlist'])

const userData = useUserDataStore()
const messages = ref<ChatMessageData[]>([])
const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null) // 引用聊天信息区域HTML元素，用于检测滚动位置
const isLoadingHistory = ref(false) // 决定是否显示加载动画，在拉取历史消息的时候使用（目前功能损坏不能使用，TODO）
const noMoreMessages = ref(false)
const oldestMessageId = ref<number | null>(null)

const editor = ref<HTMLElement | null>(null) // 引用消息输入区HTML元素
const messageParts = ref<CompoundMessagePart[]>([]) // 消息输入区的消息部件列表

// 监听选中的会话切换操作
watch(() => props.selectedChat, async (newChatId) => {
  if (newChatId !== null) {
    // 清空消息列表和状态
    messages.value = []
    oldestMessageId.value = null
    noMoreMessages.value = false

    // 加载最新消息
    await loadLatestMessages()
  }
}, { immediate: true }) // immediate 设置为 true，可以在监听器创建时立即执行一次操作

/**
 * 加载最新消息
 */
async function loadLatestMessages() {
  if (!userData.value || !props.selectedChat) return
  console.log("开始加载最新消息")
  isLoadingHistory.value = true
  const requestData: RequestChatHistoryData = {
    chatType: ChatType.PRIVATE,
    chatId: props.selectedChat
  }

  // 发送请求获取历史消息
  const message: WebSocketMessage<RequestChatHistoryData> = {
    type: MESSAGE_TYPE.REQUEST_CHAT_HISTORY,
    data: requestData
  }

  websocketService.sendMessage(message)
}

/**
 * 加载历史消息。应在打开会话或滚动到顶部时调用。
 */
async function loadEarlierMessages() {
  if (!props.selectedChat || isLoadingHistory.value || noMoreMessages.value) return

  console.log("开始加载更早的消息")
  isLoadingHistory.value = true

  const requestData: RequestChatHistoryData = {
    chatType: ChatType.PRIVATE,
    chatId: props.selectedChat,
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
    if (isLoadingHistory.value) {
      isLoadingHistory.value = false
    }
  }, 5000)
}

/**
 * 滚动事件处理器。当滚动到顶端时，执行加载历史消息操作。
 */
function handleScroll(event: Event) {
  const target = event.target as HTMLElement
  if (target.scrollTop === 0 && !isLoadingHistory.value && !noMoreMessages.value) {
    loadEarlierMessages()
  }
}

/**
 * 获取私聊中消息接收者用户ID
 */
function getRecipientId(): number | null {
  if (!props.chatInfo) return null
  return props.chatInfo?.info.userId
}

// 注册消息处理器
onMounted(() => {
  // 注册历史消息处理器
  // TODO 处理这里的问题
  websocketService.registerMessageHandler(MESSAGE_TYPE.REQUEST_CHAT_HISTORY, (data: ChatHistoryData) => {
    console.log('正在接收历史消息')
    if (data.history.length === 0) {
      console.log('已拉取所有历史消息')
      noMoreMessages.value = true
      isLoadingHistory.value = false
      return
    }

    // // 保存最早消息的ID（遍历然后取最小值）
    // const earliestMessage = data.history.reduce((prev, curr) =>
    //   (curr.id && prev.id && curr.id < prev.id) ? curr : prev, data.history[0])

    // if (earliestMessage && earliestMessage.id) {
    //   oldestMessageId.value = earliestMessage.id
    // }

    // 更新最早消息ID
    if (!data.history[0].id) {
      isLoadingHistory.value = false
      return
    }
    oldestMessageId.value = data.history[0].id

    // 将新消息添加到列表开头
    const scrollHeight = messagesContainer.value?.scrollHeight
    const scrollTop = messagesContainer.value?.scrollTop

    messages.value = [...data.history, ...messages.value]

    // 维持滚动位置
    nextTick(() => {
      if (messagesContainer.value && scrollHeight !== undefined && scrollTop !== undefined) {
        const newScrollHeight = messagesContainer.value.scrollHeight
        messagesContainer.value.scrollTop = scrollTop + (newScrollHeight - scrollHeight)
      }
      console.log('已接收并处理历史消息')
    })

    isLoadingHistory.value = false

    // 发送已读回执
    sendReadAcknowledge()
  })

  // 注册聊天消息处理器
  websocketService.registerMessageHandler(MESSAGE_TYPE.CHAT, (data: ChatMessageData) => {
    // 处理当前会话的消息
    if (data.chatId === props.selectedChat) {
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

    // 更新会话列表中的数据
    // TODO 更新会话列表
    // 将收到的消息发送给父组件，通知其更新会话列表
    emit('update_chatlist', data)
  })
})

/**
 * 发送已读回执（将前端获取到的最新的消息ID告知后端）
 */
function sendReadAcknowledge() {
  if (!props.selectedChat || messages.value.length === 0) return

  // 获取最新消息的ID
  const latestMessage = messages.value[messages.value.length - 1]
  if (!latestMessage.id) return

  const ackMessage: WebSocketMessage<ReadAcknowledgeData> = {
    type: MESSAGE_TYPE.ACK,
    data: {
      chatType: ChatType.PRIVATE,
      chatId: props.selectedChat,
      lastMessageId: latestMessage.id
    }
  }

  websocketService.sendMessage(ackMessage)
}


/**
 * 消息输入框按下回车键的处理器。若检测到按下Shift+Enter，则不进行发送。
 */
function handleChatInputEnter(event: KeyboardEvent) {
  if (!event.shiftKey) {
    sendMessage()
  }
}

/**
 * 将图片上传到MinIO
 * @param file 要上传的图片文件
 * @returns 上传成功后的图片路径(key)
 */
async function uploadImageToMinio(file: File): Promise<string | null> {
  if (!props.selectedChat) {
    ElMessage.error('请先选择一个聊天')
    return null
  }

  try {
    // 1. 获取上传链接
    const uploadUrlResponse = await getImageUploadUrlApi({
      chatId: props.selectedChat,
      fileSize: file.size,
      mimeType: file.type
    })

    if (uploadUrlResponse.data.code !== 200) {
      ElMessage.error(`获取上传链接失败: ${uploadUrlResponse.data.msg}`)
      return null
    }

    const { url, key } = uploadUrlResponse.data.data

    // 2. 上传图片到MinIO
    const uploadResponse = await fetch(url, {
      method: 'PUT',
      body: file,
      headers: {
        'Content-Type': file.type
      }
    })

    if (!uploadResponse.ok) {
      ElMessage.error(`上传图片失败: ${uploadResponse.statusText}`)
      return null
    }

    return key
  } catch (error) {
    console.error('上传图片错误:', error)
    ElMessage.error('上传图片失败，请重试')
    return null
  }
}

/**
 * 处理编辑器输入事件，解析编辑区内容并更新messageParts
 */
function handleInput() {
  if (!editor.value) return

  const newParts: CompoundMessagePart[] = []

  // 遍历编辑器中的所有节点
  editor.value.childNodes.forEach((node) => {
    if (node.nodeType === Node.TEXT_NODE) {
      // 处理文本节点
      const text = node.textContent?.trim()
      if (text) {
        newParts.push({
          type: CompoundMessagePartType.TEXT,
          content: text
        })
      }
    } else if (node.nodeType === Node.ELEMENT_NODE) {
      const element = node as HTMLElement

      // 处理图片元素
      if (element.tagName === 'IMG') {
        const imgElement = element as HTMLImageElement
        const src = imgElement.getAttribute('data-key')
        if (src) {
          newParts.push({
            type: CompoundMessagePartType.IMAGE,
            content: src
          })
        }
      } else if (element.tagName === 'BR') {
        // 处理换行
        newParts.push({
          type: CompoundMessagePartType.TEXT,
          content: '\n'
        })
      } else {
        // 处理其他元素内的文本
        const text = element.textContent?.trim()
        if (text) {
          newParts.push({
            type: CompoundMessagePartType.TEXT,
            content: text
          })
        }
      }
    }
  })

  // 合并连续的文本部分
  const mergedParts: CompoundMessagePart[] = []
  for (const part of newParts) {
    if (part.type === CompoundMessagePartType.TEXT &&
      mergedParts.length > 0 &&
      mergedParts[mergedParts.length - 1].type === CompoundMessagePartType.TEXT) {
      // 合并连续的文本
      mergedParts[mergedParts.length - 1].content += part.content
    } else {
      mergedParts.push(part)
    }
  }

  // 更新消息部件列表
  messageParts.value = mergedParts
}

/**
 * 插入图片到编辑区
 */
async function insertImage(image: File) {
  if (!editor.value) return

  // 显示上传中的占位图
  const placeholder = document.createElement('img')
  placeholder.src = URL.createObjectURL(image)
  placeholder.style.maxWidth = '200px'
  placeholder.style.maxHeight = '200px'
  placeholder.style.opacity = '0.5' // 半透明表示正在上传
  editor.value.appendChild(placeholder)

  // 上传图片到MinIO
  const key = await uploadImageToMinio(image)

  if (key) {
    // 上传成功，更新图片元素
    placeholder.style.opacity = '1'
    placeholder.setAttribute('data-key', key)

    // 触发输入事件更新messageParts
    handleInput()
  } else {
    // 上传失败，移除占位图
    placeholder.remove()
  }
}

/**
 * 处理编辑器粘贴图片事件
 */
async function handlePaste(e: ClipboardEvent) {
  if (!e.clipboardData || !editor.value) return

  // 阻止默认粘贴行为
  e.preventDefault()

  const items = e.clipboardData.items
  let hasHandledItem = false

  for (const item of items) {
    // 处理粘贴的图片
    if (item.type.startsWith('image/')) {
      hasHandledItem = true
      const file = item.getAsFile()
      if (file) {
        await insertImage(file)
      }
    }
  }

  // 如果没有处理任何图片，则作为文本粘贴
  if (!hasHandledItem) {
    const text = e.clipboardData.getData('text/plain')
    if (text) {
      document.execCommand('insertText', false, text)
    }
  }
}

// 修改sendMessage函数，支持发送复合消息
function sendMessage() {
  const recepientId = getRecipientId()
  if (!props.selectedChat || !userData.value || !recepientId) return

  // 确保消息不为空
  if (messageParts.value.length === 0) {
    const textContent = editor.value?.textContent?.trim()
    if (!textContent) return

    // 如果只有纯文本，创建文本消息部件
    messageParts.value = [{
      type: CompoundMessagePartType.TEXT,
      content: textContent
    }]
  }

  // 如果只有一个文本部件，使用普通文本消息
  if (messageParts.value.length === 1 && messageParts.value[0].type === CompoundMessagePartType.TEXT) {
    const chatMessage: ChatMessageData = {
      chatType: ChatType.PRIVATE,
      msgType: ChatMessageType.TEXT,
      chatId: props.selectedChat,
      from: userData.value.userId,
      to: recepientId,
      content: { content: messageParts.value[0].content }
    }

    websocketService.sendChatMessage(chatMessage)
  } else {
    // 否则使用复合消息
    const chatMessage: ChatMessageData<CompoundMessageContent> = {
      chatType: ChatType.PRIVATE,
      msgType: ChatMessageType.COMPOUND,
      chatId: props.selectedChat,
      from: userData.value.userId,
      to: recepientId,
      content: { parts: messageParts.value }
    }

    websocketService.sendChatMessage(chatMessage)
  }

  // 清空编辑区和消息部件
  if (editor.value) {
    editor.value.innerHTML = ''
  }
  messageParts.value = []
}

/**
 * 判断消息是否为用户发送的，用于前端渲染
 * @param message 消息数据
 */
function isOwnMessage(message: ChatMessageData): boolean {
  return message.from === userData.value?.userId
}

/**
 * 接受好友请求（告知后端已接受好友请求）
 * @param from 发出好友请求的用户ID
 */
async function acceptFriendRequest(from: number) {
  try {
    const resp = await acceptFriendRequestApi({ originUserId: from })
    if (resp.data.code == 200) {
      ElMessage({
        message: '已接受好友请求',
        type: 'success'
      })
      // 更新消息列表中的好友请求消息
      messages.value.filter(msg => msg.msgType === ChatMessageType.FRIEND_REQUEST).forEach(msg => {
        const content = msg.content as FriendRequestMessageContent
        content.isAccepted = true
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

/**
 * 对日期进行格式化
 */
function formatMessageTime(date: string): string {
  return dayjs(date).format('L LT')
}

// 图片URL映射，键为图片路径，值为下载链接
const imageUrlMap = reactive<Record<string, string>>({});

/**
 * 获取图片的下载链接
 * @param path 图片在MinIO中的路径
 * @returns 图片的下载链接或占位图
 */
function getImageUrl(path: string): string {
  // 如果已经有链接，直接返回
  if (imageUrlMap[path]) {
    return imageUrlMap[path];
  }

  // 设置占位图
  imageUrlMap[path] = 'data:image/svg+xml;charset=utf-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22100%22%20height%3D%22100%22%3E%3Crect%20fill%3D%22%23f2f2f2%22%20width%3D%22100%22%20height%3D%22100%22%2F%3E%3C%2Fsvg%3E';

  // 异步获取图片下载链接
  getImageDownloadUrlApi(path).then(response => {
    if (response.data.code === 200) {
      // 更新链接，由于imageUrlMap是响应式的，视图会自动更新
      imageUrlMap[path] = response.data.data.url;

      // 设置定时器，在链接过期前一分钟刷新
      setTimeout(() => {
        delete imageUrlMap[path]; // 删除过期链接，下次访问时会重新获取
      }, 4 * 60 * 1000); // 假设链接有效期为5分钟，提前1分钟刷新
    } else {
      console.error('获取图片下载链接失败:', response.data.msg);
    }
  }).catch(error => {
    console.error('获取图片下载链接错误:', error);
  });

  return imageUrlMap[path];
}
</script>

<template>
  <!-- 右侧聊天界面 -->
  <div class="chat-area">
    <template v-if="props.selectedChat !== null">
      <!-- 聊天消息区域 -->
      <div class="chat-messages" ref="messagesContainer" @scroll="handleScroll">

        <div v-if="noMoreMessages" class="no-more-messages">
          没有更多消息了
        </div>

        <!-- 消息列表 -->
        <template v-for="(message, index) in messages" :key="message.id || index">
          <!-- 消息气泡 -->
          <div class="message" :class="{ 'own-message': isOwnMessage(message) }">
            <div class="message-content">
              <!-- 文本消息 -->
              <div v-if="message.msgType === ChatMessageType.TEXT">
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
                    <div>请求添加你为好友</div>
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
                    <img :src="getImageUrl(part.content)" alt="图片" />
                  </div>
                </div>
              </div>
            </div>
            <div class="message-time">
              {{ formatMessageTime(message.sentAt || '') }}
            </div>
          </div>
        </template>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <div class="editor-container">
          <div ref="editor" contenteditable="true" @input="handleInput" @paste="handlePaste" class="editor"
            @keyup.enter="handleChatInputEnter"></div>
        </div>
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
  background-color: var(--el-bg-color-page);
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
  border-top: 1px solid var(--el-border-color-darker);
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
  white-space: pre-wrap;
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
  background-color: var(--el-color-info-light-7);
  color: var(--el-text-color-regular);
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

.editor-container {
  position: relative;
  flex: 1;
  margin-right: 10px;
}

.editor {
  width: 100%;
  min-height: 60px;
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 8px 40px 8px 8px;
  background-color: var(--el-fill-color-blank);
}

.editor:focus {
  outline: none;
  border-color: var(--el-color-primary);
}

.image-part img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 4px;
}
</style>
