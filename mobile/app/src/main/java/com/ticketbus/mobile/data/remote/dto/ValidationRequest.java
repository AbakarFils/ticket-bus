package com.ticketbus.mobile.data.remote.dto;

public class ValidationRequest {
    public String qrCodeData;
    public String deviceId;
    public String location;
    public Double latitude;
    public Double longitude;
    public String validationTime;

    public ValidationRequest(String qrCodeData, String deviceId, String location,
                             Double latitude, Double longitude, String validationTime) {
        this.qrCodeData = qrCodeData;
        this.deviceId = deviceId;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.validationTime = validationTime;
    }
}
