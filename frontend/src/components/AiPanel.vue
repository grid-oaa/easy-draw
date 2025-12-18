<template>
  <div class="panel">
    <div class="topbar">
      <div class="brand">
        <div class="logo">⌘</div>
        <div class="title">easy draw</div>
      </div>
      <div class="topActions">
        <el-button type="text" icon="el-icon-more" @click="noop" />
      </div>
    </div>

    <div class="content">
      <div class="chat" ref="chat">
        <div
          v-for="m in messages"
          :key="m.id"
          class="msg"
          :class="{ ai: m.role === 'ai', user: m.role === 'user' }"
        >
          <div class="bubble">{{ m.text }}</div>
          <div class="msgActions" v-if="m.role === 'ai'">
            <button class="iconBtn" type="button" title="复制" @click="copy(m.text)">
              <i class="el-icon-document-copy" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="composer">
      <el-input
        v-model="prompt"
        type="textarea"
        :rows="3"
        resize="none"
        placeholder="描述你的示意图或上传文件……"
        @keydown.native.enter.exact.prevent="send"
      />
      <div class="composerBar">
        <input ref="file" class="file" type="file" @change="onFile" />
        <el-button class="sendBtn" type="primary" :loading="aiBusy" @click="send">
          <i class="el-icon-position" />
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex';

import { requestAiPatch } from '@/api/ai';
import { STYLE_PRESET } from '@/shared/constants';

function makeId() {
  return `m_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}

export default {
  name: 'AiPanel',
  data() {
    return {
      prompt: '',
      mode: 'diagram',
      shapeMode: false,
      messages: [
        {
          id: 'm0',
          role: 'ai',
          text: '描述你的需求后，我会把当前 draw.io 的 XML 发给后端，后端返回新的 XML，我将其直接加载回画布。',
        },
      ],
    };
  },
  computed: {
    ...mapState({
      drawioXml: (state) => state.editor.drawioXml,
      aiBusy: (state) => state.editor.aiBusy,
      aiResetNonce: (state) => state.ai.resetNonce,
    }),
  },
  watch: {
    aiResetNonce() {
      this.prompt = '';
      this.messages = [
        {
          id: 'm0',
          role: 'ai',
          text: '描述你的需求后，我会把当前 draw.io 的 XML 发给后端，后端返回新的 XML，我将其直接加载回画布。',
        },
      ];
    },
  },
  methods: {
    ...mapMutations(['setAiBusy', 'setLastExplain', 'setDirty', 'requestCanvasAction']),
    noop() {},
    about() {
      this.$message.info('关于：AI + draw.io 原型（前端示例）');
    },
    setMode(command) {
      this.mode = command;
    },
    prefill() {
      this.prompt = '将当前图转换为 AWS 风格，并自动排版对齐。';
    },
    clear() {
      this.prompt = '';
    },
    exportSvg() {
      this.requestCanvasAction({ type: 'export', payload: { format: 'svg' } });
    },
    triggerUpload() {
      const el = this.$refs.file;
      if (el && el.click) el.click();
    },
    onFile(e) {
      const file = e && e.target && e.target.files ? e.target.files[0] : null;
      if (file) this.$message.info(`已选择文件：${file.name}（占位）`);
      if (e && e.target) e.target.value = '';
    },
    append(role, text) {
      this.messages.push({ id: makeId(), role, text });
      this.$nextTick(() => this.scrollToBottom());
    },
    scrollToBottom() {
      const el = this.$refs.chat;
      if (!el) return;
      el.scrollTop = el.scrollHeight;
    },
    async copy(text) {
      const value = String(text || '');
      if (!value) return;
      try {
        if (navigator.clipboard && navigator.clipboard.writeText) {
          await navigator.clipboard.writeText(value);
          this.$message.success('已复制');
          return;
        }
      } catch {
        // fall through
      }

      try {
        const textarea = document.createElement('textarea');
        textarea.value = value;
        textarea.setAttribute('readonly', 'readonly');
        textarea.style.position = 'fixed';
        textarea.style.top = '-9999px';
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        this.$message.success('已复制');
      } catch {
        this.$message.error('复制失败');
      }
    },
    async send() {
      if (this.aiBusy) return;
      const value = this.prompt.trim();
      if (!value) return;

      this.prompt = '';
      this.append('user', value);

      this.setAiBusy(true);
      try {
        const res = await requestAiPatch({
          format: 'drawio',
          prompt: value,
          drawioXml: this.drawioXml,
          stylePreset: STYLE_PRESET.AWS,
          mode: this.mode,
        });

        const nextXml = res && typeof res.drawioXml === 'string' ? res.drawioXml : '';
        if (!nextXml) throw new Error('AI 未返回 drawioXml');

        const explain = (res && res.explain) || '已生成，可一键应用到画布。';
        this.setLastExplain(explain);
        this.requestCanvasAction({ type: 'load', payload: { xml: nextXml } });
        this.setDirty(true);
        this.append('ai', explain);
      } catch (e) {
        this.$message.error(e.message || 'AI 请求失败');
      } finally {
        this.setAiBusy(false);
      }
    },
  },
};
</script>

<style scoped>
.panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.topbar {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  border-bottom: 1px solid #f0f0f0;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #111;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}
.title {
  font-weight: 700;
  color: #111;
}
.link {
  border: none;
  background: transparent;
  color: #666;
  padding: 0;
  cursor: pointer;
  font-size: 12px;
}
.warn {
  color: #ffb100;
}
.topActions {
  display: flex;
  align-items: center;
  gap: 6px;
}
.divider {
  width: 1px;
  height: 18px;
  background: #e9e9e9;
  margin: 0 4px;
}

.content {
  flex: 1;
  min-height: 0;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
}
.headerRow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.statusPill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid #f0f0f0;
  background: #f7f7f7;
  border-radius: 16px;
  cursor: pointer;
}
.dot {
  width: 18px;
  height: 18px;
  border-radius: 9px;
  background: #e9e9e9;
}
.label {
  font-size: 13px;
  color: #333;
}
.ok {
  font-size: 13px;
  color: #16a34a;
  font-weight: 600;
}
.caret {
  color: #666;
}
.quickBtn {
  padding: 10px 12px;
  border-radius: 16px;
  border: none;
  background: #1f2937;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

.chat {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 4px 0;
}
.msg {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}
.msg.user {
  align-items: flex-end;
}
.bubble {
  max-width: 88%;
  padding: 10px 12px;
  border-radius: 16px;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  border: 1px solid #f0f0f0;
  background: #fff;
  color: #111;
}
.msg.user .bubble {
  background: #1f2937;
  color: #fff;
  border: none;
}
.msgActions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 6px;
  color: #666;
}
.iconBtn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 2px;
  color: #666;
}

.composer {
  padding: 10px 12px 12px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}
.composerBar {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}
.toggleWrap {
  padding-left: 4px;
}
.spacer {
  flex: 1;
}
.file {
  display: none;
}
.sendBtn {
  border-radius: 16px;
  padding: 9px 14px;
}
</style>
