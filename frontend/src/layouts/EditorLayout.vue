<template>
  <div class="page">
    <TopBar />
    <div class="content" :class="{ aiClosed: !rightPanelOpen }">
      <main class="editor">
        <CanvasStage />
      </main>
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
  computed: {
    ...mapState({
      rightPanelOpen: (state) => state.ui.rightPanelOpen,
    }),
  },
  methods: {
    ...mapMutations(['toggleRightPanel']),
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
  grid-template-columns: 1fr 360px;
  position: relative;
}
.content.aiClosed {
  grid-template-columns: 1fr;
}
.editor {
  overflow: hidden;
  background: #f5f7fb;
}
.right {
  border-left: 1px solid #e9e9e9;
  background: #fff;
  overflow: auto;
}
.aiToggle {
  position: absolute;
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
