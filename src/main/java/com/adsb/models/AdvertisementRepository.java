package com.adsb.models;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AdvertisementRepository extends PagingAndSortingRepository<Advertisement, Long> {

    List<Advertisement> findByTitle(String title);

    List<Advertisement> findFirst1ByTitle(String title, Sort sort);
     
}
