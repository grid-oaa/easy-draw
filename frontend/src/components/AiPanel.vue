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
      <div class="msg ai">
        输入需求后将当前 draw.io XML 发给后端，后端返回新的 XML，我会直接加载到画布里。
      </div>
      <div class="msg user" v-if="lastUser">{{ lastUser }}</div>
      <div class="msg ai" v-if="lastExplain">{{ lastExplain }}</div>
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
        <el-button type="primary" size="mini" :loading="aiBusy" @click="send">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex';

import { requestAiPatch } from '@/api/ai';
import { STYLE_PRESET } from '@/shared/constants';

export default {
  name: 'AiPanel',
  data() {
    return {
      prompt: '',
      lastUser: '',
    };
  },
  computed: {
    ...mapState({
      drawioXml: (state) => state.editor.drawioXml,
      aiBusy: (state) => state.editor.aiBusy,
      lastExplain: (state) => state.editor.lastExplain,
    }),
  },
  methods: {
    ...mapMutations(['setAiBusy', 'setLastExplain', 'setDirty', 'requestCanvasAction']),
    noop() {},
    prefill() {
      this.prompt = '将当前图转换为 AWS 风格，并自动排版对齐。';
    },
    clear() {
      this.prompt = '';
    },
    async send() {
      const value = this.prompt.trim();
      if (!value) return;
      this.lastUser = value;
      this.prompt = '';

      this.setAiBusy(true);
      try {
        const res = await requestAiPatch({
          format: 'drawio',
          prompt: value,
          drawioXml: this.drawioXml,
          stylePreset: STYLE_PRESET.AWS,
        });

        const nextXml = res && typeof res.drawioXml === 'string' ? res.drawioXml : '';
        if (!nextXml) throw new Error('AI 未返回 drawioXml');

        this.setLastExplain((res && res.explain) || '已生成，可一键应用到画布。');
        this.requestCanvasAction({ type: 'load', payload: { xml: nextXml } });
        this.setDirty(true);
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
