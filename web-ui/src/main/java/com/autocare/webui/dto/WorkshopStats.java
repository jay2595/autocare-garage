package com.autocare.webui.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkshopStats(
        Long total,
        Long open,
        Map<String, Long> byStatus,
        Boolean customersServiceReachable) {

    public static WorkshopStats empty() {
        return new WorkshopStats(0L, 0L, Map.of(), Boolean.FALSE);
    }

    public long count(String status) {
        if (byStatus == null) {
            return 0;
        }
        return byStatus.getOrDefault(status, 0L);
    }
}
