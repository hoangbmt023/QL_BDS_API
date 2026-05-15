package com.example.qlbds.user_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbds.common.exception.DuplicateResourceException;
import com.example.qlbds.common.exception.InvalidResourceException;
import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.common.util.SlugUtil;
import com.example.qlbds.config.CurrentUserService;
import com.example.qlbds.auth_service.service.EmailService;
import com.example.qlbds.shared.entity.enums.AgentRequestStatus;
import com.example.qlbds.shared.entity.enums.UserRole;
import com.example.qlbds.user_service.dto.*;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.AgentRequest;
import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.mapper.RoleResponseMapper;
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
    private final EmailService emailService;
    private final RoleResponseMapper roleResponseMapper;

    // ==================== OWNER ====================

    // Nâng cấp tài khoản thành Owner (Chủ nhà) — chỉ USER mới được gọi
    @Override
    @Transactional
    public OwnerResponse becomeOwner(BecomeOwnerRequest request) {
        User user = currentUserService.getCurrentUser();

        // Chỉ USER mới được tự đăng ký thành Owner
        if (user.getRole() != UserRole.USER) {
            throw new InvalidResourceException("Tài khoản",
                    "chỉ USER mới có thể đăng ký thành chủ nhà. Role hiện tại: " + user.getRole());
        }

        // Kiểm tra xem đã từng có hồ sơ Owner chưa (kể cả đã bị xóa mềm)
        Owner owner = ownerRepository.findByUser(user).orElse(null);

        if (owner != null) {
            // Đã có hồ sơ
            if (!owner.getIsDeleted()) {
                log.info("User [{}] đã là Owner (active)", user.getUsername());
                return roleResponseMapper.toOwnerResponse(owner);
            }
            // Nếu đã xóa mềm → Khôi phục hồ sơ cũ
            owner.setIsDeleted(false);
            if (request.address() != null && !request.address().isBlank()) {
                owner.setAddress(request.address());
            }
            if (request.description() != null && !request.description().isBlank()) {
                owner.setDescription(request.description());
            }
            ownerRepository.save(owner);
        } else {
            // Chưa từng có hồ sơ → Tạo mới
            owner = Owner.builder()
                    .user(user)
                    .address(request.address())
                    .description(request.description())
                    .build();
            ownerRepository.save(owner);
        }

        // Cập nhật role → OWNER
        user.setRole(UserRole.OWNER);
        userRepository.save(user);

        log.info("User [{}] đã trở thành Owner", user.getUsername());
        return roleResponseMapper.toOwnerResponse(owner);
    }

    // ==================== AGENT REQUEST ====================

    // Gửi yêu cầu trở thành Agent (Môi giới) — chỉ USER mới được gọi
    @Override
    @Transactional
    public AgentRequestResponse submitAgentRequest(BecomeAgentRequest request) {
        User user = currentUserService.getCurrentUser();

        // Chỉ USER mới được gửi yêu cầu làm Agent
        if (user.getRole() != UserRole.USER) {
            throw new InvalidResourceException("Tài khoản",
                    "chỉ USER mới có thể gửi yêu cầu làm môi giới. Role hiện tại: " + user.getRole());
        }

        // Không cho phép nếu đang có request PENDING
        if (agentRequestRepository.existsByUserAndStatusAndIsDeletedFalse(user, AgentRequestStatus.PENDING)) {
            throw new DuplicateResourceException("AgentRequest",
                    "bạn đã có yêu cầu đang chờ duyệt, vui lòng đợi Admin xem xét");
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
        return roleResponseMapper.toAgentRequestResponse(agentRequest);
    }

    // Lấy danh sách yêu cầu Agent đang chờ duyệt (Admin)
    @Override
    @Transactional(readOnly = true)
    public List<AgentRequestResponse> getPendingAgentRequests() {
        return agentRequestRepository
                .findAllByStatusAndIsDeletedFalse(AgentRequestStatus.PENDING)
                .stream()
                .map(roleResponseMapper::toAgentRequestResponse)
                .toList();
    }

    // Admin duyệt hoặc từ chối yêu cầu làm Agent
    @Override
    @Transactional
    public AgentRequestResponse reviewAgentRequest(Long requestId, ReviewAgentRequest review) {
        AgentRequest agentRequest = agentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentRequest", requestId));

        if (agentRequest.getIsDeleted()) {
            throw new ResourceNotFoundException("AgentRequest", requestId);
        }

        if (agentRequest.getStatus() != AgentRequestStatus.PENDING) {
            throw new InvalidResourceException("AgentRequest",
                    "yêu cầu này đã được xử lý với trạng thái: " + agentRequest.getStatus());
        }

        if (review.approved()) {
            // 1. Cập nhật trạng thái request
            agentRequest.setStatus(AgentRequestStatus.APPROVED);
            agentRequest.setAdminNote(review.adminNote());

            // 2. Tạo Agent record hoặc restore nếu từng có
            User user = agentRequest.getUser();
            agentRepository.findByUser(user).ifPresentOrElse(
                existing -> {
                    existing.setIsDeleted(false);
                    existing.setLicenseNumber(agentRequest.getLicenseNumber());
                    if (agentRequest.getAgencyName() != null) {
                        existing.setAgencyName(agentRequest.getAgencyName());
                    }
                    // Sinh slug ưu tiên từ agencyName, fallback sang username
                    String slugBase = (existing.getAgencyName() != null && !existing.getAgencyName().isBlank())
                            ? existing.getAgencyName()
                            : user.getUsername();
                    existing.setSlug(SlugUtil.toSlug(slugBase));
                    agentRepository.save(existing);
                },
                () -> {
                    String slugBase = (agentRequest.getAgencyName() != null && !agentRequest.getAgencyName().isBlank())
                            ? agentRequest.getAgencyName()
                            : user.getUsername();
                    Agent agent = Agent.builder()
                            .user(user)
                            .agencyName(agentRequest.getAgencyName())
                            .licenseNumber(agentRequest.getLicenseNumber())
                            .slug(SlugUtil.toSlug(slugBase))
                            .build();
                    agentRepository.save(agent);
                }
            );

            // 3. Cập nhật role → AGENT
            user.setRole(UserRole.AGENT);
            userRepository.save(user);

            log.info("Admin đã duyệt AgentRequest [{}] cho user [{}]",
                    requestId, agentRequest.getUser().getUsername());
        } else {
            agentRequest.setStatus(AgentRequestStatus.REJECTED);
            agentRequest.setAdminNote(review.adminNote());
            log.info("Admin đã từ chối AgentRequest [{}]", requestId);
        }

        agentRequestRepository.save(agentRequest);

        // 4. Gửi email thông báo kết quả cho User
        try {
            User user = agentRequest.getUser();
            emailService.sendAgentRequestResultEmail(
                user.getEmail(), 
                user.getFullName(), 
                review.approved(), 
                review.adminNote()
            );
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo kết quả AgentRequest cho user [{}]: {}", 
                agentRequest.getUser().getUsername(), e.getMessage());
            // Không throw exception ở đây để tránh rollback transaction nếu việc gửi mail thất bại
        }

        return roleResponseMapper.toAgentRequestResponse(agentRequest);
    }
}
