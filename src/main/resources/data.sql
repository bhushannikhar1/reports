INSERT INTO sys.sysdatabases (name) VALUES ('CC_WINCC_26_11_2026_ALG_01');

INSERT INTO dbo.User_NM (User_NM) VALUES ('Test_Operator_Pune');

-- Insert sample alarms (timestamps in UTC; service adds +330 mins for IST)
INSERT INTO dbo.AlgViewENU (MsgNr, DateTime, text3, Computername, Username, PText6, TxtAck, Text1)
VALUES
    (101, DATEADD('HOUR', -6, CURRENT_TIMESTAMP), 'High Temp Alarm on @100%s@ by @102%s@ (@103%s@)', 'WINCC-SRV01', 'Operator1', 'Reactor-A', 'Came In', 'LINE1 MFG'),
    (102, DATEADD('HOUR', -5, CURRENT_TIMESTAMP), 'Conveyor Motor Trip on @100%s@', 'WINCC-SRV01', 'Operator1', 'Motor-02', 'Came In / Went Out', 'LINE1'),
    (103, DATEADD('HOUR', -4, CURRENT_TIMESTAMP), 'Server UPS Battery Low on @100%s@', 'INSKMSRPRD-08', 'Admin', 'UPS-1', 'Came In', 'SERVER');
