package org.example.project_badminton.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project_badminton.dto.request.CourtRequest;
import org.example.project_badminton.dto.request.CourtUpdateRequest;
import org.example.project_badminton.dto.response.ApiResponse;
import org.example.project_badminton.dto.response.CourtDTO;
import org.example.project_badminton.instance.CourtStatus;
import org.example.project_badminton.service.ManagerCourtService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/court")
@RequiredArgsConstructor
public class CourtController {

    private final ManagerCourtService managerCourtService;




    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/my-courts")
    public ResponseEntity<ApiResponse<List<CourtDTO>>> getAllCourtForManager(
            Authentication authentication,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir

    ){
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        String name = authentication.getName();
        List<CourtDTO> user = managerCourtService.getAllCourtForManager(keyword,name, pageable).getContent();
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách sân thành công", user));
    }
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CourtDTO>>> getAllCourt(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ){
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        List<CourtDTO> user = managerCourtService.getAllCourt(keyword, pageable).getContent();
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách sân thành công", user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CourtDTO>> createCourt(@Valid @RequestBody CourtRequest courtRequest){
        CourtDTO court = managerCourtService.createCourt(courtRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Create Thành Công", court));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourtDTO>> updateCourt(
            Authentication authentication,
            @Valid @PathVariable Long id, @RequestBody CourtUpdateRequest request){
        String name = authentication.getName();
        CourtDTO court =  managerCourtService.updateCourt(id, request, name);
        return ResponseEntity.ok(new ApiResponse<>(true, "Update Thành Công", court));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> closeCourt(@Valid @PathVariable Long id){
        managerCourtService.closeCourt(id, CourtStatus.CLOSED);
        return ResponseEntity.ok(new ApiResponse<>(true, "Close court success !", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/open/{id}")
    public ResponseEntity<ApiResponse<Void>> openCourt(@Valid @PathVariable Long id){
        managerCourtService.closeCourt(id, CourtStatus.AVAILABLE);
        return ResponseEntity.ok(new ApiResponse<>(true, "Open court success !", null));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CourtDTO>> uploadCourtImages(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        String username = authentication.getName();
        CourtDTO court = managerCourtService.uploadImagesToCourt(id, files, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Upload ảnh thành công", court));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<ApiResponse<CourtDTO>> deleteCourtImage(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long imageId) {
        String username = authentication.getName();
        CourtDTO court = managerCourtService.deleteImageFromCourt(id, imageId, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xóa ảnh thành công", court));
    }
}