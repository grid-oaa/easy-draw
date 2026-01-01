<template>
  <div class="panel">
    <div class="topbar">
      <div class="brand">
        <div class="logo">ED</div>
        <div class="title">easy draw</div>
      </div>
      <div class="topActions">
        <el-button type="text" icon="el-icon-setting" @click="openModelConfig">Model</el-button>
        <el-button type="text" icon="el-icon-more" @click="openStylePreset" />
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
              v-if="m.role === 'ai' && m.code && m.codeType === 'diagram'"
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
      <div class="composerModes">
        <div class="toggleWrap">
          <el-button-group>
            <el-button
              size="mini"
              :type="intentMode === 'generate' ? 'primary' : 'default'"
              @click="intentMode = 'generate'"
            >
              生成
            </el-button>
            <el-button
              size="mini"
              :type="intentMode === 'style' ? 'primary' : 'default'"
              @click="intentMode = 'style'"
            >
              修改
            </el-button>
          </el-button-group>
        </div>
        <div class="toggleWrap">
          <el-button-group>
            <el-button
              size="mini"
              :type="renderMode === 'native' ? 'primary' : 'default'"
              @click="renderMode = 'native'"
            >
              原生图形
            </el-button>
            <el-button
              size="mini"
              :type="renderMode === 'mermaid' ? 'primary' : 'default'"
              @click="renderMode = 'mermaid'"
            >
              Mermaid 数据
            </el-button>
          </el-button-group>
        </div>
      </div>
      <el-input
        v-model="prompt"
        type="textarea"
        :rows="5"
        resize="none"
        placeholder="请描述你要生成的图表..."
        @keydown.native.enter.exact.prevent="send"
      />
      <div class="composerBar">
        <input ref="file" class="file" type="file" @change="onFile" />
        <el-button class="clearBtn" @click="confirmClear">清空</el-button>
        <el-button
          class="sendBtn"
          type="primary"
          :loading="aiBusy"
          :disabled="!hasModelConfig"
          @click="send"
        >
          <i class="el-icon-position" />
          Send
        </el-button>
      </div>
    </div>

    <el-dialog
      title="模型设置"
      :visible.sync="modelConfigVisible"
      width="420px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form label-position="top">
        <div class="formRow">
          <div class="formLabel">厂商</div>
          <el-select v-model="modelVendor" placeholder="选择大模型厂商">
            <el-option label="OpenAI" value="openai" />
          </el-select>
        </div>
        <div v-if="modelVendor" class="formRow">
          <div class="formLabel">Base URL</div>
          <el-input
            v-model="modelConfig.baseUrl"
            placeholder="https://api.openai.com/v1/chat/completions"
          />
        </div>
        <div v-if="modelVendor" class="formRow">
          <div class="formLabel">API Key</div>
          <el-input v-model="modelConfig.apiKey" type="password" show-password />
        </div>
        <div v-if="modelVendor" class="formRow">
          <div class="formLabel">Model</div>
          <el-input v-model="modelConfig.model" placeholder="glm-4.6v-flash" />
        </div>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button class="testBtn" :disabled="testBusy" @click="testModelConfig">
          <i v-if="testBusy" class="el-icon-loading spin" />
          测试
        </el-button>
        <el-button type="primary" @click="saveModelConfig">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="设置"
      :visible.sync="stylePresetVisible"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form label-position="top">
        <div class="formRow">
          <div class="formLabel">绘图风格</div>
          <el-select v-model="stylePresetId" placeholder="选择绘图风格">
            <el-option
              v-for="preset in stylePresets"
              :key="preset.id"
              :label="preset.name"
              :value="preset.id"
            />
          </el-select>
        </div>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" :disabled="!stylePresetId" @click="applyStylePreset">
          保存
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex';

import {
  generateDiagram,
  editMermaidDiagram,
  generateStyleCommand,
  testModelConfig,
} from '@/api/ai';
import { EMPTY_DRAWIO_XML } from '@/shared/drawio';

function makeId() {
  return `m_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}

const CHAT_STORAGE_KEY = 'easy_draw_ai_messages_v1';
const MERMAID_STORAGE_KEY = 'easy_draw_last_mermaid_v1';
const MODEL_CONFIG_KEY = 'easy_draw_model_config_v1';
const STYLE_PRESET_KEY = 'easy_draw_style_preset_v1';
const STYLE_PRESETS = [
  {
    id: 'sky-bold',
    name: '浅蓝加粗',
    target: 'all',
    styles: {
      fillColor: '#E8F3FF',
      strokeColor: '#2F6FEB',
      fontColor: '#0F172A',
      strokeWidth: 2,
      rounded: 1,
    },
  },
  {
    id: 'mint-soft',
    name: '薄荷清爽',
    target: 'all',
    styles: {
      fillColor: '#E9F8F1',
      strokeColor: '#1E8E5A',
      fontColor: '#0F5132',
      strokeWidth: 2,
      rounded: 1,
    },
  },
  {
    id: 'dark-contrast',
    name: '深色对比',
    target: 'all',
    styles: {
      fillColor: '#111827',
      strokeColor: '#F9FAFB',
      fontColor: '#F9FAFB',
      strokeWidth: 3,
      rounded: 1,
      shadow: 1,
    },
  },
  {
    id: 'edges-strong',
    name: '连线加粗',
    target: 'edges',
    operations: {
      strokeWidth: { op: 'increase', value: 2 },
    },
  },
  {
    id: 'mono-minimal',
    name: '极简黑白',
    target: 'all',
    styles: {
      fillColor: '#FFFFFF',
      strokeColor: '#111111',
      fontColor: '#111111',
      strokeWidth: 2,
      rounded: 0,
    },
  },
  {
    id: 'warm-card',
    name: '暖色卡片',
    target: 'all',
    styles: {
      fillColor: '#FFF2E6',
      strokeColor: '#F59E0B',
      fontColor: '#7A3E00',
      strokeWidth: 2,
      rounded: 1,
      shadow: 1,
    },
  },
  {
    id: 'hand-drawn',
    name: '手绘风',
    target: 'all',
    styles: {
      fillColor: '#FFF7E8',
      strokeColor: '#6B4E2E',
      fontColor: '#4B3621',
      strokeWidth: 2,
      rounded: 1,
      dashed: 1,
    },
  },
  {
    id: 'corp-bluegray',
    name: '企业蓝灰',
    target: 'all',
    styles: {
      fillColor: '#E9EEF5',
      strokeColor: '#3B5166',
      fontColor: '#26323D',
      strokeWidth: 2,
      rounded: 0,
    },
  },
  {
    id: 'bluegray-highlight',
    name: '蓝灰高亮',
    target: 'all',
    styles: {
      fillColor: '#3498DB',
      strokeColor: '#2C3E50',
      rounded: 1,
      shadow: 1,
    },
  },
];
const EDIT_HINTS = [
  '修改',
  '调整',
  '优化',
  '完善',
  '补充',
  '增加',
  '删除',
  '替换',
  '更新',
  '修正',
  'edit',
  'update',
  'modify',
  'add',
  'remove',
];
const STYLE_HINTS = [
  '样式',
  '线条',
  '边框',
  '颜色',
  '字体',
  '字号',
  '加粗',
  '变粗',
  '加细',
  '变细',
  '虚线',
  '圆角',
  '阴影',
  '透明',
  '对齐',
  '箭头',
  '选中',
  'stroke',
  'color',
  'font',
  'bold',
  'dashed',
  'rounded',
  'shadow',
  'opacity',
];
const STYLE_TARGET_HINTS = {
  selected: [
    '选中',
    '选中的',
    '已选中',
    '当前',
    '当前选中',
    '所选',
    '所选中',
    '选定',
    '已选',
    '被选中',
    '选择',
    '选择的',
  ],
  edges: ['线段', '线条', '连线', '连接线', '边', '边线', '关系线', '路径', '连边'],
  vertices: ['节点', '顶点', '图形', '形状', '块', '元素', '组件', '对象', '单元'],
  all: ['所有', '全部', '全部的', '全局', '整体', '全部元素', '全部对象'],
};

export default {
  name: 'AiPanel',
  data() {
    return {
      prompt: '',
      mode: 'diagram',
      intentMode: 'generate',
      renderMode: 'native',
      stylePresetId: '',
      stylePresets: STYLE_PRESETS,
      messages: [],
      lastMermaid: '',
      modelConfigVisible: false,
      modelVendor: '',
      stylePresetVisible: false,
      // 模型连通性测试中的按钮状态
      testBusy: false,
      modelConfig: {
        baseUrl: '',
        apiKey: '',
        model: '',
        provider: '',
      },
    };
  },
  created() {
    this.loadMessages();
    this.loadLastMermaid();
    this.loadModelConfig();
    this.loadStylePreset();
  },
  computed: {
    ...mapState({
      aiBusy: (state) => state.editor.aiBusy,
      aiResetNonce: (state) => state.ai.resetNonce,
      drawioXml: (state) => state.editor.drawioXml,
      canvasDirty: (state) => state.editor.dirty,
    }),
    hasModelConfig() {
      return Boolean(this.modelConfig.baseUrl && this.modelConfig.apiKey);
    },
    hasCanvasContent() {
      if (this.canvasDirty) return true;
      if (!this.drawioXml) return false;
      return this.drawioXml.trim() !== '' && this.drawioXml !== EMPTY_DRAWIO_XML;
    },
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
    openModelConfig() {
      this.modelConfigVisible = true;
    },
    getEffectiveModelConfig() {
      return {
        ...this.modelConfig,
        provider: this.modelVendor || this.modelConfig.provider,
      };
    },
    testModelConfig() {
      if (this.testBusy) return;
      if (!this.modelVendor) {
        this.$message.warning('请选择模型厂商');
        return;
      }
      if (!this.modelConfig.baseUrl || !this.modelConfig.apiKey) {
        this.$message.warning('请填写 Base URL 和 API Key');
        return;
      }
      const payload = {
        modelConfig: this.getEffectiveModelConfig(),
      };
      this.testBusy = true;
      testModelConfig(payload)
        .then((res) => {
          if (res && res.success) {
            this.$message.success(res.message || '模型连接正常');
          } else {
            this.$message.error((res && res.message) || '模型连接失败');
          }
        })
        .catch((err) => {
          this.$message.error(err.message || '模型连接失败');
        })
        .finally(() => {
          this.testBusy = false;
        });
    },
    getActiveStylePreset() {
      return this.stylePresets.find((item) => item.id === this.stylePresetId) || null;
    },
    getStylePresetPayload(preset) {
      if (!preset) return null;
      return {
        target: preset.target || 'all',
        styles: preset.styles || undefined,
        operations: preset.operations || undefined,
      };
    },
    getStylePresetPayloadForInsert(preset) {
      if (!preset) return null;
      return {
        target: 'selected',
        styles: preset.styles || undefined,
        operations: preset.operations || undefined,
      };
    },
    openStylePreset() {
      this.stylePresetVisible = true;
    },
    saveModelConfig() {
      if (!this.modelConfig.baseUrl || !this.modelConfig.apiKey) {
        this.$message.warning('请填写 Base URL 和 API Key');
        return;
      }
      if (!this.modelVendor) {
        this.$message.warning('请选择模型厂商');
        return;
      }
      this.modelConfig.provider = this.modelVendor;
      this.saveModelConfigToStorage();
      this.modelConfigVisible = false;
      this.$message.success('模型配置已保存');
    },
    applyStylePreset() {
      const preset = this.getActiveStylePreset();
      if (!preset) {
        this.$message.warning('请选择预设风格');
        return;
      }
      this.saveStylePreset();
      if (!this.hasCanvasContent) {
        this.$message.info('已保存绘图风格，后续生成会自动应用');
        this.stylePresetVisible = false;
        return;
      }
      const payload = this.getStylePresetPayload(preset);
      this.requestCanvasAction({ type: 'modifyStyle', payload });
      this.stylePresetVisible = false;
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
      this.messages.push({ id: makeId(), role, text: '', code, codeType: 'diagram' });
      this.lastMermaid = code;
      this.saveLastMermaid();
      this.saveMessages();
      this.$nextTick(() => this.scrollToBottom());
    },
    appendStyleCode(role, content) {
      const code = content || '';
      this.messages.push({ id: makeId(), role, text: '', code, codeType: 'style' });
      this.saveMessages();
      this.$nextTick(() => this.scrollToBottom());
    },
    shouldUseEdit(prompt) {
      if (!prompt) return false;
      const lower = prompt.toLowerCase();
      return EDIT_HINTS.some((hint) => lower.includes(hint.toLowerCase()));
    },
    shouldUseStyle(prompt) {
      if (!prompt) return false;
      const lower = prompt.toLowerCase();
      return STYLE_HINTS.some((hint) => lower.includes(hint.toLowerCase()));
    },
    resolveStyleTarget(prompt, command) {
      const text = (prompt || '').toLowerCase();
      const matchSelected = STYLE_TARGET_HINTS.selected.some((k) => text.includes(k));
      const matchAll = STYLE_TARGET_HINTS.all.some((k) => text.includes(k));
      const matchEdges = STYLE_TARGET_HINTS.edges.some((k) => text.includes(k));
      const matchVertices = STYLE_TARGET_HINTS.vertices.some((k) => text.includes(k));

      if (matchSelected) return 'selected';
      if (matchAll) return 'all';
      if (matchEdges) return 'edges';
      if (matchVertices) return 'vertices';

      if (!command || !command.target || command.target === 'selected') {
        return 'all';
      }
      return command.target;
    },
    insertCode(code) {
      if (!code) return;
      const preset = this.getActiveStylePreset();
      const stylePreset = this.getStylePresetPayloadForInsert(preset);
      this.requestCanvasAction({
        type: 'insert',
        payload: {
          format: 'mermaid',
          data: code,
          editable: this.renderMode === 'native',
          ...(stylePreset ? { stylePreset } : {}),
        },
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
        // Ignore localStorage errors.
      }
    },
    saveLastMermaid() {
      try {
        localStorage.setItem(MERMAID_STORAGE_KEY, this.lastMermaid || '');
      } catch {
        // Ignore storage errors.
      }
    },
    loadModelConfig() {
      try {
        const raw = localStorage.getItem(MODEL_CONFIG_KEY);
        if (!raw) return;
        const parsed = JSON.parse(raw);
        if (parsed && typeof parsed === 'object') {
          this.modelConfig = {
            baseUrl: parsed.baseUrl || '',
            apiKey: parsed.apiKey || '',
            model: parsed.model || '',
            provider: parsed.provider || '',
          };
          if (this.modelConfig.provider) {
            this.modelVendor = this.modelConfig.provider;
          }
        }
      } catch {
        // Ignore invalid storage content.
      }
    },
    saveModelConfigToStorage() {
      try {
        localStorage.setItem(MODEL_CONFIG_KEY, JSON.stringify(this.modelConfig));
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
    loadStylePreset() {
      try {
        const raw = localStorage.getItem(STYLE_PRESET_KEY);
        if (typeof raw === 'string') {
          this.stylePresetId = raw;
        }
      } catch {
        // Ignore localStorage errors.
      }
    },
    saveStylePreset() {
      try {
        localStorage.setItem(STYLE_PRESET_KEY, this.stylePresetId || '');
      } catch {
        // Ignore storage errors.
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
      if (!this.hasModelConfig) {
        this.modelConfigVisible = true;
        this.$message.warning('请先设置模型配置');
        return;
      }

      this.prompt = '';
      this.append('user', value);

      this.setAiBusy(true);
      try {
        const useStyle = this.intentMode === 'style';
        if (useStyle) {
          if (!this.hasCanvasContent) {
            this.$message.warning('画布为空，请先生成图形后再修改');
            return;
          }
          const modelConfig = this.getEffectiveModelConfig();
          const command = await generateStyleCommand({
            prompt: value,
            modelConfig,
          });
          command.target = this.resolveStyleTarget(value, command);
          const json = JSON.stringify(command, null, 2);
          this.appendStyleCode('ai', json);
          this.requestCanvasAction({
            type: 'modifyStyle',
            payload: command,
          });
          this.setLastExplain('已生成样式修改指令');
          return;
        }

        const modelConfig = this.getEffectiveModelConfig();
        const useEdit = this.lastMermaid && this.shouldUseEdit(value);
        const res = useEdit
          ? await editMermaidDiagram({
              diagramType: '',
              prompt: value,
              mermaid: this.lastMermaid,
              modelConfig,
            })
          : await generateDiagram({
              language: 'mermaid',
              diagramType: '',
              prompt: value,
              modelConfig,
            });

        if (!res || !res.content) throw new Error('Empty result');

        this.setLastExplain(res.explain || 'Generated');
        this.appendCode('ai', res.content);

        if (res.language === 'mermaid' && res.content) {
          const preset = this.getActiveStylePreset();
          const stylePreset = this.getStylePresetPayload(preset);
          this.requestCanvasAction({
            type: 'import',
            payload: {
              format: 'mermaid',
              data: res.content,
              editable: this.renderMode === 'native',
              ...(stylePreset ? { stylePreset } : {}),
            },
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
.composerModes {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
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
.formRow {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.formLabel {
  min-width: 64px;
  font-size: 12px;
  color: #606266;
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
.spin {
  display: inline-block;
  margin-right: 6px;
  animation: btn-spin 0.9s linear infinite;
}
@keyframes btn-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
