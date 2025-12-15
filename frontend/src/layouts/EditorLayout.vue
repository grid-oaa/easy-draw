<template>
  <div class="page">
    <TopBar />
    <div class="content" :class="{ aiClosed: !rightPanelOpen }">
      <main class="editor">
        <CanvasStage />
      </main>
      <aside class="right" v-show="rightPanelOpen">
        <AiPanel />
      </aside>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';
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
</style>
