<template>
  <div class="stage">
    <div class="canvas">
      <div class="watermark">Canvas（占位）</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CanvasStage',
};
</script>

<style scoped>
.stage {
  height: 100%;
  padding: 12px;
  box-sizing: border-box;
}
.canvas {
  height: 100%;
  border: 1px solid #e9e9e9;
  border-radius: 12px;
  background-image: linear-gradient(#e9e9e9 1px, transparent 1px),
    linear-gradient(90deg, #e9e9e9 1px, transparent 1px);
  background-size: 16px 16px;
  background-color: #fff;
  position: relative;
  overflow: hidden;
}
.watermark {
  position: absolute;
  left: 16px;
  top: 12px;
  color: #8a8a8a;
  font-size: 12px;
}
</style>
