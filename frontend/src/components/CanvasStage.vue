<template>
  <div class="stage">
    <div class="canvas">
      <iframe
        ref="frame"
        class="frame"
        :src="iframeSrc"
        title="Diagrams.net Editor"
        frameborder="0"
        allow="clipboard-read; clipboard-write"
      />
    </div>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex';

import { loadDraftDiagram, saveDraftDiagram } from '@/api/localDiagramStore';
import { getDrawioBaseUrl, getDrawioOrigin } from '@/shared/drawio';

export default {
  name: 'CanvasStage',
  data() {
    return {
      ready: false,
      pendingActions: [],
    };
  },
  computed: {
    ...mapState({
      fileName: (state) => state.editor.fileName,
      diagramId: (state) => state.editor.diagramId,
      version: (state) => state.editor.version,
      drawioXml: (state) => state.editor.drawioXml,
      canvasAction: (state) => state.editor.canvasAction,
      canvasActionNonce: (state) => state.editor.canvasActionNonce,
    }),
    iframeSrc() {
      const base = getDrawioBaseUrl();
      const url = new URL(base, window.location.origin);
      if (!url.pathname.endsWith('/')) url.pathname += '/';

      url.searchParams.set('embed', '1');
      url.searchParams.set('proto', 'json');
      url.searchParams.set('spin', '1');
      url.searchParams.set('configure', '1');
      url.searchParams.set('noExitBtn', '1');
      url.searchParams.set('libraries', '1');

      return url.toString();
    },
    drawioOrigin() {
      return getDrawioOrigin();
    },
  },
  watch: {
    canvasActionNonce() {
      this.runCanvasAction(this.canvasAction);
    },
  },
  mounted() {
    const draft = loadDraftDiagram();
    if (draft) {
      this.setFileName(draft.title || 'Untitled');
      this.setDiagramId(draft.id || 'draft');
      this.setVersion(draft.version || 0);
      this.setDrawioXml(draft.drawioXml);
    }

    window.addEventListener('message', this.onMessage);
  },
  beforeDestroy() {
    window.removeEventListener('message', this.onMessage);
  },
  methods: {
    ...mapMutations([
      'setDirty',
      'setFileName',
      'setDiagramId',
      'setVersion',
      'setDrawioXml',
      'setSaving',
    ]),
    postToEditor(message) {
      const frame = this.$refs.frame;
      if (!frame || !frame.contentWindow) return;
      const targetOrigin = this.drawioOrigin || '*';
      frame.contentWindow.postMessage(JSON.stringify(message), targetOrigin);
    },
    runCanvasAction(action) {
      if (!action || !action.type) return;
      if (!this.ready) {
        this.pendingActions.push(action);
        return;
      }

      if (action.type === 'save') {
        this.postToEditor({ action: 'save', exit: false });
        return;
      }

      if (action.type === 'load') {
        const xml = action.payload && action.payload.xml ? action.payload.xml : '';
        if (xml) this.setDrawioXml(xml);
        this.postToEditor({ action: 'load', xml });
        return;
      }

      if (action.type === 'export') {
        const format = action.payload && action.payload.format ? action.payload.format : 'svg';
        this.postToEditor({ action: 'export', format, spin: '1' });
      }
    },
    flushPendingActions() {
      const actions = this.pendingActions.slice();
      this.pendingActions = [];
      actions.forEach((a) => this.runCanvasAction(a));
    },
    sanitizeFileName(value) {
      const base = (value || 'diagram').trim() || 'diagram';
      return base.replace(/[\\/:*?"<>|]/g, '_');
    },
    downloadExport(payload) {
      const format = payload && payload.format ? payload.format : '';
      const data = payload && typeof payload.data === 'string' ? payload.data : '';
      if (!format || !data) {
        this.$message.error('导出失败：缺少数据');
        return;
      }

      const name = this.sanitizeFileName(this.fileName);
      const link = document.createElement('a');
      link.rel = 'noopener';

      if (data.startsWith('data:')) {
        link.href = data;
      } else if (format === 'svg' && data.trim().startsWith('<')) {
        const blob = new Blob([data], { type: 'image/svg+xml;charset=utf-8' });
        link.href = URL.createObjectURL(blob);
      } else {
        const mime =
          format === 'png'
            ? 'image/png'
            : format === 'jpg' || format === 'jpeg'
            ? 'image/jpeg'
            : 'application/octet-stream';
        link.href = `data:${mime};base64,${data}`;
      }

      link.download = `${name}.${format}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      if (link.href.startsWith('blob:')) URL.revokeObjectURL(link.href);
      this.$message.success(`已导出 ${format.toUpperCase()}`);
    },
    onMessage(evt) {
      if (this.drawioOrigin && evt.origin !== this.drawioOrigin) return;

      let msg = evt.data;
      if (typeof msg === 'string') {
        try {
          msg = JSON.parse(msg);
        } catch {
          return;
        }
      }
      if (!msg || typeof msg !== 'object') return;

      if (msg.event === 'configure') {
        this.postToEditor({
          action: 'configure',
          config: {
            defaultFonts: ['Helvetica', 'Arial', 'Microsoft YaHei'],
            darkMode: false,
          },
        });
        return;
      }

      if (msg.event === 'init') {
        this.ready = true;
        this.postToEditor({ action: 'load', xml: this.drawioXml });
        this.flushPendingActions();
        return;
      }

      if (msg.event === 'change') {
        this.setDirty(true);
        return;
      }

      if (msg.event === 'save') {
        const xml = msg.xml;
        if (typeof xml === 'string' && xml) this.setDrawioXml(xml);

        const nextVersion = (this.version || 0) + 1;
        const diagram = {
          id: this.diagramId || 'draft',
          title: this.fileName || 'Untitled',
          format: 'drawio',
          drawioXml: typeof xml === 'string' && xml ? xml : this.drawioXml,
          version: nextVersion,
          updatedAt: new Date().toISOString(),
        };

        this.setDiagramId(diagram.id);
        this.setVersion(diagram.version);
        saveDraftDiagram(diagram);
        this.setSaving(false);
        this.setDirty(false);
        this.$message.success('已保存');
        return;
      }

      if (msg.event === 'export') {
        this.downloadExport(msg);
        return;
      }

      if (msg.event === 'error') {
        const message = msg.message || 'draw.io 出错';
        this.setSaving(false);
        this.$message.error(message);
      }
    },
  },
};
</script>

<style scoped>
.stage {
  height: 100%;
  padding: 0;
  box-sizing: border-box;
}
.canvas {
  height: 100%;
  background-color: #fff;
  position: relative;
  overflow: hidden;
}
.frame {
  width: 100%;
  height: 100%;
  border: none;
}
</style>
