import Vue from 'vue';
import Router from 'vue-router';

import EditorView from '@/views/EditorView.vue';

Vue.use(Router);

export default new Router({
  mode: 'history',
  routes: [
    { path: '/', redirect: '/editor' },
    { path: '/editor', name: 'editor', component: EditorView },
  ],
});
