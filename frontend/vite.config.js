import { defineConfig } from 'vite';

// https://vitejs.dev/config/
export default defineConfig({
    server: {
        // Cổng 5173 (hoặc bất kỳ cổng nào bạn thích)
        port: 5173,
        proxy: {
            // Chuyển tiếp tất cả API call
            '/api/v1': {
                target: 'http://localhost:80', // Đến API Gateway của bạn
                changeOrigin: true,
            }
        }
    }
});