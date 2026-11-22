package com.syntropic.industrial.reports.alarmReport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AlarmReportRequest {

    @NotNull(message = "From Date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate = LocalDate.now();

    @NotNull(message = "From Time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime fromTime = LocalTime.now();

    @NotNull(message = "To Date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate = LocalDate.now();

    @NotNull(message = "To Time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime toTime = LocalTime.now();

    @NotBlank(message = "Please Select Area")
    private String area;

    public LocalDateTime getFromDateTime() {
        return LocalDateTime.of(fromDate, fromTime.withSecond(0));
    }

    public LocalDateTime getToDateTime() {
        return LocalDateTime.of(toDate, toTime.withSecond(0));
    }

    public boolean isValidDateRange() {
        return fromDate != null && fromTime != null && toDate != null && toTime != null
                && getFromDateTime().isBefore(getToDateTime());
    }

    // Replicates VB6 Group mapping logic
    public String resolveGroup() {
        if ("LINE1 MFG".equals(area) || "LINE1 SFMT".equals(area)
                || "LINE1 Sonicator".equals(area) || "LINE1 NETWORK".equals(area)) {
            return "LINE1";
        }
        return "Group";
    }

    // Getters & Setters
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalTime getFromTime() { return fromTime; }
    public void setFromTime(LocalTime fromTime) { this.fromTime = fromTime; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public LocalTime getToTime() { return toTime; }
    public void setToTime(LocalTime toTime) { this.toTime = toTime; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
}
