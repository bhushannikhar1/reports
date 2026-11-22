package com.syntropic.industrial.reports.alarmReport;

public record AlarmRecordDto(
    String formattedDateTime,
    String alarmText,
    String ackStatus,
    String username
) {}
