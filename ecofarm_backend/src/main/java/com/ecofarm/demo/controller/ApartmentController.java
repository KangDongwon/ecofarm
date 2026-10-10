package com.ecofarm.demo.controller;

import com.ecofarm.demo.auth.dto.ErrorResponse;
import com.ecofarm.demo.auth.dto.JsonResponse;
import com.ecofarm.demo.dto.ApartmentDTO;
import com.ecofarm.demo.entity.Apartment;
import com.ecofarm.demo.service.ApartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/apartments")
public class ApartmentController {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private ApartmentService apartmentService;

    @GetMapping("/list")
    public List<ApartmentDTO> getApartmentList() {
        return apartmentService.findAll().stream()
                .map(this::convertToDTOwithIDAndName)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ApartmentDTO> getAllApartments() {
        return apartmentService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApartmentDTO> getApartmentById(@PathVariable Long id) {
        Optional<Apartment> apartment = apartmentService.findById(id);
        return apartment.map(a -> ResponseEntity.ok(convertToDTO(a)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createApartment(@RequestBody ApartmentDTO apartmentDTO) {
        if (apartmentService.existsByName(apartmentDTO.getApartmentName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Apartment already in use."));
        }

        Apartment apartment = convertToEntity(apartmentDTO);
        Apartment savedApartment = apartmentService.save(apartment);
        return ResponseEntity.ok(convertToDTO(savedApartment));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<Apartment> updateApartment(@RequestBody ApartmentDTO apartmentDTO) {
        try {
            Apartment updatedApartment = apartmentService.updateApartment(apartmentDTO);
            return ResponseEntity.ok(updatedApartment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApartment(@PathVariable Long id) {
        apartmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ApartmentDTO convertToDTOwithIDAndName(Apartment apartment) {
        return ApartmentDTO.builder()
                .apartmentID(apartment.getApartmentID())
                .apartmentName(apartment.getApartmentName())
                .build();
    }

    private ApartmentDTO convertToDTO(Apartment apartment) {
        return ApartmentDTO.builder()
                .apartmentID(apartment.getApartmentID())
                .apartmentName(apartment.getApartmentName())
                .address(apartment.getAddress())
                .contractStartDate(apartment.getContractStartDate())
                .contractEndDate(apartment.getContractEndDate())
                .build();
    }

    private Apartment convertToEntity(ApartmentDTO dto) {
        Apartment apartment = new Apartment();
        apartment.setApartmentID(dto.getApartmentID());
        apartment.setApartmentName(dto.getApartmentName());
        apartment.setAddress(dto.getAddress());
        apartment.setContractStartDate(dto.getContractStartDate());
        apartment.setContractEndDate(dto.getContractEndDate());
        return apartment;
    }
}
