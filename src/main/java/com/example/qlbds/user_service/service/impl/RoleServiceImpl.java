package com.example.qlbds.user_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.config.CurrentUserService;
import com.example.qlbds.shared.entity.enums.AgentRequestStatus;
import com.example.qlbds.shared.entity.enums.UserRole;
import com.example.qlbds.user_service.dto.*;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.AgentRequest;
import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.repository.AgentRepository;
import com.example.qlbds.user_service.repository.AgentRequestRepository;
import com.example.qlbds.user_service.repository.OwnerRepository;
import com.example.qlbds.user_service.repository.UserRepository;
import com.example.qlbds.user_service.service.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final AgentRepository agentRepository;
    private final AgentRequestRepository agentRequestRepository;

    // ==================== OWNER ====================

    @Override
    @Transactional
    public OwnerResponse becomeOwner(BecomeOwnerRequest request) {
        User user = currentUserService.getCurrentUser();

        // Đã là Owner hoặc Agent/Admin rồi → trả về hoặc báo lỗi
        if (ownerRepository.existsByUser(user)) {
            Owner existing = ownerRepository.findByUser(user).orElseThrow();
            log.info("User [{}] đã là Owner", user.getUsername());
            return toOwnerResponse(existing);
        }

        // Tạo Owner record
        Owner owner = Owner.builder()
                .user(user)
                .address(request.address())
                .description(request.description())
                .build();
        ownerRepository.save(owner);

        // Cập nhật role (nếu đang là USER thì chuyển thành OWNER)
        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.OWNER);
            userRepository.save(user);
        }

        log.info("User [{}] đã trở thành Owner", user.getUsername());
        return toOwnerResponse(owner);
    }

    // ==================== AGENT REQUEST ====================

    @Override
    @Transactional
    public AgentRequestResponse submitAgentRequest(BecomeAgentRequest request) {
        User user = currentUserService.getCurrentUser();

        // Không cho phép nếu đã là AGENT rồi
        if (user.getRole() == UserRole.AGENT) {
            throw new IllegalStateException("Bạn đã là môi giới viên rồi");
        }

        // Không cho phép nếu đang có request PENDING
        if (agentRequestRepository.existsByUserAndStatus(user, AgentRequestStatus.PENDING)) {
            throw new IllegalStateException("Bạn đã có yêu cầu đang chờ duyệt, vui lòng đợi Admin xem xét");
        }

        AgentRequest agentRequest = AgentRequest.builder()
                .user(user)
                .licenseNumber(request.licenseNumber())
                .agencyName(request.agencyName())
                .note(request.note())
                .status(AgentRequestStatus.PENDING)
                .build();

        agentRequestRepository.save(agentRequest);
        log.info("User [{}] đã gửi yêu cầu làm Agent", user.getUsername());
        return toAgentRequestResponse(agentRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentRequestResponse> getPendingAgentRequests() {
        return agentRequestRepository
                .findAllByStatus(AgentRequestStatus.PENDING)
                .stream()
                .map(this::toAgentRequestResponse)
                .toList();
    }

    @Override
    @Transactional
    public AgentRequestResponse reviewAgentRequest(Long requestId, ReviewAgentRequest review) {
        AgentRequest agentRequest = agentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy yêu cầu với ID: " + requestId));

        if (agentRequest.getStatus() != AgentRequestStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý rồi");
        }

        if (review.approved()) {
            // 1. Cập nhật trạng thái request
            agentRequest.setStatus(AgentRequestStatus.APPROVED);
            agentRequest.setAdminNote(review.adminNote());

            // 2. Tạo Agent record (nếu chưa có)
            User user = agentRequest.getUser();
            if (!agentRepository.existsByUser(user)) {
                Agent agent = Agent.builder()
                        .user(user)
                        .agencyName(agentRequest.getAgencyName())
                        .licenseNumber(agentRequest.getLicenseNumber())
                        .build();
                agentRepository.save(agent);
            }

            // 3. Cập nhật role → AGENT
            User user2 = agentRequest.getUser();
            user2.setRole(UserRole.AGENT);
            userRepository.save(user2);

            log.info("Admin đã duyệt Agent request [{}] cho user [{}]",
                    requestId, agentRequest.getUser().getUsername());
        } else {
            agentRequest.setStatus(AgentRequestStatus.REJECTED);
            agentRequest.setAdminNote(review.adminNote());
            log.info("Admin đã từ chối Agent request [{}]", requestId);
        }

        agentRequestRepository.save(agentRequest);
        return toAgentRequestResponse(agentRequest);
    }

    // ==================== Mappers ====================

    private OwnerResponse toOwnerResponse(Owner owner) {
        return new OwnerResponse(
                owner.getId(),
                owner.getUser().getId(),
                owner.getUser().getUsername(),
                owner.getUser().getFullName(),
                owner.getAddress(),
                owner.getDescription()
        );
    }

    private AgentRequestResponse toAgentRequestResponse(AgentRequest r) {
        return new AgentRequestResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getUsername(),
                r.getAgencyName(),
                r.getLicenseNumber(),
                r.getNote(),
                r.getStatus(),
                r.getAdminNote(),
                r.getCreatedAt()
        );
    }
}
