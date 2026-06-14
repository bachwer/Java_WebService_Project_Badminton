package org.example.project_badminton.Controller;

import org.aspectj.lang.annotation.Before;
import org.example.project_badminton.controller.CourtController;
import org.example.project_badminton.dto.request.CourtRequest;
import org.example.project_badminton.dto.request.CourtUpdateRequest;
import org.example.project_badminton.dto.response.ApiResponse;
import org.example.project_badminton.dto.response.CourtDTO;
import org.example.project_badminton.instance.CourtStatus;
import org.example.project_badminton.service.ManagerCourtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CourtControllerTest {

    @Mock
    private ManagerCourtService managerCourtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CourtController courtController;

    private CourtDTO dto;

    @BeforeEach
    void setup(){
        dto = new CourtDTO();
    }

    @Test
    void getAllCourtForManager() {
        when(authentication.getName()).thenReturn("manager1");
        Page<CourtDTO> page = new PageImpl<>(List.of(dto));


        when(managerCourtService.getAllCourtForManager(
                anyString(),
                anyString(),
                any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<ApiResponse<List<CourtDTO>>> response =

                courtController.getAllCourtForManager(

                        authentication,

                        "",

                        0,

                        5,

                        "id",

                        "desc"

                );

        assertEquals(200, response.getStatusCode().value());

        assertTrue(response.getBody().isSuccess());

        assertEquals(1, response.getBody().getData().size());

        verify(authentication).getName();

        verify(managerCourtService)
                .getAllCourtForManager(
                        anyString(),
                        eq("manager1"),
                        any(Pageable.class)
                );


    }

    @Test
    void getAllCourt() {

        Page<CourtDTO> page = new PageImpl<>(List.of(dto));

        when(managerCourtService.getAllCourt(anyString(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse<List<CourtDTO>>> response = courtController.getAllCourt(
                "",
                0, 5, "id", "desc"
        );

        assertEquals(200, response.getStatusCode().value());

        assertTrue(response.getBody().isSuccess());

        assertEquals(1, response.getBody().getData().size());

        verify(managerCourtService)

                .getAllCourt(anyString(), any(Pageable.class));


    }

    @Test
    void createCourt() {
        CourtRequest request = new CourtRequest();



        when(managerCourtService.createCourt(any(CourtRequest.class)))

                .thenReturn(dto);

        ResponseEntity<ApiResponse<CourtDTO>> response =

                courtController.createCourt(request);

        assertEquals(200, response.getStatusCode().value());

        assertTrue(response.getBody().isSuccess());

        assertEquals(dto, response.getBody().getData());

        verify(managerCourtService).createCourt(request);

    }

    @Test
    void updateCourt() {
        Long courtId = 1L;

        CourtUpdateRequest request = new CourtUpdateRequest();

        when(authentication.getName()).thenReturn("manager1");
        when(managerCourtService.updateCourt(
                eq(courtId),
                any(CourtUpdateRequest.class),
                eq("manager1")
        )).thenReturn(dto);


        ResponseEntity<ApiResponse<CourtDTO>> response =

                courtController.updateCourt(

                        authentication,

                        courtId,

                        request

                );

        assertEquals(200, response.getStatusCode().value());

        assertEquals(dto, response.getBody().getData());

        verify(managerCourtService)

                .updateCourt(courtId, request, "manager1");


    }

    @Test

    void closeCourt() {

        Long courtId = 1L;

        doNothing().when(managerCourtService)

                .closeCourt(courtId, CourtStatus.CLOSED);

        ResponseEntity<ApiResponse<Void>> response =

                courtController.closeCourt(courtId);

        assertEquals(200, response.getStatusCode().value());

        assertTrue(response.getBody().isSuccess());

        verify(managerCourtService)

                .closeCourt(courtId, CourtStatus.CLOSED);

    }


    @Test

    void deleteCourtImage() {

        Long courtId = 1L;

        Long imageId = 10L;

        when(authentication.getName()).thenReturn("manager1");

        when(managerCourtService.deleteImageFromCourt(

                courtId,

                imageId,

                "manager1"))

                .thenReturn(dto);

        ResponseEntity<ApiResponse<CourtDTO>> response =

                courtController.deleteCourtImage(

                        authentication,

                        courtId,

                        imageId

                );

        assertEquals(200, response.getStatusCode().value());

        assertEquals(dto, response.getBody().getData());

        verify(managerCourtService)

                .deleteImageFromCourt(

                        courtId,

                        imageId,

                        "manager1");

    }

}
