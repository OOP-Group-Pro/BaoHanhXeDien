package com.oem.evwarranty.service;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FCMService {

    public void sendNotification(String targetToken, String title, String body) {
        if (targetToken == null || targetToken.isEmpty()) {
            System.out.println("⚠️ Không có Token, bỏ qua việc gửi thông báo.");
            return;
        }

        try {
            // Tạo nội dung thông báo
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            // Gửi đi
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("🔔 Đã gửi thông báo thành công: " + response);

        } catch (Exception e) {
            // Bắt lỗi để không làm ảnh hưởng đến luồng chính (Duyệt phiếu vẫn thành công dù gửi tin lỗi)
            System.err.println("❌ Lỗi gửi FCM: " + e.getMessage());
        }
    }
}