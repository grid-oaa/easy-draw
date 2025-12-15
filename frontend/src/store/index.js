import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    ui: {
      leftPanelOpen: true,
      rightPanelOpen: true,
    },
    editor: {
      zoom: 0.6,
      dirty: false,
      fileName: 'Untitled',
    },
  },
  mutations: {
    setZoom(state, zoom) {
      state.editor.zoom = zoom;
    },
    setDirty(state, dirty) {
      state.editor.dirty = dirty;
    },
    setFileName(state, fileName) {
      state.editor.fileName = fileName;
    },
    toggleLeftPanel(state) {
      state.ui.leftPanelOpen = !state.ui.leftPanelOpen;
    },
    toggleRightPanel(state) {
      state.ui.rightPanelOpen = !state.ui.rightPanelOpen;
    },
  },
});
