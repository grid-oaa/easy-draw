<template>
  <div class="page">
    <TopBar />
    <div class="content">
      <aside class="left" v-show="leftPanelOpen">
        <ShapeLibrary />
      </aside>
      <main class="center">
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
import ShapeLibrary from '@/components/ShapeLibrary.vue';
import CanvasStage from '@/components/CanvasStage.vue';
import AiPanel from '@/components/AiPanel.vue';

export default {
  name: 'EditorLayout',
  components: { TopBar, ShapeLibrary, CanvasStage, AiPanel },
  computed: {
    ...mapState({
      leftPanelOpen: (state) => state.ui.leftPanelOpen,
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
  grid-template-columns: 280px 1fr 360px;
}
.left {
  border-right: 1px solid #e9e9e9;
  background: #fafafa;
  overflow: auto;
}
.center {
  overflow: hidden;
  background: #f5f7fb;
}
.right {
  border-left: 1px solid #e9e9e9;
  background: #fff;
  overflow: auto;
}
</style>
