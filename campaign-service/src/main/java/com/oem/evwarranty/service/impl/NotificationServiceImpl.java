package com.oem.evwarranty.service.impl;

import com.oem.evwarranty.dto.request.NotificationCreateRequest;
import com.oem.evwarranty.dto.response.NotificationResponse;
import com.oem.evwarranty.exception.NotFoundException;
import com.oem.evwarranty.model.*;
import com.oem.evwarranty.model.enums.Channel;
import com.oem.evwarranty.model.enums.DeliveryStatus;
import com.oem.evwarranty.repository.*;
import com.oem.evwarranty.service.NotificationService;
import com.oem.evwarranty.service.mapper.NotificationMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notiRepo;
    private final CampaignRepository campaignRepo;
    private final AffectedVehicleRepository avRepo;

    public NotificationServiceImpl(NotificationRepository notiRepo,
                                   CampaignRepository campaignRepo,
                                   AffectedVehicleRepository avRepo) {
        this.notiRepo = notiRepo;
        this.campaignRepo = campaignRepo;
        this.avRepo = avRepo;
    }

    @Override
    public NotificationResponse create(NotificationCreateRequest req) {
        Campaign c = campaignRepo.findById(req.getCampaignId())
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
        AffectedVehicle av = avRepo.findById(req.getAffectedId())
                .orElseThrow(() -> new NotFoundException("Affected vehicle not found"));

        Notification n = new Notification();
        n.setCampaign(c);
        n.setAffected(av);
        // nếu Channel/DeliveryStatus là enum, bạn chuyển đổi ở đây:
        n.setChannel(Channel.valueOf(req.getChannel()));   // nếu enum
        n.setStatus(DeliveryStatus.valueOf(req.getStatus())); // nếu enum
        n.setSentAt(java.time.LocalDateTime.now());

        notiRepo.save(n);
        return NotificationMapper.toResponse(n);
    }

    @Override
    public NotificationResponse get(Integer id) {
        return notiRepo.findById(id)
                .map(NotificationMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
    }

    @Override
    public void delete(Integer id) {
        if (!notiRepo.existsById(id)) throw new NotFoundException("Notification not found");
        notiRepo.deleteById(id);
    }

    @Override
    public Page<NotificationResponse> listByCampaign(Integer campaignId, Pageable pageable) {
        return notiRepo.findByCampaignId(campaignId, pageable).map(NotificationMapper::toResponse);
    }

    @Override
    public Page<NotificationResponse> listByAffected(Integer affectedId, Pageable pageable) {
        return notiRepo.findByAffectedId(affectedId, pageable).map(NotificationMapper::toResponse);
    }
}
