package com.ticketbus.mobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_validations")
public class PendingValidation {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String qrData;
    public String deviceId;
    public String location;
    public Double latitude;
    public Double longitude;
    public long validationTime;
    public String result;
    public boolean synced;

    public PendingValidation(String qrData, String deviceId, String location,
                             Double latitude, Double longitude, String result) {
        this.qrData = qrData;
        this.deviceId = deviceId;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.validationTime = System.currentTimeMillis();
        this.result = result;
        this.synced = false;
    }
}
