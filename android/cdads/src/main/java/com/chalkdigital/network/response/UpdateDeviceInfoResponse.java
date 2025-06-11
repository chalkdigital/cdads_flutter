package com.chalkdigital.network.response;

public class UpdateDeviceInfoResponse extends SSBBaseResponse{
    private String deviceId;
    private String uuid;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
