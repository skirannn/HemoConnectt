import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Module 9: React <-> Spring Boot integration.
// The dev server proxies /api/** straight to the Spring Boot backend
// (port 8080 by default), so every existing fetch('/api/...') call in
// this codebase keeps working with NO path changes - only the target
// port changed, from the old Express server (5000) to Spring Boot (8080).
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    chunkSizeWarningLimit: 1000, // Increase limit to suppress warning (optional)
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            return id
              .toString()
              .split('node_modules/')[1]
              .split('/')[0]
              .toString();
          }
        },
      },
    },
  },
});