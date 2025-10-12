package com.oem.evwarranty.dto.response;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Integer notificationId;
    private Integer campaignId;
    private Integer affectedId;
    private String channel;
    private String status;
    private LocalDateTime sentAt;

    public Integer getNotificationId() { return notificationId; }
    public void setNotificationId(Integer notificationId) { this.notificationId = notificationId; }
    public Integer getCampaignId() { return campaignId; }
    public void setCampaignId(Integer campaignId) { this.campaignId = campaignId; }
    public Integer getAffectedId() { return affectedId; }
    public void setAffectedId(Integer affectedId) { this.affectedId = affectedId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}