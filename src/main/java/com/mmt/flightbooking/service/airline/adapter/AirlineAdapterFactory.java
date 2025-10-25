package com.mmt.flightbooking.service.airline.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Factory for creating and managing airline adapters
 * Follows Factory Pattern for extensible airline integration
 */
@Component
public class AirlineAdapterFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(AirlineAdapterFactory.class);
    
    private final Map<String, AirlineAdapter> adaptersByCode = new HashMap<>();
    private final List<AirlineAdapter> allAdapters = new ArrayList<>();
    
    @Autowired
    public AirlineAdapterFactory(
            IndiGoAdapter indigoAdapter,
            AirIndiaAdapter airIndiaAdapter,
            SpiceJetAdapter spiceJetAdapter,
            VistaraAdapter vistaraAdapter) {
        
        // Register all airline adapters
        registerAdapter(indigoAdapter);
        registerAdapter(airIndiaAdapter);
        registerAdapter(spiceJetAdapter);
        registerAdapter(vistaraAdapter);
        
        logger.info("Airline adapters registered: {}", adaptersByCode.keySet());
    }
    
    /**
     * Register an airline adapter
     * @param adapter Airline adapter to register
     */
    public void registerAdapter(AirlineAdapter adapter) {
        adaptersByCode.put(adapter.getAirlineCode(), adapter);
        allAdapters.add(adapter);
        logger.info("Registered adapter for airline: {} ({})", 
                   adapter.getAirlineName(), adapter.getAirlineCode());
    }
    
    /**
     * Get adapter for specific airline code
     * @param airlineCode Airline code (e.g., "6E", "AI")
     * @return Airline adapter
     * @throws IllegalArgumentException if airline code is not supported
     */
    public AirlineAdapter getAdapter(String airlineCode) {
        AirlineAdapter adapter = adaptersByCode.get(airlineCode);
        
        if (adapter == null) {
            logger.error("No adapter found for airline code: {}", airlineCode);
            throw new IllegalArgumentException("Unsupported airline code: " + airlineCode);
        }
        
        logger.debug("Retrieved adapter for airline: {} ({})", 
                    adapter.getAirlineName(), airlineCode);
        return adapter;
    }
    
    /**
     * Get all registered airline adapters
     * @return List of all airline adapters
     */
    public List<AirlineAdapter> getAllAdapters() {
        return new ArrayList<>(allAdapters);
    }
    
    /**
     * Get all available (healthy) airline adapters
     * @return List of available airline adapters
     */
    public List<AirlineAdapter> getAvailableAdapters() {
        return allAdapters.stream()
            .filter(AirlineAdapter::isAvailable)
            .toList();
    }
    
    /**
     * Check if airline code is supported
     * @param airlineCode Airline code to check
     * @return true if supported, false otherwise
     */
    public boolean isSupported(String airlineCode) {
        return adaptersByCode.containsKey(airlineCode);
    }
    
    /**
     * Get number of registered airlines
     * @return Count of registered airlines
     */
    public int getAirlineCount() {
        return allAdapters.size();
    }
}

