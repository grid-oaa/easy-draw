<template>
  <div class="topbar">
    <div class="left">
      <el-menu mode="horizontal" :default-active="'draw'" class="menu">
        <el-submenu index="draw">
          <template #title>绘图</template>
          <el-menu-item index="draw:new">新建</el-menu-item>
          <el-menu-item index="draw:open">打开</el-menu-item>
        </el-submenu>
        <el-submenu index="shape">
          <template #title>形状</template>
          <el-menu-item index="shape:lib">形状库</el-menu-item>
        </el-submenu>
        <el-submenu index="format">
          <template #title>格式</template>
          <el-menu-item index="format:style">样式</el-menu-item>
        </el-submenu>
        <el-submenu index="insert">
          <template #title>插入</template>
          <el-menu-item index="insert:text">文本</el-menu-item>
          <el-menu-item index="insert:image">图片</el-menu-item>
        </el-submenu>
        <el-submenu index="delete">
          <template #title>删除</template>
          <el-menu-item index="delete:selection">删除选中</el-menu-item>
        </el-submenu>
      </el-menu>
    </div>

    <div class="center">
      <el-button-group>
        <el-button size="mini" icon="el-icon-refresh-left" @click="noop">撤销</el-button>
        <el-button size="mini" icon="el-icon-refresh-right" @click="noop">重做</el-button>
      </el-button-group>

      <div class="zoom">
        <el-button size="mini" icon="el-icon-zoom-out" @click="zoomOut" />
        <span class="value">{{ zoomPercent }}</span>
        <el-button size="mini" icon="el-icon-zoom-in" @click="zoomIn" />
      </div>
    </div>

    <div class="right">
      <el-button type="primary" size="mini" @click="save">保存</el-button>
    </div>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex';

export default {
  name: 'TopBar',
  computed: {
    ...mapState({
      zoom: (state) => state.editor.zoom,
    }),
    zoomPercent() {
      return `${Math.round(this.zoom * 100)}%`;
    },
  },
  methods: {
    ...mapMutations(['setZoom', 'setDirty']),
    noop() {},
    zoomIn() {
      this.setZoom(Math.min(2, +(this.zoom + 0.1).toFixed(2)));
    },
    zoomOut() {
      this.setZoom(Math.max(0.1, +(this.zoom - 0.1).toFixed(2)));
    },
    save() {
      this.setDirty(false);
      this.$message.success('已保存（占位实现）');
    },
  },
};
</script>

<style scoped>
.topbar {
  height: 48px;
  display: grid;
  grid-template-columns: 1fr auto auto;
  align-items: center;
  gap: 12px;
  padding: 0 12px;
  border-bottom: 1px solid #e9e9e9;
  background: #fff;
}
.menu {
  border-bottom: none;
}
.center {
  display: flex;
  align-items: center;
  gap: 12px;
}
.zoom {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border: 1px solid #e9e9e9;
  border-radius: 8px;
  background: #fff;
}
.value {
  width: 56px;
  text-align: center;
  color: #333;
  font-variant-numeric: tabular-nums;
}
.right {
  display: flex;
  justify-content: flex-end;
}
</style>
