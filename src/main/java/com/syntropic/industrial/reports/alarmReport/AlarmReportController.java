package com.syntropic.industrial.reports.alarmReport;


import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/alert-report")
public class AlarmReportController {

    private final WinccAlarmReportService reportService;

    // Exact items from FrmAlarm_Load ComboBox1
    private static final List<String> AREAS = List.of(
            "LINE1 MFG",
            "LINE1 SFMT",
            "LINE1 Sonicator",
            "LINE1 NETWORK",
            "Group"
    );

    public AlarmReportController(WinccAlarmReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("reportRequest", new AlarmReportRequest());
        model.addAttribute("areas", AREAS);
        return "AlarmReport";
    }

    @PostMapping("/generate")
    public Object generateReport(@Valid @ModelAttribute("reportRequest") AlarmReportRequest request,
                                 BindingResult bindingResult,
                                 Model model) throws Exception {

        if (!bindingResult.hasErrors() && !request.isValidDateRange()) {
            bindingResult.reject("invalidRange", "From Date and Time should be less than To Date and Time");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("areas", AREAS);
            return "AlarmReport";
        }

        byte[] pdfBytes = reportService.generateAlarmPdf(request);

        // Matches VB6 timestamp naming: yyyyMMdd_HHmmss
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "Alarm_" + timestamp + ".pdf";
        java.nio.file.Path outputPath = java.nio.file.Paths.get(System.getProperty("user.home"), "Downloads", filename);
        java.nio.file.Files.write(outputPath, pdfBytes);
        // 2. Open automatically in OS default PDF viewer (macOS Preview / Windows Adobe Reader)
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().open(outputPath.toFile());
        }

        return ResponseEntity.ok()
                // "inline" opens the browser's built-in PDF viewer with print button
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
