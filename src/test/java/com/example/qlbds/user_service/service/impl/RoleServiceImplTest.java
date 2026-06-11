package com.example.qlbds.user_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.qlbds.auth_service.service.EmailService;
import com.example.qlbds.common.exception.DuplicateResourceException;
import com.example.qlbds.common.exception.InvalidResourceException;
import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.config.CurrentUserService;
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

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OwnerRepository ownerRepository;
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private AgentRequestRepository agentRequestRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private RoleResponseMapper roleResponseMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private User mockUser;
    private Owner mockOwner;
    private Agent mockAgent;
    private AgentRequest mockAgentRequest;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
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

        mockAgentRequest = AgentRequest.builder()
                .id(1L)
                .user(mockUser)
                .status(AgentRequestStatus.PENDING)
                .isDeleted(false)
                .build();
    }

    // ==================== becomeOwner ====================

    @Test
    void becomeOwner_Success_NewOwner() {
        BecomeOwnerRequest request = new BecomeOwnerRequest("123 Street", "Desc");
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(ownerRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        OwnerResponse expectedResponse = new OwnerResponse(1L, 1L, "testuser", "Test User", "123 Street", "Desc");
        when(roleResponseMapper.toOwnerResponse(any(Owner.class))).thenReturn(expectedResponse);

        OwnerResponse response = roleService.becomeOwner(request);

        verify(ownerRepository).save(any(Owner.class));
        verify(userRepository).save(mockUser);
        assertEquals(UserRole.OWNER, mockUser.getRole());
        assertEquals(expectedResponse, response);
    }

    @Test
    void becomeOwner_Success_RestoreDeletedOwner() {
        mockOwner.setIsDeleted(true);
        BecomeOwnerRequest request = new BecomeOwnerRequest("123 Street", "Desc");
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(ownerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockOwner));
        OwnerResponse expectedResponse = new OwnerResponse(1L, 1L, "testuser", "Test User", "123 Street", "Desc");
        when(roleResponseMapper.toOwnerResponse(any(Owner.class))).thenReturn(expectedResponse);

        OwnerResponse response = roleService.becomeOwner(request);

        assertFalse(mockOwner.getIsDeleted());
        assertEquals("123 Street", mockOwner.getAddress());
        verify(ownerRepository).save(mockOwner);
        verify(userRepository).save(mockUser);
        assertEquals(UserRole.OWNER, mockUser.getRole());
        assertEquals(expectedResponse, response);
    }

    @Test
    void becomeOwner_Success_RestoreDeletedOwner_BlankFields() {
        mockOwner.setIsDeleted(true);
        mockOwner.setAddress("Old Address");
        mockOwner.setDescription("Old Desc");
        BecomeOwnerRequest request = new BecomeOwnerRequest("", null);
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(ownerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockOwner));
        OwnerResponse expectedResponse = new OwnerResponse(1L, 1L, "testuser", "Test User", "Old Address", "Old Desc");
        when(roleResponseMapper.toOwnerResponse(any(Owner.class))).thenReturn(expectedResponse);

        OwnerResponse response = roleService.becomeOwner(request);

        assertFalse(mockOwner.getIsDeleted());
        assertEquals("Old Address", mockOwner.getAddress()); // Address not overwritten
        assertEquals("Old Desc", mockOwner.getDescription()); // Description not overwritten
        verify(ownerRepository).save(mockOwner);
        verify(userRepository).save(mockUser);
        assertEquals(UserRole.OWNER, mockUser.getRole());
        assertEquals(expectedResponse, response);
    }

    @Test
    void becomeOwner_Success_AlreadyOwner() {
        BecomeOwnerRequest request = new BecomeOwnerRequest("123 Street", "Desc");
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(ownerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockOwner));
        OwnerResponse expectedResponse = new OwnerResponse(1L, 1L, "testuser", "Test User", "Old Street", "Old Desc");
        when(roleResponseMapper.toOwnerResponse(mockOwner)).thenReturn(expectedResponse);

        OwnerResponse response = roleService.becomeOwner(request);

        verify(ownerRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        assertEquals(expectedResponse, response);
    }

    @Test
    void becomeOwner_Throws_WhenNotUserRole() {
        mockUser.setRole(UserRole.AGENT);
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        BecomeOwnerRequest request = new BecomeOwnerRequest("123 Street", "Desc");

        assertThrows(InvalidResourceException.class, () -> roleService.becomeOwner(request));
    }

    // ==================== submitAgentRequest ====================

    @Test
    void submitAgentRequest_Success() {
        BecomeAgentRequest request = new BecomeAgentRequest("L123", "Agency", "Note");
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(agentRequestRepository.existsByUserAndStatusAndIsDeletedFalse(mockUser, AgentRequestStatus.PENDING))
                .thenReturn(false);
        AgentRequestResponse expectedResponse = new AgentRequestResponse(1L, 1L, "testuser", "Agency", "L123", "Note",
                AgentRequestStatus.PENDING, null, null, null);
        when(roleResponseMapper.toAgentRequestResponse(any(AgentRequest.class))).thenReturn(expectedResponse);

        AgentRequestResponse response = roleService.submitAgentRequest(request);

        ArgumentCaptor<AgentRequest> captor = ArgumentCaptor.forClass(AgentRequest.class);
        verify(agentRequestRepository).save(captor.capture());
        assertEquals("L123", captor.getValue().getLicenseNumber());
        assertEquals("Agency", captor.getValue().getAgencyName());
        assertEquals("Note", captor.getValue().getNote());
        assertEquals(AgentRequestStatus.PENDING, captor.getValue().getStatus());
        
        assertEquals(expectedResponse, response);
    }

    @Test
    void submitAgentRequest_Throws_WhenNotUserRole() {
        mockUser.setRole(UserRole.OWNER);
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        BecomeAgentRequest request = new BecomeAgentRequest("L123", "Agency", "Note");

        assertThrows(InvalidResourceException.class, () -> roleService.submitAgentRequest(request));
    }

    @Test
    void submitAgentRequest_Throws_WhenPendingRequestExists() {
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);
        when(agentRequestRepository.existsByUserAndStatusAndIsDeletedFalse(mockUser, AgentRequestStatus.PENDING))
                .thenReturn(true);
        BecomeAgentRequest request = new BecomeAgentRequest("L123", "Agency", "Note");

        assertThrows(DuplicateResourceException.class, () -> roleService.submitAgentRequest(request));
    }

    // ==================== getPendingAgentRequests ====================

    @Test
    void getPendingAgentRequests_Success() {
        when(agentRequestRepository.findAllByStatusAndIsDeletedFalse(AgentRequestStatus.PENDING))
                .thenReturn(List.of(mockAgentRequest));
        AgentRequestResponse expectedResponse = new AgentRequestResponse(1L, 1L, "testuser", "Agency", "L123", "Note",
                AgentRequestStatus.PENDING, null, null, null);
        when(roleResponseMapper.toAgentRequestResponse(mockAgentRequest)).thenReturn(expectedResponse);

        List<AgentRequestResponse> list = roleService.getPendingAgentRequests();

        assertEquals(1, list.size());
        assertEquals(expectedResponse, list.get(0));
    }

    @Test
    void getPendingAgentRequests_EmptyList() {
        when(agentRequestRepository.findAllByStatusAndIsDeletedFalse(AgentRequestStatus.PENDING))
                .thenReturn(List.of());

        List<AgentRequestResponse> list = roleService.getPendingAgentRequests();

        assertTrue(list.isEmpty());
    }

    // ==================== reviewAgentRequest ====================

    @Test
    void reviewAgentRequest_Success_Approved_NewAgent() {
        mockAgentRequest.setLicenseNumber("L123");
        mockAgentRequest.setAgencyName("Agency");
        ReviewAgentRequest request = new ReviewAgentRequest(true, "Welcome");
        when(agentRequestRepository.findById(1L)).thenReturn(Optional.of(mockAgentRequest));
        when(agentRepository.findByUser(mockUser)).thenReturn(Optional.empty());

        roleService.reviewAgentRequest(1L, request);

        assertEquals(AgentRequestStatus.APPROVED, mockAgentRequest.getStatus());
        assertEquals("Welcome", mockAgentRequest.getAdminNote());
        
        ArgumentCaptor<Agent> captor = ArgumentCaptor.forClass(Agent.class);
        verify(agentRepository).save(captor.capture());
        assertEquals("L123", captor.getValue().getLicenseNumber());
        assertEquals("Agency", captor.getValue().getAgencyName());
        assertEquals("agency", captor.getValue().getSlug());

        verify(agentRequestRepository).save(mockAgentRequest);
        verify(userRepository).save(mockUser);
        assertEquals(UserRole.AGENT, mockUser.getRole());
        verify(emailService).sendAgentRequestResultEmail(mockUser.getEmail(), mockUser.getFullName(), true, "Welcome");
    }

    @Test
    void reviewAgentRequest_Success_Approved_ExistingAgent() {
        mockAgent.setIsDeleted(true);
        mockAgentRequest.setLicenseNumber("L123");
        mockAgentRequest.setAgencyName("Agency");
        ReviewAgentRequest request = new ReviewAgentRequest(true, "Welcome back");
        when(agentRequestRepository.findById(1L)).thenReturn(Optional.of(mockAgentRequest));
        when(agentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockAgent));

        roleService.reviewAgentRequest(1L, request);

        assertEquals(AgentRequestStatus.APPROVED, mockAgentRequest.getStatus());
        assertFalse(mockAgent.getIsDeleted());
        assertEquals("L123", mockAgent.getLicenseNumber());
        assertEquals("Agency", mockAgent.getAgencyName());
        assertEquals("agency", mockAgent.getSlug());
        
        verify(agentRepository).save(mockAgent);
        verify(agentRequestRepository).save(mockAgentRequest);
        verify(userRepository).save(mockUser);
        assertEquals(UserRole.AGENT, mockUser.getRole());
        verify(emailService).sendAgentRequestResultEmail(mockUser.getEmail(), mockUser.getFullName(), true,
                "Welcome back");
    }

    @Test
    void reviewAgentRequest_Success_Approved_ExistingAgent_NullAgencyName() {
        mockAgent.setIsDeleted(true);
        mockAgent.setAgencyName("Old Agency");
        mockAgentRequest.setLicenseNumber("L123");
        mockAgentRequest.setAgencyName(null);
        ReviewAgentRequest request = new ReviewAgentRequest(true, "Welcome back");
        when(agentRequestRepository.findById(1L)).thenReturn(Optional.of(mockAgentRequest));
        when(agentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockAgent));

        roleService.reviewAgentRequest(1L, request);

        assertEquals(AgentRequestStatus.APPROVED, mockAgentRequest.getStatus());
        assertFalse(mockAgent.getIsDeleted());
        assertEquals("L123", mockAgent.getLicenseNumber());
        assertEquals("Old Agency", mockAgent.getAgencyName()); // Should not be overwritten
        assertEquals("old-agency", mockAgent.getSlug());
        
        verify(agentRepository).save(mockAgent);
        verify(agentRequestRepository).save(mockAgentRequest);
        verify(userRepository).save(mockUser);
        assertEquals(UserRole.AGENT, mockUser.getRole());
    }

    @Test
    void reviewAgentRequest_Success_Rejected() {
        ReviewAgentRequest request = new ReviewAgentRequest(false, "Lacking info");
        when(agentRequestRepository.findById(1L)).thenReturn(Optional.of(mockAgentRequest));

        roleService.reviewAgentRequest(1L, request);

        assertEquals(AgentRequestStatus.REJECTED, mockAgentRequest.getStatus());
        assertEquals("Lacking info", mockAgentRequest.getAdminNote());
        verify(agentRepository, never()).save(any());
        verify(agentRequestRepository).save(mockAgentRequest);
        verify(userRepository, never()).save(any());
        verify(emailService).sendAgentRequestResultEmail(mockUser.getEmail(), mockUser.getFullName(), false,
                "Lacking info");
    }

    @Test
    void reviewAgentRequest_EmailException_DoesNotRollback() {
        ReviewAgentRequest request = new ReviewAgentRequest(false, "Lacking info");
        when(agentRequestRepository.findById(1L)).thenReturn(Optional.of(mockAgentRequest));
        doThrow(new RuntimeException("Mail error")).when(emailService).sendAgentRequestResultEmail(any(), any(),
                anyBoolean(), any());

        assertDoesNotThrow(() -> roleService.reviewAgentRequest(1L, request));
        assertEquals(AgentRequestStatus.REJECTED, mockAgentRequest.getStatus());
        verify(agentRequestRepository).save(mockAgentRequest);
    }

    @Test
    void reviewAgentRequest_EmailException_Approved_DoesNotRollback() {
        ReviewAgentRequest request = new ReviewAgentRequest(true, "Welcome");
        when(agentRequestRepository.findById(1L)).thenReturn(Optional.of(mockAgentRequest));
        when(agentRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Mail error")).when(emailService).sendAgentRequestResultEmail(any(), any(),
                anyBoolean(), any());

        assertDoesNotThrow(() -> roleService.reviewAgentRequest(1L, request));
        assertEquals(AgentRequestStatus.APPROVED, mockAgentRequest.getStatus());
        assertEquals(UserRole.AGENT, mockUser.getRole());
        verify(agentRequestRepository).save(mockAgentRequest);
    }

    @Test
    void reviewAgentRequest_Throws_WhenNotFound() {
        when(agentRequestRepository.findById(1L)).thenReturn(Optional.empty());
        ReviewAgentRequest request = new ReviewAgentRequest(true, "OK");

        assertThrows(ResourceNotFoundException.class, () -> roleService.reviewAgentRequest(1L, request));
    }

    @Test
    void reviewAgentRequest_Throws_WhenDeleted() {
        mockAgentRequest.setIsDeleted(true);
        when(agentRequestRepository.findById(1L)).thenReturn(Optional.of(mockAgentRequest));
        ReviewAgentRequest request = new ReviewAgentRequest(true, "OK");

        assertThrows(ResourceNotFoundException.class, () -> roleService.reviewAgentRequest(1L, request));
    }

    @Test
    void reviewAgentRequest_Throws_WhenNotPending() {
        mockAgentRequest.setStatus(AgentRequestStatus.APPROVED);
        when(agentRequestRepository.findById(1L)).thenReturn(Optional.of(mockAgentRequest));
        ReviewAgentRequest request = new ReviewAgentRequest(true, "OK");

        assertThrows(InvalidResourceException.class, () -> roleService.reviewAgentRequest(1L, request));
    }
}
