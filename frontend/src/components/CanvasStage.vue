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

import { getDrawioBaseUrl, getDrawioOrigin, EMPTY_DRAWIO_XML } from '@/shared/drawio';

export default {
  name: 'CanvasStage',
  data() {
    return {
      ready: false,
      pendingActions: [],
      lastChangeAt: 0,
      lastMermaidAckAt: 0,
      pendingMermaidImportAt: 0,
      lastMermaidInsertAckAt: 0,
      pendingMermaidInsertAt: 0,
      mermaidPluginReady: false,
      pendingSaveFormat: '',
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
      url.searchParams.set('plugins', '1');
      url.searchParams.set('p', 'mermaid-import');
      url.searchParams.set('dev', '1');

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
    // Debug: verify iframe URL uses the expected draw.io base.
    // eslint-disable-next-line no-console
    console.log('[drawio] iframe src:', this.iframeSrc);
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
      'resetAiChat',
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
        return;
      }

      if (action.type === 'import') {
        const format = action.payload && action.payload.format ? action.payload.format : 'xml';
        const data = action.payload && action.payload.data ? action.payload.data : '';
        if (!data) return;
        if (format === 'mermaid') {
          if (!this.mermaidPluginReady) {
            this.$message.error('Mermaid import plugin is not ready.');
            return;
          }
          const requestedAt = Date.now();
          this.pendingMermaidImportAt = requestedAt;
          this.postToEditor({
            action: 'importMermaid',
            mermaid: data,
          });
          setTimeout(() => {
            if (
              this.pendingMermaidImportAt === requestedAt &&
              this.lastMermaidAckAt < requestedAt
            ) {
              this.$message.error(
                'Mermaid import not acknowledged. Ensure mermaid-import plugin is loaded.',
              );
            }
          }, 1200);
          return;
        }
        this.postToEditor({ action: 'import', format, data });
        return;
      }

      if (action.type === 'insert') {
        const format = action.payload && action.payload.format ? action.payload.format : 'xml';
        const data = action.payload && action.payload.data ? action.payload.data : '';
        if (!data) return;
        if (format === 'mermaid') {
          if (!this.mermaidPluginReady) {
            this.$message.error('Mermaid insert plugin is not ready.');
            return;
          }
          const requestedAt = Date.now();
          this.pendingMermaidInsertAt = requestedAt;
          this.postToEditor({
            action: 'insertMermaid',
            mermaid: data,
          });
          setTimeout(() => {
            if (
              this.pendingMermaidInsertAt === requestedAt &&
              this.lastMermaidInsertAckAt < requestedAt
            ) {
              this.$message.error('Mermaid insert not acknowledged.');
            }
          }, 1200);
          return;
        }
        this.postToEditor({ action: 'insert', format, data });
        return;
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
    ensureDrawioFileName(name) {
      const safe = this.sanitizeFileName(name || 'diagram');
      return safe.toLowerCase().endsWith('.drawio') ? safe : `${safe}.drawio`;
    },
    async saveDrawioToLocalFile(xml) {
      const fileName = this.ensureDrawioFileName(this.fileName);

      if (window.showSaveFilePicker) {
        try {
          const handle = await window.showSaveFilePicker({
            suggestedName: fileName,
            types: [
              {
                description: 'Draw.io Diagram',
                accept: {
                  'application/xml': ['.drawio', '.xml'],
                },
              },
            ],
          });
          const writable = await handle.createWritable();
          await writable.write(new Blob([xml], { type: 'application/xml;charset=utf-8' }));
          await writable.close();
          if (handle && handle.name) this.setFileName(handle.name.replace(/\.drawio$/i, ''));
          return true;
        } catch (e) {
          if (e && (e.name === 'AbortError' || e.code === 20)) return false;
          throw e;
        }
      }

      const blob = new Blob([xml], { type: 'application/xml;charset=utf-8' });
      const link = document.createElement('a');
      link.rel = 'noopener';
      link.href = URL.createObjectURL(blob);
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(link.href);
      return true;
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
    async onMessage(evt) {
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
            plugins: ['mermaid'],
          },
        });
        return;
      }

      if (msg.event === 'init') {
        const isReload = this.ready;
        this.ready = true;
        if (isReload) {
          this.setDrawioXml(EMPTY_DRAWIO_XML);
          this.setDirty(false);
          this.resetAiChat();
        }
        this.postToEditor({ action: 'load', xml: this.drawioXml });
        this.flushPendingActions();
        return;
      }

      if (msg.event === 'change') {
        this.lastChangeAt = Date.now();
        this.setDirty(true);
        return;
      }

      if (msg.event === 'importMermaid') {
        this.lastMermaidAckAt = Date.now();
        this.pendingMermaidImportAt = 0;
        if (!msg.success) {
          this.$message.error(msg.error || 'Mermaid import failed in draw.io.');
        }
        return;
      }

      if (msg.event === 'insertMermaid') {
        this.lastMermaidInsertAckAt = Date.now();
        this.pendingMermaidInsertAt = 0;
        if (!msg.success) {
          this.$message.error(msg.error || 'Mermaid insert failed in draw.io.');
        }
        return;
      }

      if (msg.event === 'mermaid-import-ready') {
        this.mermaidPluginReady = true;
        return;
      }

      if (msg.event === 'save') {
        const xml = msg.xml;
        if (typeof xml === 'string' && xml) this.setDrawioXml(xml);

        try {
          await this.$msgbox({
            title: '保存',
            message: '请选择保存格式',
            showCancelButton: true,
            confirmButtonText: '保存为 .drawio',
            cancelButtonText: '保存为 PNG',
            distinguishCancelAndClose: true,
            type: 'info',
          });
          const ok = await this.saveDrawioToLocalFile(
            typeof xml === 'string' && xml ? xml : this.drawioXml,
          );
          if (ok) {
            this.setSaving(false);
            this.setDirty(false);
            this.postToEditor({ action: 'status', message: 'Saved' });
            this.$message.success('已保存为 .drawio 文件');
          } else {
            this.setSaving(false);
            this.postToEditor({ action: 'status', message: 'Cancelled' });
          }
        } catch (e) {
          if (e === 'cancel') {
            this.pendingSaveFormat = 'png';
            this.postToEditor({ action: 'export', format: 'png', spin: '1' });
            return;
          }
          if (e === 'close') {
            this.setSaving(false);
            this.postToEditor({ action: 'status', message: 'Cancelled' });
            return;
          }
          this.setSaving(false);
          this.postToEditor({ action: 'status', message: 'Error' });
          this.$message.error(e.message || '保存失败');
        }
        return;
      }

      if (msg.event === 'export') {
        this.downloadExport(msg);
        if (this.pendingSaveFormat === 'png') {
          this.pendingSaveFormat = '';
          this.setSaving(false);
          this.setDirty(false);
          this.postToEditor({ action: 'status', message: 'Saved' });
          this.$message.success('已保存为 PNG 图片');
        }
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
