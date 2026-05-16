package com.example.qlbds.user_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbds.common.exception.InvalidResourceException;
import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.common.util.SlugUtil;
import com.example.qlbds.config.CurrentUserService;
import com.example.qlbds.shared.entity.enums.UserRole;
import com.example.qlbds.user_service.dto.AdminChangeRoleRequest;
import com.example.qlbds.user_service.dto.UpdateProfileRequest;
import com.example.qlbds.user_service.dto.UserProfileResponse;
import com.example.qlbds.user_service.dto.UserSummaryResponse;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.mapper.UserResponseMapper;
import com.example.qlbds.user_service.repository.AgentRepository;
import com.example.qlbds.user_service.repository.AgentRequestRepository;
import com.example.qlbds.user_service.repository.OwnerRepository;
import com.example.qlbds.user_service.repository.UserRepository;
import com.example.qlbds.auth_service.repository.RefreshTokenRepository;
import com.example.qlbds.property_service.repository.PropertyRepository;
import com.example.qlbds.user_service.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final AgentRepository agentRepository;
    private final AgentRequestRepository agentRequestRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PropertyRepository propertyRepository;
    private final CurrentUserService currentUserService;
    private final UserResponseMapper userResponseMapper;

    // Lấy danh sách tất cả người dùng (ADMIN)
    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userResponseMapper::toUserSummaryResponse)
                .toList();
    }

    // Lấy thông tin cá nhân của người dùng hiện tại
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse me() throws ResourceNotFoundException {
        User user = currentUserService.getCurrentUser();
        return userResponseMapper.toUserProfileResponse(user);
    }

    // Cập nhật thông tin profile của user đang đăng nhập
    @Override
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) throws ResourceNotFoundException {
        User user = currentUserService.getCurrentUser();

        // 1. Cập nhật thông tin cơ bản
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        userRepository.save(user);

        // 2. Cập nhật thông tin Agent nếu có
        if (request.agent() != null) {
            agentRepository.findByUser(user).ifPresentOrElse(
                agent -> {
                    if (request.agent().licenseNumber() != null) agent.setLicenseNumber(request.agent().licenseNumber());
                    if (request.agent().agencyName() != null)    agent.setAgencyName(request.agent().agencyName());
                    
                    String slugBase = (agent.getAgencyName() != null && !agent.getAgencyName().isBlank())
                            ? agent.getAgencyName()
                            : user.getUsername();
                    agent.setSlug(SlugUtil.toSlug(slugBase));
                    agentRepository.save(agent);
                },
                () -> {
                    // Nếu chưa có hồ sơ Agent nhưng request có dữ liệu -> chỉ tạo nếu role là AGENT
                    if (user.getRole() == UserRole.AGENT) {
                        String slugBase = (request.agent().agencyName() != null && !request.agent().agencyName().isBlank())
                                ? request.agent().agencyName()
                                : user.getUsername();
                        Agent newAgent = Agent.builder()
                                .user(user)
                                .licenseNumber(request.agent().licenseNumber())
                                .agencyName(request.agent().agencyName())
                                .slug(SlugUtil.toSlug(slugBase))
                                .build();
                        agentRepository.save(newAgent);
                    }
                }
            );
        }

        // 3. Cập nhật thông tin Owner nếu có
        if (request.owner() != null) {
            ownerRepository.findByUser(user).ifPresentOrElse(
                owner -> {
                    if (request.owner().address() != null)     owner.setAddress(request.owner().address());
                    if (request.owner().description() != null) owner.setDescription(request.owner().description());
                    ownerRepository.save(owner);
                },
                () -> {
                    // Nếu chưa có hồ sơ Owner nhưng request có dữ liệu -> chỉ tạo nếu role là OWNER
                    if (user.getRole() == UserRole.OWNER) {
                        Owner newOwner = Owner.builder()
                                .user(user)
                                .address(request.owner().address())
                                .description(request.owner().description())
                                .build();
                        ownerRepository.save(newOwner);
                    }
                }
            );
        }

        log.info("User [{}] đã cập nhật thông tin cá nhân", user.getUsername());
        return userResponseMapper.toUserProfileResponse(user);
    }

    // Xóa user (soft-delete)
    @Override
    @Transactional
    public void deleteUser(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));

        if (user.getIsDeleted()) {
            throw new ResourceNotFoundException("Người dùng", userId);
        }

        user.setIsDeleted(true);
        userRepository.save(user);

        // 1. Xóa mềm hồ sơ Agent/Owner nếu có
        agentRepository.findByUserAndIsDeletedFalse(user).ifPresent(agent -> {
            agent.setIsDeleted(true);
            agentRepository.save(agent);
            // Xóa luôn các Property do Agent này quản lý
            propertyRepository.findAllByAgent(agent).forEach(p -> {
                p.setIsDeleted(true);
                propertyRepository.save(p);
            });
        });
        ownerRepository.findByUserAndIsDeletedFalse(user).ifPresent(owner -> {
            owner.setIsDeleted(true);
            ownerRepository.save(owner);
            // Xóa luôn các Property của Owner này
            propertyRepository.findAllByOwner(owner).forEach(p -> {
                p.setIsDeleted(true);
                propertyRepository.save(p);
            });
        });

        // 2. Xóa mềm các AgentRequest đang có (nếu có)
        agentRequestRepository.findTopByUserAndIsDeletedFalseOrderByCreatedAtDesc(user).ifPresent(request -> {
            request.setIsDeleted(true);
            agentRequestRepository.save(request);
        });

        // 3. Thu hồi tất cả Refresh Tokens (đăng xuất khỏi mọi thiết bị)
        refreshTokenRepository.deleteByUser(user);

        log.info("Admin đã xóa mềm toàn bộ dữ liệu liên quan đến user [{}]", userId);
    }

    // Khôi phục user (soft-restore)
    @Override
    @Transactional
    public void restoreUser(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));

        if (!user.getIsDeleted()) {
            throw new InvalidResourceException("Người dùng", "Tài khoản chưa bị xóa");
        }

        user.setIsDeleted(false);
        userRepository.save(user);

        // Khôi phục hồ sơ Agent/Owner nếu có và tương ứng với role hiện tại
        if (user.getRole() == UserRole.AGENT) {
            agentRepository.findByUser(user).ifPresent(agent -> {
                agent.setIsDeleted(false);
                agentRepository.save(agent);
                // Khôi phục các Property liên quan
                propertyRepository.findAllByAgent(agent).forEach(p -> {
                    p.setIsDeleted(false);
                    propertyRepository.save(p);
                });
            });
        } else if (user.getRole() == UserRole.OWNER) {
            ownerRepository.findByUser(user).ifPresent(owner -> {
                owner.setIsDeleted(false);
                ownerRepository.save(owner);
                // Khôi phục các Property liên quan
                propertyRepository.findAllByOwner(owner).forEach(p -> {
                    p.setIsDeleted(false);
                    propertyRepository.save(p);
                });
            });
        }

        log.info("Admin đã khôi phục user [{}]", userId);
    }

    /**
     * Admin đổi role của một user.
     */
    @Override
    @Transactional
    public UserSummaryResponse changeRole(Long userId, AdminChangeRoleRequest request)
            throws ResourceNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));

        if (user.getIsDeleted()) {
            throw new ResourceNotFoundException("Người dùng", userId);
        }

        UserRole fromRole = user.getRole();
        UserRole toRole = request.role();

        if (fromRole == toRole) {
            return userResponseMapper.toUserSummaryResponse(user);
        }

        // 1. Xóa mềm record cũ nếu đang là OWNER hoặc AGENT
        if (fromRole == UserRole.OWNER) {
            ownerRepository.findByUserAndIsDeletedFalse(user).ifPresent(owner -> {
                owner.setIsDeleted(true);
                ownerRepository.save(owner);
                log.info("Đã xóa mềm Owner record của user [{}]", user.getUsername());
            });
        }

        if (fromRole == UserRole.AGENT) {
            agentRepository.findByUserAndIsDeletedFalse(user).ifPresent(agent -> {
                agent.setIsDeleted(true);
                agentRepository.save(agent);
                log.info("Đã xóa mềm Agent record của user [{}]", user.getUsername());
            });
        }

        // 2. Tạo hoặc restore record mới theo role đích
        switch (toRole) {
            case OWNER -> handleBecomeOwner(user, request);
            case AGENT -> handleBecomeAgent(user, request);
            default -> { /* USER, ADMIN không cần record phụ */ }
        }

        // 3. Cập nhật role trên bảng users
        user.setRole(toRole);
        User saved = userRepository.save(user);

        log.info("Admin đã đổi role user [{}] từ {} → {}", userId, fromRole, toRole);
        return userResponseMapper.toUserSummaryResponse(saved);
    }

    // ==================== Private helpers ====================

    private void handleBecomeOwner(User user, AdminChangeRoleRequest request) {
        ownerRepository.findByUser(user).ifPresentOrElse(
                existing -> {
                    existing.setIsDeleted(false);
                    if (request.address() != null && !request.address().isBlank())
                        existing.setAddress(request.address());
                    if (request.description() != null && !request.description().isBlank())
                        existing.setDescription(request.description());
                    ownerRepository.save(existing);
                    log.info("Đã restore Owner record của user [{}]", user.getUsername());
                },
                () -> {
                    Owner owner = Owner.builder()
                            .user(user)
                            .address(request.address())
                            .description(request.description())
                            .build();
                    ownerRepository.save(owner);
                    log.info("Đã tạo mới Owner record cho user [{}]", user.getUsername());
                });
    }

    private void handleBecomeAgent(User user, AdminChangeRoleRequest request) {
        agentRepository.findByUser(user).ifPresentOrElse(
                existing -> {
                    existing.setIsDeleted(false);
                    if (request.licenseNumber() != null && !request.licenseNumber().isBlank()) {
                        existing.setLicenseNumber(request.licenseNumber());
                    }
                    if (request.agencyName() != null && !request.agencyName().isBlank()) {
                        existing.setAgencyName(request.agencyName());
                    }
                    String slugBase = (existing.getAgencyName() != null && !existing.getAgencyName().isBlank())
                            ? existing.getAgencyName()
                            : user.getUsername();
                    existing.setSlug(SlugUtil.toSlug(slugBase));
                    agentRepository.save(existing);
                    log.info("Đã restore Agent record của user [{}]", user.getUsername());
                },
                () -> {
                    if (request.licenseNumber() == null || request.licenseNumber().isBlank()) {
                        throw new InvalidResourceException("Agent", "licenseNumber là bắt buộc khi tạo mới hồ sơ Môi giới");
                    }
                    String slugBase = (request.agencyName() != null && !request.agencyName().isBlank())
                            ? request.agencyName()
                            : user.getUsername();
                    Agent agent = Agent.builder()
                            .user(user)
                            .licenseNumber(request.licenseNumber())
                            .agencyName(request.agencyName())
                            .slug(SlugUtil.toSlug(slugBase))
                            .build();
                    agentRepository.save(agent);
                    log.info("Đã tạo mới Agent record cho user [{}]", user.getUsername());
                });
    }
}
