import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const allowedHosts = (env.VITE_ALLOWED_HOSTS ?? "")
    .split(",")
    .map((host) => host.trim())
    .filter(Boolean);

  return {
    plugins: [react(), tsconfigPaths()],
    server: {
      // Allow external connections (required for Docker)
      host: '0.0.0.0',
      port: 5173,
      strictPort: true,
      
      // Enable file watching with polling (required for Docker on Windows/WSL)
      watch: {
        usePolling: true,
        interval: 1000,
      },
      
      // Apply allowed hosts if configured
      ...(allowedHosts.length > 0 ? { allowedHosts } : {}),
    },
  };
});
