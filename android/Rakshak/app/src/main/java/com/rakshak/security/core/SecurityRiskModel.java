package com.rakshak.security.core;

public class SecurityRiskModel {

    public int callRisk;
    public int fileRisk;
    public int linkRisk;
    public int permissionRisk;
    public int healthRisk;

    public int getTotalRiskScore() {
        return callRisk + fileRisk + linkRisk + permissionRisk + healthRisk;
    }
}
