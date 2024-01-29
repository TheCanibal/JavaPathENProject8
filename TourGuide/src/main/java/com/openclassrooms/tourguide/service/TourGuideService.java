package com.openclassrooms.tourguide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.dto.ClosestFiveAttractions;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;
    private ExecutorService executor = Executors.newFixedThreadPool(50);

    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        Locale.setDefault(Locale.US);

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
                : trackUserLocation(user);
        return visitedLocation;
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    /**
     * execute trackUserLocation on multiple thread
     * 
     * @param user
     * @return
     */
    public Future<VisitedLocation> trackUserLocationAsync(User user) {
        return executor.submit(() -> trackUserLocation(user));
    }

    /**
     * get user's location to add visited location and calculate his rewards
     * 
     * @param user
     * @return visited location
     */
    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    /**
     * get the five closest attractions from user's position
     * 
     * @param visitedLocation
     * @return list of the five closest attraction
     */
    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
        // List of five closest attractions
        List<Attraction> fiveClosestAttractions = new ArrayList<>();
        // all attractions around user, sort by ascending order
        Map<Double, Attraction> allDistancesAttractions = new TreeMap<>();
        // Five closest attractions and their distance
        Map<Double, Attraction> fiveClosestDistancesAttractions = new HashMap<>();
        int attractionNumber = 0;
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        // set buffer to get all attractions whatever is the distance
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        // for all attractions, put all attractions's distance and all attractions in a
        // map
        for (Attraction attraction : gpsUtil.getAttractions()) {
            allDistancesAttractions.put(rewardsService.getDistance(attraction, visitedLocation.location), attraction);
        }
        // for all attractions in all attractions map, put five closest attractions in
        // another map
        for (Map.Entry<Double, Attraction> mapEntry : allDistancesAttractions.entrySet()) {
            if (attractionNumber < 5) {
                fiveClosestDistancesAttractions.put(mapEntry.getKey(), mapEntry.getValue());
                attractionNumber++;
            }
        }
        // for the five closest attractions, separate the distances and the attractions
        // in two different list to be able to access informations
        for (Map.Entry<Double, Attraction> mapEntry : fiveClosestDistancesAttractions.entrySet()) {
            fiveClosestAttractions.add(mapEntry.getValue());
        }
        return fiveClosestAttractions;
    }

    /**
     * get the distances between the five closest attraction and user's position
     * 
     * @param visitedLocation
     * @return list of the distances of the five closest attraction
     */
    public List<Double> getNearByAttractionsDistances(VisitedLocation visitedLocation) {
        // List of five closest distances
        List<Double> fiveClosestDistances = new ArrayList<>();
        // all attractions around user, sort by ascending order
        Map<Double, Attraction> allDistancesAttractions = new TreeMap<>();
        // Five closest attractions and their distance
        Map<Double, Attraction> fiveClosestDistancesAttractions = new HashMap<>();
        int attractionNumber = 0;
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        // set buffer to get all attractions whatever is the distance
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        // for all attractions, put all attractions's distance and all attractions in a
        // map
        for (Attraction attraction : gpsUtil.getAttractions()) {
            allDistancesAttractions.put(rewardsService.getDistance(attraction, visitedLocation.location), attraction);
        }
        // for all attractions in all attractions map, put five closest attractions in
        // another map
        for (Map.Entry<Double, Attraction> mapEntry : allDistancesAttractions.entrySet()) {
            if (attractionNumber < 5) {
                fiveClosestDistancesAttractions.put(mapEntry.getKey(), mapEntry.getValue());
                attractionNumber++;
            }
        }
        // for the five closest attractions, separate the distances and the attractions
        // in two different list to be able to access informations
        for (Map.Entry<Double, Attraction> mapEntry : fiveClosestDistancesAttractions.entrySet()) {
            fiveClosestDistances.add(mapEntry.getKey());
        }
        return fiveClosestDistances;
    }

    /**
     * display the five closest attraction with some informations
     * 
     * @param visitedLocation
     * @return informations of the five closest attractions in json format
     */
    public List<ClosestFiveAttractions> getFiveClosestAttractions(VisitedLocation visitedLocation) {
        // List of five closest attractions
        List<Attraction> fiveClosestAttractions = getNearByAttractions(visitedLocation);
        // List of five closest distances
        List<Double> fiveClosestDistances = getNearByAttractionsDistances(visitedLocation);
        // List of five closest attractions to display
        List<ClosestFiveAttractions> closestFiveAttractions = new ArrayList<>();
        int distanceNumber = 0;
        int rewardPoints = 0;
        RewardCentral rewardCentral = new RewardCentral();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        // set buffer to get all attractions whatever is the distance
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        // for each five attractions in the five closest attractions list
        for (Attraction attraction : fiveClosestAttractions) {
            // create new ClosestFiveAttractions object
            ClosestFiveAttractions fiveAttractions = new ClosestFiveAttractions();
            // Create new attractions map String Object to show name, latitude and longitude
            Map<String, Object> attractions = new HashMap<>();
            // Create new userLocation map to show user's latitude and longitude
            Map<String, Double> userLocation = new HashMap<>();
            // Put all necessary informations to display
            attractions.put("name", attraction.attractionName);
            attractions.put("latitude", attraction.latitude);
            attractions.put("longitude", attraction.longitude);
            userLocation.put("latitude", visitedLocation.location.latitude);
            userLocation.put("longitude", visitedLocation.location.longitude);
            rewardPoints = rewardCentral.getAttractionRewardPoints(attraction.attractionId, visitedLocation.userId);
            // Then set value of each attribute in ClosestFiveAttraction POJO
            fiveAttractions.setAttractionNameLatLong(attractions);
            fiveAttractions.setDistanceBetween(fiveClosestDistances.get(distanceNumber));
            fiveAttractions.setUserLocation(userLocation);
            fiveAttractions.setRewardPoints(rewardPoints);
            distanceNumber++;
            // Finally add each attraction information
            closestFiveAttractions.add(fiveAttractions);
        }
        return closestFiveAttractions;
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     * 
     * Methods Below: For Internal Testing
     * 
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes
    // internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
