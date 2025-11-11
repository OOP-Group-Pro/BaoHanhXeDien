package com.oem.evwarranty.dto.response;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long notificationId;
    private Long campaignId;
    private Long affectedId;
    private String channel;
    private String status;
    private LocalDateTime sentAt;

    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public Long getAffectedId() { return affectedId; }
    public void setAffectedId(Long affectedId) { this.affectedId = affectedId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}