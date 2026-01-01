const fs = require('fs');
const path = 'frontend/src/components/AiPanel.vue';
let text = fs.readFileSync(path, 'utf8');
if (text.includes('renderMode') && text.includes('Mermaid 数据')) {
  console.log('skip');
  process.exit(0);
}
const eol = text.includes('\r\n') ? '\r\n' : '\n';
const marker = eol + '        <input ref="file" class="file" type="file" @change="onFile" />' + eol;
if (!text.includes(marker)) {
  console.error('marker-not-found');
  process.exit(1);
}
const insert = eol + '        <div class="toggleWrap">'
  + eol + '          <el-radio-group v-model="renderMode" size="mini">'
  + eol + '            <el-radio label="native">原生图形</el-radio>'
  + eol + '            <el-radio label="mermaid">Mermaid 数据</el-radio>'
  + eol + '          </el-radio-group>'
  + eol + '        </div>';
text = text.replace(marker, marker + insert + eol);
fs.writeFileSync(path, text, 'utf8');
console.log('patched');