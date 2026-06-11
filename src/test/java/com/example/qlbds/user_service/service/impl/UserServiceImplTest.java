package com.example.qlbds.user_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.qlbds.auth_service.repository.RefreshTokenRepository;
import com.example.qlbds.common.exception.InvalidResourceException;
import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.config.CurrentUserService;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.property_service.repository.PropertyRepository;
import com.example.qlbds.shared.entity.enums.PropertyStatus;
import com.example.qlbds.shared.entity.enums.UserRole;
import com.example.qlbds.user_service.dto.*;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.AgentRequest;
import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.mapper.UserResponseMapper;
import com.example.qlbds.user_service.repository.AgentRepository;
import com.example.qlbds.user_service.repository.AgentRequestRepository;
import com.example.qlbds.user_service.repository.OwnerRepository;
import com.example.qlbds.user_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OwnerRepository ownerRepository;
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private AgentRequestRepository agentRequestRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private UserResponseMapper userResponseMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private Owner mockOwner;
    private Agent mockAgent;
    private Property mockProperty;
    private AgentRequest mockAgentRequest;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .role(UserRole.USER)
                .isDeleted(false)
                .build();

        mockOwner = Owner.builder()
                .id(1L)
                .user(mockUser)
                .isDeleted(false)
                .build();

        mockAgent = Agent.builder()
                .id(1L)
                .user(mockUser)
                .isDeleted(false)
                .build();

        mockProperty = Property.builder()
                .id(1L)
                .isDeleted(false)
                .status(PropertyStatus.PENDING)
                .visibility(false)
                .build();
                
        mockAgentRequest = AgentRequest.builder()
                .id(1L)
                .isDeleted(false)
                .build();
    }

    // ==================== getAllUsers ====================

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        UserSummaryResponse expectedResponse = new UserSummaryResponse(1L, "testuser", "email", "name", UserRole.USER, true);
        when(userResponseMapper.toUserSummaryResponse(mockUser)).thenReturn(expectedResponse);

        List<UserSummaryResponse> list = userService.getAllUsers();

        assertEquals(1, list.size());
        assertEquals(expectedResponse, list.get(0));
    }

    // ==================== me ====================

    @Test
    void me_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        UserProfileResponse expectedResponse = new UserProfileResponse(1L, "testuser", "email", "name", "phone", UserRole.USER, true, null, null);
        when(userResponseMapper.toUserProfileResponse(mockUser)).thenReturn(expectedResponse);

        UserProfileResponse response = userService.me();

        assertEquals(expectedResponse, response);
    }

    // ==================== updateProfile ====================

    @Test
    void updateProfile_Success_BasicInfoOnly() {
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        UpdateProfileRequest request = new UpdateProfileRequest("New Name", "0123456789", null, null);

        userService.updateProfile(request);

        assertEquals("New Name", mockUser.getFullName());
        assertEquals("0123456789", mockUser.getPhone());
        verify(userRepository).save(mockUser);
        verify(agentRepository, never()).save(any());
        verify(ownerRepository, never()).save(any());
    }

    @Test
    void updateProfile_Success_UpdateAgentIfExists() {
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(agentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockAgent));
        UpdateProfileRequest request = new UpdateProfileRequest("Name", "Phone", new UpdateProfileRequest.UpdateAgentInfo("NewL", "NewAgency"), null);

        userService.updateProfile(request);

        assertEquals("NewL", mockAgent.getLicenseNumber());
        assertEquals("NewAgency", mockAgent.getAgencyName());
        verify(agentRepository).save(mockAgent);
    }

    @Test
    void updateProfile_Success_CreateAgentIfRoleIsAgent() {
        mockUser.setRole(UserRole.AGENT);
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(agentRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        UpdateProfileRequest request = new UpdateProfileRequest("Name", "Phone", new UpdateProfileRequest.UpdateAgentInfo("NewL", "NewAgency"), null);

        userService.updateProfile(request);

        verify(agentRepository).save(any(Agent.class));
    }

    @Test
    void updateProfile_Success_SkipCreateAgentIfRoleNotAgent() {
        mockUser.setRole(UserRole.USER);
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(agentRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        UpdateProfileRequest request = new UpdateProfileRequest("Name", "Phone", new UpdateProfileRequest.UpdateAgentInfo("NewL", "NewAgency"), null);

        userService.updateProfile(request);

        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    void updateProfile_Success_UpdateOwnerIfExists() {
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(ownerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockOwner));
        UpdateProfileRequest request = new UpdateProfileRequest("Name", "Phone", null, new UpdateProfileRequest.UpdateOwnerInfo("Addr", "Desc"));

        userService.updateProfile(request);

        assertEquals("Addr", mockOwner.getAddress());
        assertEquals("Desc", mockOwner.getDescription());
        verify(ownerRepository).save(mockOwner);
    }

    @Test
    void updateProfile_Success_CreateOwnerIfRoleIsOwner() {
        mockUser.setRole(UserRole.OWNER);
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(ownerRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        UpdateProfileRequest request = new UpdateProfileRequest("Name", "Phone", null, new UpdateProfileRequest.UpdateOwnerInfo("Addr", "Desc"));

        userService.updateProfile(request);

        verify(ownerRepository).save(any(Owner.class));
    }

    // ==================== deleteUser ====================

    @Test
    void deleteUser_Success_DeletesEverything() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(agentRepository.findByUserAndIsDeletedFalse(mockUser)).thenReturn(Optional.of(mockAgent));
        when(ownerRepository.findByUserAndIsDeletedFalse(mockUser)).thenReturn(Optional.of(mockOwner));
        when(propertyRepository.findAllByAgent(mockAgent)).thenReturn(List.of(mockProperty));
        when(propertyRepository.findAllByOwner(mockOwner)).thenReturn(List.of(mockProperty));
        when(agentRequestRepository.findTopByUserAndIsDeletedFalseOrderByCreatedAtDesc(mockUser)).thenReturn(Optional.of(mockAgentRequest));

        userService.deleteUser(1L);

        assertTrue(mockUser.getIsDeleted());
        assertTrue(mockAgent.getIsDeleted());
        assertTrue(mockOwner.getIsDeleted());
        assertTrue(mockProperty.getIsDeleted());
        assertEquals(PropertyStatus.DELETED, mockProperty.getStatus());
        assertTrue(mockAgentRequest.getIsDeleted());
        verify(refreshTokenRepository).deleteByUser(mockUser);
    }

    @Test
    void deleteUser_Throws_WhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void deleteUser_Throws_WhenAlreadyDeleted() {
        mockUser.setIsDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
    }

    // ==================== restoreUser ====================

    @Test
    void restoreUser_Success_RoleAgent() {
        mockUser.setIsDeleted(true);
        mockUser.setRole(UserRole.AGENT);
        mockAgent.setIsDeleted(true);
        mockProperty.setIsDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(agentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockAgent));
        when(propertyRepository.findAllByAgent(mockAgent)).thenReturn(List.of(mockProperty));

        userService.restoreUser(1L);

        assertFalse(mockUser.getIsDeleted());
        assertFalse(mockAgent.getIsDeleted());
        assertFalse(mockProperty.getIsDeleted());
        assertEquals(PropertyStatus.PENDING, mockProperty.getStatus());
    }

    @Test
    void restoreUser_Success_RoleOwner() {
        mockUser.setIsDeleted(true);
        mockUser.setRole(UserRole.OWNER);
        mockOwner.setIsDeleted(true);
        mockProperty.setIsDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(ownerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockOwner));
        when(propertyRepository.findAllByOwner(mockOwner)).thenReturn(List.of(mockProperty));

        userService.restoreUser(1L);

        assertFalse(mockUser.getIsDeleted());
        assertFalse(mockOwner.getIsDeleted());
        assertFalse(mockProperty.getIsDeleted());
        assertEquals(PropertyStatus.PENDING, mockProperty.getStatus());
    }

    @Test
    void restoreUser_Throws_WhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.restoreUser(1L));
    }

    @Test
    void restoreUser_Throws_WhenNotDeleted() {
        mockUser.setIsDeleted(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        assertThrows(InvalidResourceException.class, () -> userService.restoreUser(1L));
    }

    // ==================== changeRole ====================

    @Test
    void changeRole_Success_SameRole() {
        mockUser.setRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        AdminChangeRoleRequest request = new AdminChangeRoleRequest(UserRole.USER, null, null, null, null);

        userService.changeRole(1L, request);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeRole_Success_OwnerToAgent() {
        mockUser.setRole(UserRole.OWNER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(ownerRepository.findByUserAndIsDeletedFalse(mockUser)).thenReturn(Optional.of(mockOwner));
        when(agentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockAgent)); // Restore existing
        AdminChangeRoleRequest request = new AdminChangeRoleRequest(UserRole.AGENT, null, null, "Lic", "Ag");

        userService.changeRole(1L, request);

        assertTrue(mockOwner.getIsDeleted());
        verify(ownerRepository).save(mockOwner);
        assertFalse(mockAgent.getIsDeleted());
        assertEquals("Lic", mockAgent.getLicenseNumber());
        verify(agentRepository).save(mockAgent);
        assertEquals(UserRole.AGENT, mockUser.getRole());
        verify(userRepository).save(mockUser);
    }
    
    @Test
    void changeRole_Success_AgentToOwner() {
        mockUser.setRole(UserRole.AGENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(agentRepository.findByUserAndIsDeletedFalse(mockUser)).thenReturn(Optional.of(mockAgent));
        when(ownerRepository.findByUser(mockUser)).thenReturn(Optional.empty()); // Create new
        AdminChangeRoleRequest request = new AdminChangeRoleRequest(UserRole.OWNER, "Addr", "Desc", null, null);

        userService.changeRole(1L, request);

        assertTrue(mockAgent.getIsDeleted());
        verify(agentRepository).save(mockAgent);
        verify(ownerRepository).save(any(Owner.class));
        assertEquals(UserRole.OWNER, mockUser.getRole());
        verify(userRepository).save(mockUser);
    }

    @Test
    void changeRole_Throws_WhenAgentMissingLicense() {
        mockUser.setRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(agentRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        AdminChangeRoleRequest request = new AdminChangeRoleRequest(UserRole.AGENT, null, null, null, null);

        assertThrows(InvalidResourceException.class, () -> userService.changeRole(1L, request));
    }
    
    @Test
    void changeRole_Throws_WhenUserDeleted() {
        mockUser.setIsDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        AdminChangeRoleRequest request = new AdminChangeRoleRequest(UserRole.USER, null, null, null, null);

        assertThrows(ResourceNotFoundException.class, () -> userService.changeRole(1L, request));
    }
}
