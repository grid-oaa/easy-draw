import Vue from 'vue';
import Vuex from 'vuex';

import { EMPTY_DRAWIO_XML } from '@/shared/drawio';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    ui: {
      rightPanelOpen: true,
    },
    ai: {
      resetNonce: 0,
    },
    editor: {
      dirty: false,
      fileName: 'Untitled',
      format: 'drawio',
      diagramId: null,
      version: 0,
      drawioXml: EMPTY_DRAWIO_XML,
      saving: false,
      aiBusy: false,
      lastExplain: '',
      canvasAction: null,
      canvasActionNonce: 0,
    },
  },
  mutations: {
    setDirty(state, dirty) {
      state.editor.dirty = dirty;
    },
    setFileName(state, fileName) {
      state.editor.fileName = fileName;
    },
    setDiagramId(state, diagramId) {
      state.editor.diagramId = diagramId;
    },
    setVersion(state, version) {
      state.editor.version = version;
    },
    setDrawioXml(state, drawioXml) {
      state.editor.drawioXml = drawioXml;
    },
    setSaving(state, saving) {
      state.editor.saving = saving;
    },
    setAiBusy(state, aiBusy) {
      state.editor.aiBusy = aiBusy;
    },
    setLastExplain(state, lastExplain) {
      state.editor.lastExplain = lastExplain;
    },
    requestCanvasAction(state, canvasAction) {
      state.editor.canvasActionNonce += 1;
      state.editor.canvasAction = canvasAction;
    },
    resetAiChat(state) {
      state.ai.resetNonce += 1;
    },
    toggleRightPanel(state) {
      state.ui.rightPanelOpen = !state.ui.rightPanelOpen;
    },
  },
});
