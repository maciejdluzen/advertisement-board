package com.adsb.models;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findByTitle(String title);

    List<Advertisement> findFirst1ByTitle(String title, Sort sort);
     
}
