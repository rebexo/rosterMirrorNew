import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'https://hta6zrcy0k.execute-api.eu-central-1.amazonaws.com/Prod', // backend bei aws
        changeOrigin: true,
        // Falls dein Backend "api" im Pfad erwartet, lassen wir es so.
        // Falls dein Backend direkt unter /auth liegt (ohne /api davor), müsstest du rewrite nutzen.
        // Laut deinem Code scheinen die Pfade aber /api zu beinhalten (siehe axios calls).
      }
    }
  }

})
