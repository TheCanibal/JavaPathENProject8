package com.openclassrooms.tourguide.dto;

import java.util.Map;

/**
 * DTO to format and show the five closest attractions informations
 */

public class ClosestFiveAttractions {

    // attraction name and latitude/longitude to display
    Map<String, Object> attractionNameLatLong;
    // User's latitude/longitude to display
    Map<String, Double> UserLocation;
    // distance between attraction and user
    Double distanceBetween;
    // reward points ernable when you visit this attraction
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
