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
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';

export default {
  name: 'TopBar',
  computed: {
    ...mapState({
      dirty: (state) => state.editor.dirty,
      fileName: (state) => state.editor.fileName,
    }),
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
