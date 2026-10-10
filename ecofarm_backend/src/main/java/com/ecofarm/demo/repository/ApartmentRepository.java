package com.ecofarm.demo.repository;

import com.ecofarm.demo.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    boolean existsByApartmentName(String apartmentName);

}
