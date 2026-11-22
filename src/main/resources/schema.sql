CREATE SCHEMA IF NOT EXISTS sys;
CREATE SCHEMA IF NOT EXISTS dbo;

-- Mock master.sys.sysdatabases
CREATE TABLE IF NOT EXISTS sys.sysdatabases (
                                                name VARCHAR(100)
    );

-- Mock User_NM table
CREATE TABLE IF NOT EXISTS dbo.User_NM (
                                           User_NM VARCHAR(50)
    );

-- Mock WinCC Alarm View (AlgViewENU)
CREATE TABLE IF NOT EXISTS dbo.AlgViewENU (
                                              MsgNr INT,
                                              DateTime TIMESTAMP,
                                              text3 VARCHAR(255),
    Computername VARCHAR(50),
    Username VARCHAR(50),
    PText6 VARCHAR(50),
    TxtAck VARCHAR(50),
    Text1 VARCHAR(50)
    );
