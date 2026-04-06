package com.ticketbus.mobile.data.remote.dto;

public class PendingValidationDto {
    public String ticketNumber;
    public String deviceId;
    public String location;
    public Double latitude;
    public Double longitude;
    public String validationTime;
    public String result;

    public PendingValidationDto(String ticketNumber, String deviceId, String location,
                                Double latitude, Double longitude,
                                String validationTime, String result) {
        this.ticketNumber = ticketNumber;
        this.deviceId = deviceId;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.validationTime = validationTime;
        this.result = result;
    }
}
