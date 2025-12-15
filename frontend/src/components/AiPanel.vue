<template>
  <div class="panel">
    <div class="header">
      <div class="title">下一个 AI Drawio</div>
      <el-button type="text" icon="el-icon-setting" @click="noop" />
    </div>

    <div class="cta">
      <el-button type="primary" plain size="small" @click="prefill"
        >用 AWS 风格复制这个过程</el-button
      >
    </div>

    <div class="status">
      <el-tag size="mini">生成图</el-tag>
      <el-tag size="mini" type="success">完整</el-tag>
    </div>

    <div class="chat">
      <div class="msg ai">输入需求，我会返回可应用到画布的变更指令（占位）。</div>
      <div class="msg user" v-if="lastUser">{{ lastUser }}</div>
    </div>

    <div class="composer">
      <el-input
        type="textarea"
        :rows="4"
        placeholder="描述你的示意图或上传文件……"
        v-model="prompt"
      />
      <div class="actions">
        <el-button size="mini" @click="clear">清空</el-button>
        <el-button type="primary" size="mini" @click="send">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AiPanel',
  data() {
    return {
      prompt: '',
      lastUser: '',
    };
  },
  methods: {
    noop() {},
    prefill() {
      this.prompt = '将当前图转换为 AWS 风格，并自动排版对齐。';
    },
    clear() {
      this.prompt = '';
    },
    send() {
      const value = this.prompt.trim();
      if (!value) return;
      this.lastUser = value;
      this.prompt = '';
      this.$message.info('已发送（占位实现）');
    },
  },
};
</script>

<style scoped>
.panel {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title {
  font-weight: 700;
}
.cta {
  display: flex;
  justify-content: flex-end;
}
.status {
  display: flex;
  gap: 8px;
}
.chat {
  flex: 1;
  min-height: 180px;
  border: 1px solid #e9e9e9;
  border-radius: 12px;
  padding: 10px;
  background: #fafafa;
  overflow: auto;
}
.msg {
  padding: 8px 10px;
  border-radius: 10px;
  margin-bottom: 8px;
  font-size: 13px;
  line-height: 1.4;
}
.msg.ai {
  background: #fff;
  border: 1px solid #e9e9e9;
}
.msg.user {
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  align-self: flex-end;
}
.composer {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}
.actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
