const fs = require('fs');
const path = 'frontend/public/drawio/js/app.min.js';
let text = fs.readFileSync(path, 'utf8');
const old = 'tags:"plugins/tags.js"}';
const neu = 'tags:"plugins/tags.js","mermaid-import":"plugins/mermaid-import.js"}';
if (text.includes(neu)) {
  console.log('already-patched');
  process.exit(0);
}
if (!text.includes(old)) {
  console.error('pattern-not-found');
  process.exit(1);
}
text = text.replace(old, neu);
fs.writeFileSync(path, text, 'utf8');
console.log('patched');