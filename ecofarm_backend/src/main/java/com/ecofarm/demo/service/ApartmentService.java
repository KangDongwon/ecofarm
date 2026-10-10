package com.ecofarm.demo.service;

import com.ecofarm.demo.dto.ApartmentDTO;
import com.ecofarm.demo.entity.Admin;
import com.ecofarm.demo.entity.Apartment;
import com.ecofarm.demo.repository.ApartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ApartmentService {

    @Autowired
    private ApartmentRepository apartmentRepository;

    public List<Apartment> findAll() {
        return apartmentRepository.findAll();
    }

    public Optional<Apartment> findById(Long id) {
        return apartmentRepository.findById(id);
    }

    public Apartment save(Apartment apartment) {
        return apartmentRepository.save(apartment);
    }

    public void deleteById(Long id) {
        apartmentRepository.deleteById(id);
    }

    public boolean existsByName(String name) { return apartmentRepository.existsByApartmentName(name); }

    @Transactional
    public Apartment updateApartment(ApartmentDTO apartmentDTO) {
        // DB에서 해당 아파트를 찾습니다.
        Optional<Apartment> optionalApartment = apartmentRepository.findById(apartmentDTO.getApartmentID());

        if (optionalApartment.isPresent()) {
            Apartment apartment = optionalApartment.get();
            // 업데이트할 필드들을 설정합니다.
            apartment.setApartmentName(apartmentDTO.getApartmentName());
            apartment.setAddress(apartmentDTO.getAddress());
            apartment.setContractStartDate(apartmentDTO.getContractStartDate());
            apartment.setContractEndDate(apartmentDTO.getContractEndDate());

            // 변경 사항을 저장합니다.
            return apartmentRepository.save(apartment);
        } else {
            throw new RuntimeException("Apartment not found with ID: " + apartmentDTO.getApartmentID());
        }
    }

    @Transactional
    public void createTestApartments() {
        for (int i = 0; i < 10; i++) {
            Apartment apartment = new Apartment();
            apartment.setApartmentName(APARTMENT_NAMES[i]);
            apartment.setAddress(APARTMENT_ADDRESS[i]);

            apartment.setContractStartDate(LocalDate.now());
            apartment.setContractEndDate(LocalDate.now().plusDays(365));

            apartmentRepository.save(apartment);
        }
    }

    private static final String[] APARTMENT_ADDRESS = {
            "세종시 마음로 181",
            "세종시 마음로 151",
            "세종시 나성동 364",
            "세종시 보람로 15",
            "세종시 새롬북로 13",
            "세종시 남세종로 441",
            "세종시 시청대로 236",
            "세종시 반곡로 14",
            "세종시 갈매로 480",
            "세종시 누리로 59"
    };

    private static final String[] APARTMENT_NAMES = {
            "가락마을 10,13,14단지",
            "가락마을 9,11,12단지",
            "나릿재마을 3,4,6단지",
            "도램마을 9,10,14,15단지",
            "새뜸마을 4,7,10단지",
            "호려울마을 5단지",
            "새샘마을 3,4단지",
            "수루배마을 1,3단지",
            "한뜰마을 2,3단지",
            "첫마을 5,6단지"
    };
}
