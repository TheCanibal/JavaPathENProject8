package com.openclassrooms.tourguide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.tourguide.dto.ClosestFiveAttractions;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tripPricer.Provider;

@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * get user's location lat and long
     * 
     * @param userName
     * @return user's location
     * @throws InterruptedException
     * @throws ExecutionException
     */

    @RequestMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam String userName) throws InterruptedException, ExecutionException {
        return tourGuideService.getUserLocation(getUser(userName));
    }

    /**
     * get the closest five attractions from user's location
     * 
     * @param userName
     * @return DTO with the five closest attraction information : name and location
     *         of attraction, user's location and distance between them
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @RequestMapping("/getNearbyAttractions")
    public List<ClosestFiveAttractions> getNearbyAttractions(@RequestParam String userName)
            throws InterruptedException, ExecutionException {
        List<ClosestFiveAttractions> closestFiveAttractions = new ArrayList<>();
        User user = getUser(userName);
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        closestFiveAttractions = tourGuideService.getFiveClosestAttractions(visitedLocation);
        return closestFiveAttractions;

    }

    /**
     * get user's rewards
     * 
     * @param userName
     * @return user's rewards
     */
    @RequestMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        return tourGuideService.getUserRewards(getUser(userName));
    }

    /**
     * get all deals and show them to user
     * 
     * @param userName
     * @return all deals
     */
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        return tourGuideService.getTripDeals(getUser(userName));
    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }

}