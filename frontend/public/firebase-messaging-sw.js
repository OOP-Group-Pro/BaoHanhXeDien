// frontend/firebase-messaging-sw.js
importScripts('https://www.gstatic.com/firebasejs/9.22.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.22.0/firebase-messaging-compat.js');

// Lấy config ở: Firebase Console -> Project Settings -> General -> Your apps -> SDK setup
const firebaseConfig = {
      apiKey: "AIzaSyDNE6e-KnSZurxp-IvCQsP__l8tF6bfgQQ",
      authDomain: "oem-ev-warranty.firebaseapp.com",
      projectId: "oem-ev-warranty",
      storageBucket: "oem-ev-warranty.firebasestorage.app",
      messagingSenderId: "702645009628",
      appId: "1:702645009628:web:231ba7c194e6261f9e8819",
      measurementId: "G-BNF6P0YF3Z"
    };

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

// Xử lý khi app đang đóng (Background)
messaging.onBackgroundMessage((payload) => {
    console.log('[Background] Nhận tin:', payload);
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: '/public/images/logo.png' // Đường dẫn icon của bạn
  };

    self.registration.showNotification(notificationTitle, notificationOptions);
});