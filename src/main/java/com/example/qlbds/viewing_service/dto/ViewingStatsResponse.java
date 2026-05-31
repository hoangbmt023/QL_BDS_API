package com.example.qlbds.viewing_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewingStatsResponse {
    private long total;
    private long pending;
    private long confirmed;
    private long completed;
    private long cancelled;
}
