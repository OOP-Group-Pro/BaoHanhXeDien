import { initializeApp } from "https://www.gstatic.com/firebasejs/9.22.0/firebase-app.js";
import { getMessaging, getToken, onMessage } from "https://www.gstatic.com/firebasejs/9.22.0/firebase-messaging.js";
import { api } from '../services/apiClient.js';



const firebaseConfig = {
      apiKey: "AIzaSyDNE6e-KnSZurxp-IvCQsP__l8tF6bfgQQ",
      authDomain: "oem-ev-warranty.firebaseapp.com",
      projectId: "oem-ev-warranty",
      storageBucket: "oem-ev-warranty.firebasestorage.app",
      messagingSenderId: "702645009628",
      appId: "1:702645009628:web:231ba7c194e6261f9e8819",
      measurementId: "G-BNF6P0YF3Z"
    };

const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);

export const registerNotification = async () => {
    try {
        // 1. Xin quyền
        const permission = await Notification.requestPermission();
        if (permission === 'granted') {

            // 2. Lấy Token (Dán VAPID Key A ở Bước 1 vào đây)
            const token = await getToken(messaging, {
                vapidKey: "BNpzaNgtf4tEqXLlnUm1KHzZPptXnrHWFBhP0G9VI8rgmgRTYfQgpPvzI_ZIpaOZfhuBxEHEOH3ttUGrQ7WaCiA"
            });

            if (token) {
                console.log("🔔 Token thiết bị:", token);
                // 3. Gửi Token về User Service để lưu
                await api.post('/users/fcm-token', { token: token });
            }
        } else {
            console.log("🔕 Người dùng từ chối nhận thông báo.");
        }
    } catch (error) {
        console.error("Lỗi FCM:", error);
    }
};

// Lắng nghe khi đang mở Web (Foreground)
export const listenInForeground = () => {
    const messaging = getMessaging();

    onMessage(messaging, (payload) => {
        console.log('🔥🔥🔥 CÓ TIN NHẮN MỚI ĐẾN:', payload);

        // 1. Lấy thông tin
        const { title, body } = payload.notification;

        // 2. Hiện Alert (Hoặc dùng thư viện Toast nếu có)
        // Đây là cái giúp bạn thấy "Ting Ting" khi đang mở web
        alert(`🔔 THÔNG BÁO MỚI:\n\n${title}\n${body}`);

        // 3. Nếu muốn phát âm thanh (Tùy chọn)
        // const audio = new Audio('/assets/notification.mp3');
        // audio.play();
    });
};