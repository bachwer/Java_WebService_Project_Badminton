package org.example.project_badminton.service;


import jakarta.transaction.Transactional;
import org.example.project_badminton.dto.request.CourtRequest;
import org.example.project_badminton.dto.request.CourtUpdateRequest;
import org.example.project_badminton.dto.response.CourtDTO;
import org.example.project_badminton.entity.Court;
import org.example.project_badminton.entity.CourtImage;
import org.example.project_badminton.entity.User;
import org.example.project_badminton.exception.ConflictException;
import org.example.project_badminton.exception.ResourceNotFoundException;
import org.example.project_badminton.instance.CourtStatus;
import org.example.project_badminton.instance.Role;
import org.example.project_badminton.mapper.CourtMapper;
import org.example.project_badminton.repository.CourtImageRepository;
import org.example.project_badminton.repository.CourtRepository;
import org.example.project_badminton.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ManagerCourtService {
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourtImageRepository courtImageRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    @Transactional
    public Page<CourtDTO> getAllCourtForManager(String keyword, String ManagerName, Pageable pageable){
        Page<Court> courts;
        User u = userRepository.findByUsername(ManagerName);

        if(u == null) throw new ResourceNotFoundException("Lỗi khi lấy data User");


        if (keyword != null && !keyword.trim().isEmpty()) {
            courts = courtRepository.findByNameContainingIgnoreCaseAndManagerId(keyword,u.getId(), pageable);
        } else {
            courts = courtRepository.findByManagerId(u.getId(), pageable);
        }
        return courts.map(CourtMapper::toDTO);
    }

    @Transactional
    public Page<CourtDTO> getAllCourt(String keyword, Pageable pageable){
        Page<Court> courts;

        if (keyword != null && !keyword.trim().isEmpty()) {
            courts = courtRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            courts = courtRepository.findAll(pageable);
        }
        return courts.map(CourtMapper::toDTO);
    }

    //admin
    @Transactional
    public CourtDTO createCourt(CourtRequest dto){
        Court court = Court.builder()
                .manager(checkRoleManager(dto.getManagerId()))
                .name(dto.getName())
                .description(dto.getDescription())
                .pricePerHour(dto.getPricePerHour())
                .status(CourtStatus.AVAILABLE)
                .build();
        courtRepository.save(court);
        return CourtMapper.toDTO(court);

    }


    //admin
    @Transactional
    public CourtDTO updateCourt(Long id, CourtUpdateRequest dto, String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException("Lỗi khi lấy data User");
        }

        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found Court Id!"));

        // Các field được phép update cho cả ADMIN và MANAGER
        if (dto.getName() != null) {
            court.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            court.setDescription(dto.getDescription());
        }

        if (dto.getPricePerHour() != null) {
            court.setPricePerHour(dto.getPricePerHour());
        }

        if (dto.getImages() != null) {
            court.setImages(dto.getImages());
        }

        // Chỉ ADMIN được sửa status và manager
        if (user.getRole() == Role.ROLE_ADMIN) {

            if (dto.getStatus() != null) {
                court.setStatus(dto.getStatus());
            }

            if (dto.getManagerId() != null) {

                User manager = userRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

                if (manager.getRole() != Role.ROLE_MANAGER) {
                    throw new IllegalArgumentException("User không phải Manager");
                }

                court.setManager(manager);
            }
        }

        courtRepository.save(court);

        return CourtMapper.toDTO(court);
    }


    //admin
    @Transactional
    public void closeCourt(Long id, CourtStatus status){
       Court court = courtRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found Court Id"));
        court.setStatus(status);
        courtRepository.save(court);
    }

    @Transactional
    public CourtDTO uploadImagesToCourt(Long courtId, List<MultipartFile> files, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("Lỗi khi lấy data User");
        }

        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found Court Id"));

        if (user.getRole() != Role.ROLE_ADMIN && !court.getManager().getId().equals(user.getId())) {
            throw new ConflictException("Bạn không có quyền cập nhật ảnh cho sân này");
        }

        for (MultipartFile file : files) {
            String url = cloudinaryService.uploadImage(file);
            CourtImage courtImage = CourtImage.builder()
                    .court(court)
                    .imageUrl(url)
                    .build();
            court.getImages().add(courtImage);
        }

        courtRepository.save(court);
        return CourtMapper.toDTO(court);
    }

    @Transactional
    public CourtDTO deleteImageFromCourt(Long courtId, Long imageId, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("Lỗi khi lấy data User");
        }

        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found Court Id"));

        if (user.getRole() != Role.ROLE_ADMIN && !court.getManager().getId().equals(user.getId())) {
            throw new ConflictException("Bạn không có quyền cập nhật ảnh cho sân này");
        }

        CourtImage image = courtImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found Image Id"));

        if (!image.getCourt().getId().equals(courtId)) {
            throw new ConflictException("Ảnh không thuộc sân này");
        }

        court.getImages().remove(image);
        courtImageRepository.delete(image);
        courtRepository.save(court);
        return CourtMapper.toDTO(court);
    }

    public User checkRoleManager(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Manager Id notFound !!"));
        if(user.getRole() != Role.ROLE_MANAGER){
            throw new ConflictException("invalid Role");
        }
        return user;
    }

}
