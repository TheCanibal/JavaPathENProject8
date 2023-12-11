package com.openclassrooms.tourguide.dto;

import java.util.Map;

public class ClosestFiveAttractions {

    Map<String, Object> attractionNameLatLong;
    Map<String, Double> UserLocation;
    Double distanceBetween;
    Integer rewardPoints;

    public Map<String, Object> getAttractionNameLatLong() {
        return attractionNameLatLong;
    }

    public void setAttractionNameLatLong(Map<String, Object> attractionNameLatLong) {
        this.attractionNameLatLong = attractionNameLatLong;
    }

    public Map<String, Double> getUserLocation() {
        return UserLocation;
    }

    public void setUserLocation(Map<String, Double> userLocation) {
        UserLocation = userLocation;
    }

    public Double getDistanceBetween() {
        return distanceBetween;
    }

    public void setDistanceBetween(Double distanceBetween) {
        this.distanceBetween = distanceBetween;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

}
