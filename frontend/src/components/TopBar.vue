<template>
  <div class="topbar">
    <div class="left">
      <div class="brand">Easy Draw</div>
    </div>

    <div class="right">
      <div class="meta">
        <span class="file">{{ fileName }}</span>
        <span class="dirty" v-if="dirty">未保存</span>
      </div>
      <el-dropdown trigger="click" @command="exportAs">
        <el-button size="mini">导出<i class="el-icon-arrow-down el-icon--right" /></el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="svg">SVG</el-dropdown-item>
          <el-dropdown-item command="png">PNG</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-button type="primary" size="mini" :loading="saving" @click="save">保存</el-button>
      <el-button size="mini" @click="toggleAi">{{
        rightPanelOpen ? '隐藏 AI' : '显示 AI'
      }}</el-button>
    </div>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex';

export default {
  name: 'TopBar',
  computed: {
    ...mapState({
      dirty: (state) => state.editor.dirty,
      saving: (state) => state.editor.saving,
      fileName: (state) => state.editor.fileName,
      rightPanelOpen: (state) => state.ui.rightPanelOpen,
    }),
  },
  methods: {
    ...mapMutations(['setSaving', 'requestCanvasAction', 'toggleRightPanel']),
    save() {
      if (this.saving) return;
      this.setSaving(true);
      this.requestCanvasAction({ type: 'save' });
    },
    exportAs(format) {
      this.requestCanvasAction({ type: 'export', payload: { format } });
    },
    toggleAi() {
      this.toggleRightPanel();
    },
  },
};
</script>

<style scoped>
.topbar {
  height: 48px;
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 0 12px;
  border-bottom: 1px solid #e9e9e9;
  background: #fff;
}
.brand {
  font-weight: 700;
  color: #333;
}
.right {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}
.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 12px;
}
.file {
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dirty {
  color: #e6a23c;
}
</style>
