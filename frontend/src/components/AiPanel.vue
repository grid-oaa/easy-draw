<template>
  <div class="panel">
    <div class="topbar">
      <div class="brand">
        <div class="logo">ED</div>
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
          <div class="bubble">
            <template v-if="m.role === 'ai' && m.code">
              <details class="codeToggle">
                <summary>Show diagram code</summary>
                <pre class="codeBlock">{{ m.code }}</pre>
              </details>
            </template>
            <template v-else>{{ m.text }}</template>
          </div>
          <div class="msgActions">
            <button class="iconBtn" type="button" title="Copy" @click="copy(m.code || m.text)">
              <i class="el-icon-document-copy" />
            </button>
            <button
              v-if="m.role === 'ai' && m.code"
              class="iconBtn"
              type="button"
              title="Insert"
              @click="insertCode(m.code)"
            >
              <i class="el-icon-plus" />
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
        placeholder="请描述你要生成的图表..."
        @keydown.native.enter.exact.prevent="send"
      />
      <div class="composerBar">
        <input ref="file" class="file" type="file" @change="onFile" />
        <el-button class="clearBtn" @click="confirmClear">清空</el-button>
        <el-button class="sendBtn" type="primary" :loading="aiBusy" @click="send">
          <i class="el-icon-position" />
          Send
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex';

import { generateDiagram, editMermaidDiagram } from '@/api/ai';
import { EMPTY_DRAWIO_XML } from '@/shared/drawio';

function makeId() {
  return `m_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}

const CHAT_STORAGE_KEY = 'easy_draw_ai_messages_v1';
const MERMAID_STORAGE_KEY = 'easy_draw_last_mermaid_v1';
const EDIT_HINTS = [
  '修改',
  '调整',
  '优化',
  '完善',
  '补充',
  '增加',
  '删除',
  '替换',
  '改',
  '更新',
  '修正',
  'edit',
  'update',
  'modify',
  'add',
  'remove',
];

export default {
  name: 'AiPanel',
  data() {
    return {
      prompt: '',
      mode: 'diagram',
      shapeMode: false,
      messages: [],
      lastMermaid: '',
    };
  },
  created() {
    this.loadMessages();
    this.loadLastMermaid();
  },
  computed: {
    ...mapState({
      aiBusy: (state) => state.editor.aiBusy,
      aiResetNonce: (state) => state.ai.resetNonce,
    }),
  },
  watch: {
    aiResetNonce() {
      this.prompt = '';
      this.messages = [];
      this.saveMessages();
      this.lastMermaid = '';
      this.saveLastMermaid();
    },
  },
  methods: {
    ...mapMutations(['setAiBusy', 'setLastExplain', 'requestCanvasAction', 'setDirty']),
    noop() {},
    about() {
      this.$message.info('About: AI + draw.io prototype (frontend demo)');
    },
    setMode(command) {
      this.mode = command;
    },
    prefill() {
      this.prompt = 'Convert the diagram to AWS style and auto layout.';
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
      if (file) this.$message.info(`Selected file: ${file.name} (placeholder)`);
      if (e && e.target) e.target.value = '';
    },
    append(role, text) {
      this.messages.push({ id: makeId(), role, text });
      this.saveMessages();
      this.$nextTick(() => this.scrollToBottom());
    },
    appendCode(role, content) {
      const code = content || '';
      this.messages.push({ id: makeId(), role, text: '', code });
      this.lastMermaid = code;
      this.saveLastMermaid();
      this.saveMessages();
      this.$nextTick(() => this.scrollToBottom());
    },
    shouldUseEdit(prompt) {
      if (!prompt) return false;
      const lower = prompt.toLowerCase();
      return EDIT_HINTS.some((hint) => lower.includes(hint.toLowerCase()));
    },
    insertCode(code) {
      if (!code) return;
      this.requestCanvasAction({
        type: 'insert',
        payload: { format: 'mermaid', data: code },
      });
    },
    confirmClear() {
      this.$confirm('确认清空聊天记录和画布内容吗？', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          this.messages = [];
          this.lastMermaid = '';
          this.saveMessages();
          this.saveLastMermaid();
          this.requestCanvasAction({ type: 'load', payload: { xml: EMPTY_DRAWIO_XML } });
          this.setDirty(false);
        })
        .catch(() => {});
    },
    scrollToBottom() {
      const el = this.$refs.chat;
      if (!el) return;
      el.scrollTop = el.scrollHeight;
    },
    loadMessages() {
      try {
        const raw = localStorage.getItem(CHAT_STORAGE_KEY);
        if (!raw) {
          this.messages = [];
          return;
        }
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed)) {
          this.messages = parsed;
        }
      } catch {
        // Ignore invalid localStorage content.
      }
    },
    loadLastMermaid() {
      try {
        const raw = localStorage.getItem(MERMAID_STORAGE_KEY);
        if (typeof raw === 'string') {
          this.lastMermaid = raw;
        }
      } catch {
        // Ignore invalid localStorage content.
      }
    },
    saveLastMermaid() {
      try {
        localStorage.setItem(MERMAID_STORAGE_KEY, this.lastMermaid || '');
      } catch {
        // Ignore storage errors.
      }
    },
    saveMessages() {
      try {
        localStorage.setItem(CHAT_STORAGE_KEY, JSON.stringify(this.messages));
      } catch {
        // Ignore storage errors (quota, privacy mode).
      }
    },
    async copy(text) {
      const value = String(text || '');
      if (!value) return;
      try {
        if (navigator.clipboard && navigator.clipboard.writeText) {
          await navigator.clipboard.writeText(value);
          this.$message.success('Copied');
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
        this.$message.success('Copied');
      } catch {
        this.$message.error('Copy failed');
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
        const useEdit = this.lastMermaid && this.shouldUseEdit(value);
        const res = useEdit
          ? await editMermaidDiagram({
              diagramType: '',
              prompt: value,
              mermaid: this.lastMermaid,
            })
          : await generateDiagram({
              language: 'mermaid',
              diagramType: '',
              prompt: value,
            });

        if (!res || !res.content) throw new Error('Empty result');

        this.setLastExplain(res.explain || 'Generated');
        this.appendCode('ai', res.content);

        if (res.language === 'mermaid' && res.content) {
          this.requestCanvasAction({
            type: 'import',
            payload: { format: 'mermaid', data: res.content },
          });
        }
      } catch (e) {
        this.$message.error(e.message || 'AI request failed');
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
.codeToggle summary {
  cursor: pointer;
  font-weight: 600;
  color: #111;
  outline: none;
}
.codeBlock {
  margin: 8px 0 0;
  padding: 8px 10px;
  border-radius: 10px;
  background: #f7f7f7;
  border: 1px solid #e5e5e5;
  font-size: 12px;
  white-space: pre-wrap;
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
.controls {
  display: none;
}
.composerBar {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}
.clearBtn {
  border-radius: 16px;
  padding: 8px 12px;
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
