package com.rusty.openaiapigps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusty.openaiapigps.domain.dto.GpsDataDto;
import com.rusty.openaiapigps.domain.service.GpsService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GpsController.class)
public class GpsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private GpsService gpsService; // @MockBean -> @Mock

    @InjectMocks
    private GpsController gpsController; // @InjectMocks 추가

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void saveGpsDataTest() throws Exception {
        // given
        GpsDataDto gpsDataDto = new GpsDataDto();
        gpsDataDto.setLatitude(37.566535);
        gpsDataDto.setLongitude(126.977969);

        doNothing().when(gpsService).saveGpsData(gpsDataDto); // 변경된 부분

        // when & then
        mockMvc.perform(post("/api/gps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gpsDataDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("GPS data saved successfully"));
    }
}