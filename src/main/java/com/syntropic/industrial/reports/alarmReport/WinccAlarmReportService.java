package com.syntropic.industrial.reports.alarmReport;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class WinccAlarmReportService {

    private  JdbcTemplate jdbcTemplate;
    private  TemplateEngine templateEngine;

    @Value("${appmode:sqlserver}")
    private String appMode;

    public WinccAlarmReportService(JdbcTemplate jdbcTemplate, TemplateEngine templateEngine) {
        this.jdbcTemplate = jdbcTemplate;
        this.templateEngine = templateEngine;
    }

    private boolean isH2Mode() {
        return "h2".equalsIgnoreCase(appMode != null ? appMode.trim() : "");
    }

    private JdbcTemplate resolveJdbcTemplate(AlarmReportRequest request) {
        if (isH2Mode()) {
            return this.jdbcTemplate;
        }
        if (request.getConnectionString() != null && !request.getConnectionString().isBlank()) {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setUrl(request.getConnectionString().trim());
            if (request.getDbUsername() != null && !request.getDbUsername().isBlank()) {
                ds.setUsername(request.getDbUsername().trim());
            }
            if (request.getDbPassword() != null) {
                ds.setPassword(request.getDbPassword());
            }
            return new JdbcTemplate(ds);
        }
        return this.jdbcTemplate;
    }

    public byte[] generateAlarmPdf(AlarmReportRequest request) throws Exception {
        // Use the dynamic JdbcTemplate for this specific report execution!
        JdbcTemplate activeJdbc = resolveJdbcTemplate(request);

        DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");
        String frmDate = request.getFromDateTime().format(sqlFormatter);
        String toDate = request.getToDateTime().format(sqlFormatter);
        String group = request.resolveGroup();

        // 1. Fetch Username based on appmode
        String username = fetchActiveUsername(activeJdbc);

        this.jdbcTemplate = activeJdbc;
        // 2. Get database names based on appmode
        String dbListSql = isH2Mode()
                ? "SELECT name FROM sys.sysdatabases ORDER BY name"
                : "SELECT name FROM master.sys.sysdatabases ORDER BY name";

        List<String> allDatabases = jdbcTemplate.queryForList(dbListSql, String.class);
        List<AlarmRecordDto> allAlarms = new ArrayList<>();

        // 3. Loop through _ALG_ archive databases
        for (String dbName : allDatabases) {
            if (dbName != null && dbName.length() >= 24 && "_ALG_".equals(dbName.substring(19, 24))) {
                if (!dbName.matches("^[a-zA-Z0-9_\\-]+$")) {
                    continue;
                }

                String alarmSql = buildAlarmQuery(dbName);

                List<AlarmRecordDto> dbRecords = jdbcTemplate.query(
                        alarmSql,
                        (rs, rowNum) -> new AlarmRecordDto(
                                rs.getString("alarm_dt"),
                                rs.getString("alarm_msg"),
                                rs.getString("ack_status"),
                                rs.getString("user_name")
                        ),
                        frmDate, toDate, request.getArea(), group
                );

                allAlarms.addAll(dbRecords);
            }
        }

        // 4. Bind data to Thymeleaf template
        Context context = new Context();
        context.setVariable("fromDate", frmDate);
        context.setVariable("toDate", toDate);
        context.setVariable("area", request.getArea());
        context.setVariable("username", username);
        context.setVariable("alarms", allAlarms);

        // 5. Render HTML to PDF
        String html = templateEngine.process("alarm-report-pdf", context);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        }
    }

    private String buildAlarmQuery(String dbName) {
        if (isH2Mode()) {
            return """
                SELECT 
                    FORMATDATETIME(DATEADD('MINUTE', 330, DateTime), 'dd/MM/yyyy HH:mm:ss') AS alarm_dt,
                    REPLACE(REPLACE(REPLACE(text3, '@100%s@', COALESCE(Computername, '')), '@102%s@', COALESCE(Username, '')), '@103%s@', COALESCE(PText6, '')) AS alarm_msg,
                    TxtAck AS ack_status,
                    Username AS user_name
                FROM dbo.AlgViewENU
                WHERE MsgNr <= 15100
                  AND DATEADD('MINUTE', 330, DateTime) BETWEEN ? AND ?
                  AND TxtAck NOT LIKE '%%Acknowledgment%%'
                  AND (Text1 = ? OR Text1 = ?)
                ORDER BY DateTime ASC
                """;
        }

        return """
            SELECT 
                CONVERT(varchar(10), DateAdd(MINUTE, 330, [AlgViewENU].DateTime), 103) + ' ' +
                CONVERT(varchar(8), DateAdd(MINUTE, 330, [AlgViewENU].DateTime), 14) AS alarm_dt,
                REPLACE(REPLACE(REPLACE(text3, '@100%s@', ISNULL(Computername, '')), '@102%s@', ISNULL(Username, '')), '@103%s@', ISNULL(PText6, '')) AS alarm_msg,
                [TxtAck] AS ack_status,
                [Username] AS user_name
            FROM [%s].[dbo].[AlgViewENU]
            WHERE MsgNr <= 15100
              AND DateAdd(MINUTE, 330, [AlgViewENU].DateTime) BETWEEN ? AND ?
              AND TxtAck NOT LIKE '%%Acknowledgment%%'
              AND (Text1 = ? OR Text1 = ?)
            ORDER BY [AlgViewENU].DateTime ASC
            """.formatted(dbName);
    }

    private String fetchActiveUsername(JdbcTemplate activeJdbcTemplate) {
        String userSql = isH2Mode()
                ? "SELECT User_NM FROM dbo.User_NM LIMIT 1"
                : "SELECT TOP 1 User_NM FROM [Database].[dbo].[User_NM]";
        try {
            List<String> users = activeJdbcTemplate.queryForList(userSql, String.class);
            return users.isEmpty() ? "Operator" : users.get(0);
        } catch (Exception e) {
            return "Operator";
        }
    }
}
