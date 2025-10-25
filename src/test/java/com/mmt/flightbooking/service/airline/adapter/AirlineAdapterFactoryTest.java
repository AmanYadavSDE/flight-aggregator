package com.mmt.flightbooking.service.airline.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AirlineAdapterFactoryTest {

    @Mock
    private IndiGoAdapter indigoAdapter;

    @Mock
    private AirIndiaAdapter airIndiaAdapter;

    @Mock
    private SpiceJetAdapter spiceJetAdapter;

    @Mock
    private VistaraAdapter vistaraAdapter;

    private AirlineAdapterFactory airlineAdapterFactory;

    @BeforeEach
    void setUp() {
        lenient().when(indigoAdapter.getAirlineCode()).thenReturn("6E");
        lenient().when(indigoAdapter.getAirlineName()).thenReturn("IndiGo");
        lenient().when(indigoAdapter.isAvailable()).thenReturn(true);

        lenient().when(airIndiaAdapter.getAirlineCode()).thenReturn("AI");
        lenient().when(airIndiaAdapter.getAirlineName()).thenReturn("Air India");
        lenient().when(airIndiaAdapter.isAvailable()).thenReturn(true);

        lenient().when(spiceJetAdapter.getAirlineCode()).thenReturn("SG");
        lenient().when(spiceJetAdapter.getAirlineName()).thenReturn("SpiceJet");
        lenient().when(spiceJetAdapter.isAvailable()).thenReturn(true);

        lenient().when(vistaraAdapter.getAirlineCode()).thenReturn("UK");
        lenient().when(vistaraAdapter.getAirlineName()).thenReturn("Vistara");
        lenient().when(vistaraAdapter.isAvailable()).thenReturn(true);

        airlineAdapterFactory = new AirlineAdapterFactory(
            indigoAdapter,
            airIndiaAdapter,
            spiceJetAdapter,
            vistaraAdapter
        );
    }

    @Test
    void testGetAdapter_IndiGo() {
        // Act
        AirlineAdapter adapter = airlineAdapterFactory.getAdapter("6E");

        // Assert
        assertNotNull(adapter);
        assertEquals(indigoAdapter, adapter);
        assertEquals("IndiGo", adapter.getAirlineName());
    }

    @Test
    void testGetAdapter_AirIndia() {
        // Act
        AirlineAdapter adapter = airlineAdapterFactory.getAdapter("AI");

        // Assert
        assertNotNull(adapter);
        assertEquals(airIndiaAdapter, adapter);
        assertEquals("Air India", adapter.getAirlineName());
    }

    @Test
    void testGetAdapter_SpiceJet() {
        // Act
        AirlineAdapter adapter = airlineAdapterFactory.getAdapter("SG");

        // Assert
        assertNotNull(adapter);
        assertEquals(spiceJetAdapter, adapter);
        assertEquals("SpiceJet", adapter.getAirlineName());
    }

    @Test
    void testGetAdapter_Vistara() {
        // Act
        AirlineAdapter adapter = airlineAdapterFactory.getAdapter("UK");

        // Assert
        assertNotNull(adapter);
        assertEquals(vistaraAdapter, adapter);
        assertEquals("Vistara", adapter.getAirlineName());
    }

    @Test
    void testGetAdapter_UnsupportedAirline() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            airlineAdapterFactory.getAdapter("XX");
        });

        assertTrue(exception.getMessage().contains("Unsupported airline code"));
        assertTrue(exception.getMessage().contains("XX"));
    }

    @Test
    void testGetAdapter_NullAirlineCode() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            airlineAdapterFactory.getAdapter(null);
        });

        assertTrue(exception.getMessage().contains("Unsupported airline code"));
    }

    @Test
    void testGetAllAdapters() {
        // Act
        List<AirlineAdapter> adapters = airlineAdapterFactory.getAllAdapters();

        // Assert
        assertNotNull(adapters);
        assertEquals(4, adapters.size());
        assertTrue(adapters.contains(indigoAdapter));
        assertTrue(adapters.contains(airIndiaAdapter));
        assertTrue(adapters.contains(spiceJetAdapter));
        assertTrue(adapters.contains(vistaraAdapter));
    }

    @Test
    void testGetAvailableAdapters_AllAvailable() {
        // Act
        List<AirlineAdapter> adapters = airlineAdapterFactory.getAvailableAdapters();

        // Assert
        assertNotNull(adapters);
        assertEquals(4, adapters.size());
    }

    @Test
    void testGetAvailableAdapters_SomeUnavailable() {
        // Arrange
        when(spiceJetAdapter.isAvailable()).thenReturn(false);
        when(vistaraAdapter.isAvailable()).thenReturn(false);

        // Act
        List<AirlineAdapter> adapters = airlineAdapterFactory.getAvailableAdapters();

        // Assert
        assertNotNull(adapters);
        assertEquals(2, adapters.size());
        assertTrue(adapters.contains(indigoAdapter));
        assertTrue(adapters.contains(airIndiaAdapter));
        assertFalse(adapters.contains(spiceJetAdapter));
        assertFalse(adapters.contains(vistaraAdapter));
    }

    @Test
    void testGetAvailableAdapters_NoneAvailable() {
        // Arrange
        when(indigoAdapter.isAvailable()).thenReturn(false);
        when(airIndiaAdapter.isAvailable()).thenReturn(false);
        when(spiceJetAdapter.isAvailable()).thenReturn(false);
        when(vistaraAdapter.isAvailable()).thenReturn(false);

        // Act
        List<AirlineAdapter> adapters = airlineAdapterFactory.getAvailableAdapters();

        // Assert
        assertNotNull(adapters);
        assertEquals(0, adapters.size());
    }

    @Test
    void testIsSupported_SupportedAirlines() {
        // Assert
        assertTrue(airlineAdapterFactory.isSupported("6E"));
        assertTrue(airlineAdapterFactory.isSupported("AI"));
        assertTrue(airlineAdapterFactory.isSupported("SG"));
        assertTrue(airlineAdapterFactory.isSupported("UK"));
    }

    @Test
    void testIsSupported_UnsupportedAirline() {
        // Assert
        assertFalse(airlineAdapterFactory.isSupported("XX"));
        assertFalse(airlineAdapterFactory.isSupported("YY"));
        assertFalse(airlineAdapterFactory.isSupported(null));
    }

    @Test
    void testGetAirlineCount() {
        // Act
        int count = airlineAdapterFactory.getAirlineCount();

        // Assert
        assertEquals(4, count);
    }

    @Test
    void testRegisterAdapter_NewAirline() {
        // Arrange
        AirlineAdapter newAdapter = mock(AirlineAdapter.class);
        lenient().when(newAdapter.getAirlineCode()).thenReturn("9W");
        lenient().when(newAdapter.getAirlineName()).thenReturn("Jet Airways");
        lenient().when(newAdapter.isAvailable()).thenReturn(true);

        // Act
        airlineAdapterFactory.registerAdapter(newAdapter);

        // Assert
        assertTrue(airlineAdapterFactory.isSupported("9W"));
        assertEquals(5, airlineAdapterFactory.getAirlineCount());
        assertEquals(newAdapter, airlineAdapterFactory.getAdapter("9W"));
    }

    @Test
    void testRegisterAdapter_OverrideExisting() {
        // Arrange
        AirlineAdapter newIndigoAdapter = mock(AirlineAdapter.class);
        when(newIndigoAdapter.getAirlineCode()).thenReturn("6E");
        when(newIndigoAdapter.getAirlineName()).thenReturn("IndiGo V2");

        // Act
        airlineAdapterFactory.registerAdapter(newIndigoAdapter);

        // Assert
        AirlineAdapter retrievedAdapter = airlineAdapterFactory.getAdapter("6E");
        assertEquals(newIndigoAdapter, retrievedAdapter);
        assertNotEquals(indigoAdapter, retrievedAdapter);
    }

    @Test
    void testGetAllAdapters_ReturnsNewList() {
        // Act
        List<AirlineAdapter> adapters1 = airlineAdapterFactory.getAllAdapters();
        List<AirlineAdapter> adapters2 = airlineAdapterFactory.getAllAdapters();

        // Assert - Should return different list instances
        assertNotSame(adapters1, adapters2);
        assertEquals(adapters1.size(), adapters2.size());
    }

    @Test
    void testFactoryInitialization() {
        // Assert all airlines are registered during initialization
        assertNotNull(airlineAdapterFactory.getAdapter("6E"));
        assertNotNull(airlineAdapterFactory.getAdapter("AI"));
        assertNotNull(airlineAdapterFactory.getAdapter("SG"));
        assertNotNull(airlineAdapterFactory.getAdapter("UK"));
    }

    @Test
    void testGetAdapter_MultipleCallsSameInstance() {
        // Act
        AirlineAdapter adapter1 = airlineAdapterFactory.getAdapter("6E");
        AirlineAdapter adapter2 = airlineAdapterFactory.getAdapter("6E");

        // Assert
        assertSame(adapter1, adapter2);
    }
}

