package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.model.Data;
import hu.benkototh.cardgame.backend.rest.repository.IDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeartBeatRestServiceTest {

    @Mock
    private IDataRepository dataRepository;

    @InjectMocks
    private HeartBeatRestService heartBeatRestService;

    private List<Data> testDataList;

    @BeforeEach
    void setUp() {
        testDataList = new ArrayList<>();
        
        Data data1 = new Data();
        data1.setId(1L);
        data1.setMessage("Test message 1");
        
        Data data2 = new Data();
        data2.setId(2L);
        data2.setMessage("Test message 2");
        
        testDataList.add(data1);
        testDataList.add(data2);
    }

    @Test
    void testAll() {
        when(dataRepository.findAll()).thenReturn(testDataList);
        
        List<Data> result = heartBeatRestService.all();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Test message 1", result.get(0).getMessage());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Test message 2", result.get(1).getMessage());
        
        verify(dataRepository, times(1)).findAll();
    }

    @Test
    void testAllEmptyList() {
        when(dataRepository.findAll()).thenReturn(new ArrayList<>());
        
        List<Data> result = heartBeatRestService.all();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(dataRepository, times(1)).findAll();
    }
}