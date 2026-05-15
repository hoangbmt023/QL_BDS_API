package com.example.qlbds.user_service.service;

import java.util.List;

import com.example.qlbds.user_service.dto.*;

/**
 * RoleService xử lý các nghiệp vụ nâng cấp role của người dùng.
 *
 * Flow Owner: User đăng tin → tự động trở thành Owner (không cần duyệt).
 * Flow Agent: User gửi yêu cầu → Admin duyệt → cấp role AGENT + tạo Agent record.
 */
public interface RoleService {

    // User tự đăng ký thành Owner. Nếu đã là Owner → trả về thông tin hiện tại.
    OwnerResponse becomeOwner(BecomeOwnerRequest request);

    // User nộp yêu cầu làm Agent. Chỉ cho phép 1 request PENDING tại 1 thời điểm.
    AgentRequestResponse submitAgentRequest(BecomeAgentRequest request);

    // Lấy danh sách tất cả request đang chờ duyệt (ADMIN).
    List<AgentRequestResponse> getPendingAgentRequests();

    // Admin duyệt hoặc từ chối request (ADMIN).
    AgentRequestResponse reviewAgentRequest(Long requestId, ReviewAgentRequest review);
}
