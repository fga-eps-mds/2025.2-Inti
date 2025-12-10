import { defineConfig } from 'vite';

export default defineConfig({
  root: '.',          // procura index.html na raiz
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    minify: false,
  },
});
