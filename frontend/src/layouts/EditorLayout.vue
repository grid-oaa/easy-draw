<template>
  <div class="page">
    <TopBar />
    <div
      class="content"
      :class="{ aiClosed: !rightPanelOpen, resizing: resizing }"
      :style="contentStyle"
    >
      <main class="editor">
        <CanvasStage />
      </main>
      <div
        v-show="rightPanelOpen"
        class="resizer"
        role="separator"
        aria-orientation="vertical"
        @mousedown="startResize"
      />
      <button class="aiToggle" type="button" @click="toggleRightPanel">
        <i :class="rightPanelOpen ? 'el-icon-arrow-right' : 'el-icon-arrow-left'" />
      </button>
      <aside class="right" v-show="rightPanelOpen">
        <AiPanel />
      </aside>
    </div>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex';
import TopBar from '@/components/TopBar.vue';
import CanvasStage from '@/components/CanvasStage.vue';
import AiPanel from '@/components/AiPanel.vue';

export default {
  name: 'EditorLayout',
  components: { TopBar, CanvasStage, AiPanel },
  data() {
    return {
      panelWidth: 360,
      resizing: false,
    };
  },
  computed: {
    ...mapState({
      rightPanelOpen: (state) => state.ui.rightPanelOpen,
    }),
    contentStyle() {
      if (!this.rightPanelOpen) return {};
      return { gridTemplateColumns: `1fr 10px ${this.panelWidth}px` };
    },
  },
  methods: {
    ...mapMutations(['toggleRightPanel']),
    startResize(event) {
      if (!this.rightPanelOpen) return;
      this.resizing = true;
      this.onResize(event);
      window.addEventListener('mousemove', this.onResize);
      window.addEventListener('mouseup', this.stopResize);
    },
    onResize(event) {
      if (!this.resizing) return;
      if (this.resizeFrame) return;
      const moveEvent = event;
      this.resizeFrame = window.requestAnimationFrame(() => {
        this.resizeFrame = null;
        this.applyResize(moveEvent);
      });
    },
    applyResize(event) {
      const container = this.$el.querySelector('.content');
      if (!container) return;
      const rect = container.getBoundingClientRect();
      const maxWidth = Math.max(260, rect.width - 320);
      const minWidth = 260;
      const nextWidth = rect.right - event.clientX;
      this.panelWidth = Math.min(Math.max(nextWidth, minWidth), maxWidth);
    },
    stopResize() {
      if (!this.resizing) return;
      this.resizing = false;
      if (this.resizeFrame) {
        window.cancelAnimationFrame(this.resizeFrame);
        this.resizeFrame = null;
      }
      window.removeEventListener('mousemove', this.onResize);
      window.removeEventListener('mouseup', this.stopResize);
    },
  },
  beforeDestroy() {
    this.stopResize();
  },
};
</script>

<style scoped>
.page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.content {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 10px 360px;
  grid-template-rows: 1fr;
  position: relative;
}
.content.aiClosed {
  grid-template-columns: 1fr;
}
.editor {
  overflow: hidden;
  background: #f5f7fb;
  grid-column: 1 / 2;
  grid-row: 1 / 2;
}
.right {
  border-left: 1px solid #e9e9e9;
  background: #fff;
  overflow: auto;
  grid-column: 3 / 4;
  grid-row: 1 / 2;
}
.resizer {
  cursor: col-resize;
  width: 10px;
  background: transparent;
  position: relative;
  grid-column: 2 / 3;
  grid-row: 1 / 2;
}
.resizer::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e5e5e5;
}
.resizer:hover::before {
  background: #c9c9c9;
}
.content.resizing {
  cursor: col-resize;
  user-select: none;
}
.content.resizing .editor,
.content.resizing .right {
  pointer-events: none;
}
.aiToggle {
  position: fixed;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
  z-index: 5;
  padding: 6px 8px;
  border-radius: 12px 0 0 12px;
  border: 1px solid #e5e5e5;
  border-right: none;
  background: #fff;
  color: #333;
  font-size: 12px;
  cursor: pointer;
}
.aiToggle:hover {
  border-color: #c9c9c9;
}
.content.aiClosed .aiToggle {
  right: 0;
}
</style>
