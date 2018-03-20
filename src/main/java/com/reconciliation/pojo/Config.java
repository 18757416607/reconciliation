package com.reconciliation.pojo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2018/3/13.
 * 配置类
 */
@Component
@ConfigurationProperties(prefix = "config")
public class Config {

    private String ftp_filePath;
    private String commercialCode;
    private String account_filePath;
    private String account_filePath_prefix;
    private String account_filePath_suffix;
    private String zs_filePath;
    private String zs_filePath_prefix;
    private String zs_filePath_suffix;
    private String signCert;
    private String drive_path;
    private String ys_path;

    public String getFtp_filePath() {
        return ftp_filePath;
    }

    public void setFtp_filePath(String ftp_filePath) {
        this.ftp_filePath = ftp_filePath;
    }

    public String getCommercialCode() {
        return commercialCode;
    }

    public void setCommercialCode(String commercialCode) {
        this.commercialCode = commercialCode;
    }

    public String getAccount_filePath() {
        return account_filePath;
    }

    public void setAccount_filePath(String account_filePath) {
        this.account_filePath = account_filePath;
    }

    public String getAccount_filePath_prefix() {
        return account_filePath_prefix;
    }

    public void setAccount_filePath_prefix(String account_filePath_prefix) {
        this.account_filePath_prefix = account_filePath_prefix;
    }

    public String getAccount_filePath_suffix() {
        return account_filePath_suffix;
    }

    public void setAccount_filePath_suffix(String account_filePath_suffix) {
        this.account_filePath_suffix = account_filePath_suffix;
    }

    public String getZs_filePath() {
        return zs_filePath;
    }

    public void setZs_filePath(String zs_filePath) {
        this.zs_filePath = zs_filePath;
    }

    public String getZs_filePath_prefix() {
        return zs_filePath_prefix;
    }

    public void setZs_filePath_prefix(String zs_filePath_prefix) {
        this.zs_filePath_prefix = zs_filePath_prefix;
    }

    public String getZs_filePath_suffix() {
        return zs_filePath_suffix;
    }

    public void setZs_filePath_suffix(String zs_filePath_suffix) {
        this.zs_filePath_suffix = zs_filePath_suffix;
    }

    public String getSignCert() {
        return signCert;
    }

    public void setSignCert(String signCert) {
        this.signCert = signCert;
    }

    public String getDrive_path() {
        return drive_path;
    }

    public void setDrive_path(String drive_path) {
        this.drive_path = drive_path;
    }

    public String getYs_path() {
        return ys_path;
    }

    public void setYs_path(String ys_path) {
        this.ys_path = ys_path;
    }

    @Override
    public String toString() {
        return "Config{" +
                "ftp_filePath='" + ftp_filePath + '\'' +
                ", commercialCode='" + commercialCode + '\'' +
                ", account_filePath='" + account_filePath + '\'' +
                ", account_filePath_prefix='" + account_filePath_prefix + '\'' +
                ", account_filePath_suffix='" + account_filePath_suffix + '\'' +
                ", zs_filePath='" + zs_filePath + '\'' +
                ", zs_filePath_prefix='" + zs_filePath_prefix + '\'' +
                ", zs_filePath_suffix='" + zs_filePath_suffix + '\'' +
                ", signCert='" + signCert + '\'' +
                ", drive_path='" + drive_path + '\'' +
                ", ys_path='" + ys_path + '\'' +
                '}';
    }
}
