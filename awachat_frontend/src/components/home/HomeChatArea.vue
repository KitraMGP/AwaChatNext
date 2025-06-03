<script setup lang="ts">
const props = defineProps<{
  selectedConversation: number | null
}>()
</script>

<template>
  <!-- 右侧聊天界面 -->
  <div class="chat-area">
    <template v-if="props.selectedConversation">
      <!-- 这里将来放置聊天内容 -->
      <div class="chat-messages">
        <!-- 聊天消息将在这里显示 -->
      </div>
      <div class="chat-input">
        <!-- 输入框将在这里 -->
        <el-input type="textarea" :rows=3 placeholder="请输入消息..." />
        <el-button type="primary">发送</el-button>
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
  padding: 15px;
  border-top: 1px solid #ebeef5;
  background-color: #fff;
  display: flex;
  align-items: flex-end;
}

.chat-input .el-textarea {
  margin-right: 10px;
  flex: 1;
}

.chat-input .el-button {
  height: 40px;
}
</style>
