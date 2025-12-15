<template>
  <div class="panel">
    <div class="header">形状</div>
    <div class="search">
      <el-input
        size="small"
        placeholder="键入/进行搜索"
        v-model="query"
        prefix-icon="el-icon-search"
      />
    </div>

    <el-collapse v-model="active" accordion>
      <el-collapse-item title="通用" name="general">
        <div class="grid">
          <div v-for="item in filteredGeneral" :key="item" class="item">{{ item }}</div>
        </div>
      </el-collapse-item>
      <el-collapse-item title="杂项" name="misc">
        <div class="grid">
          <div v-for="item in filteredMisc" :key="item" class="item">{{ item }}</div>
        </div>
      </el-collapse-item>
      <el-collapse-item title="高级" name="advanced">
        <div class="hint">预留：高级形状/图标库（如 AWS）</div>
      </el-collapse-item>
      <el-collapse-item title="基本" name="basic">
        <div class="hint">预留：基础形状</div>
      </el-collapse-item>
      <el-collapse-item title="箭头" name="arrows">
        <div class="hint">预留：箭头/连接器</div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
export default {
  name: 'ShapeLibrary',
  data() {
    return {
      query: '',
      active: 'general',
      general: ['矩形', '圆角矩形', '圆形', '菱形', '文本'],
      misc: ['云', '数据库', '用户', '注释'],
    };
  },
  computed: {
    filteredGeneral() {
      const q = this.query.trim();
      return q ? this.general.filter((x) => x.includes(q)) : this.general;
    },
    filteredMisc() {
      const q = this.query.trim();
      return q ? this.misc.filter((x) => x.includes(q)) : this.misc;
    },
  },
};
</script>

<style scoped>
.panel {
  padding: 12px;
}
.header {
  font-weight: 600;
  margin-bottom: 8px;
}
.search {
  margin-bottom: 12px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}
.item {
  height: 44px;
  border: 1px solid #e9e9e9;
  border-radius: 8px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  user-select: none;
}
.hint {
  color: #666;
  font-size: 12px;
}
</style>
