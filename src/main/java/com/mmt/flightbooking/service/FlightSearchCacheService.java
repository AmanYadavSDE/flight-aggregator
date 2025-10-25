package com.mmt.flightbooking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.flightbooking.dto.FlightSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Cache search results temporarily so we can retrieve flight details during booking
 * This is how aggregators work - they don't store flight data permanently
 */
@Service
public class FlightSearchCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String CACHE_PREFIX = "flight:search:result:";
    private static final long CACHE_TTL_MINUTES = 30; // Search results valid for 30 minutes
    
    /**
     * Cache a flight search result by its ID
     */
    public void cacheFlightResult(String flightId, FlightSearchResult flightResult) {
        String key = CACHE_PREFIX + flightId;
        redisTemplate.opsForValue().set(key, flightResult, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
    }
    
    /**
     * Retrieve cached flight details by ID
     * This allows booking to get flight details from search results
     */
    public FlightSearchResult getFlightFromCache(String flightId) {
        String key = CACHE_PREFIX + flightId;
        Object cached = redisTemplate.opsForValue().get(key);
        
        if (cached == null) {
            return null;
        }
        
        // Convert from LinkedHashMap to FlightSearchResult
        try {
            return objectMapper.convertValue(cached, FlightSearchResult.class);
        } catch (Exception e) {
            // If conversion fails, try direct cast
            if (cached instanceof FlightSearchResult) {
                return (FlightSearchResult) cached;
            }
            return null;
        }
    }
    
    /**
     * Clear cached flight result
     */
    public void clearFlightCache(String flightId) {
        String key = CACHE_PREFIX + flightId;
        redisTemplate.delete(key);
    }
}

