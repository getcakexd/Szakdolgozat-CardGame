const fs = require('fs');
const path = require('path');

const mainJsPath = path.join(__dirname, 'dist', 'frontend', 'browser', 'main.js');

let content = fs.readFileSync(mainJsPath, 'utf8');

content = content.replace('${GOOGLE_CLIENT_ID}', process.env.GOOGLE_CLIENT_ID || '');

fs.writeFileSync(mainJsPath, content);

console.log('Environment variables replaced successfully');
