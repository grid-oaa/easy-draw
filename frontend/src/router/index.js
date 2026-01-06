import Vue from 'vue';
import Router from 'vue-router';

import EditorView from '@/views/EditorView.vue';

Vue.use(Router);

export default new Router({
  mode: 'history',
  // 中文注释：与 publicPath 保持一致，确保 /draw 前缀下的路由可用
  base: process.env.BASE_URL,
  routes: [
    { path: '/', redirect: '/editor' },
    { path: '/editor', name: 'editor', component: EditorView },
  ],
});
