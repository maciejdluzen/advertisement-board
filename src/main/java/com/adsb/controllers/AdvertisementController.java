package com.adsb.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.adsb.models.Advertisement;
import com.adsb.models.AdvertisementRepository;
import com.fasterxml.jackson.annotation.JsonProperty;

import javassist.NotFoundException;


@RestController
@RequestMapping(path = AdvertisementController.PATH)
@RequestScope
@Validated
public class AdvertisementController {
    public static final String PATH = "/api/v1/ads";
    public static final String PATH_PAGES = PATH + "/pages";
    public static final int FIRST_PAGE_ID = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    private AdvertisementRepository adRepository;
    
    @Inject
    public AdvertisementController(AdvertisementRepository repository) {
        this.adRepository = repository;
    }

    @GetMapping
    public ResponseEntity<AdvertisementList> advertisements() {
        return advertisementsForPage(FIRST_PAGE_ID);
    }
    
    @GetMapping("/pages/{pageId}")
    public ResponseEntity<AdvertisementList> advertisementsForPage(@PathVariable("pageId") int pageId) {
        Page<Advertisement> page = adRepository.findAll(PageRequest.of(pageId, DEFAULT_PAGE_SIZE));
        return new ResponseEntity<AdvertisementList>(new AdvertisementList(page.getContent()), 
                buildLinkHeader(page, PATH_PAGES), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Advertisement advertisementById(@PathVariable("id") @Min(0) Long id) throws NotFoundException {
        throwIfNonexisting(id);
        return adRepository.getOne(id);
    }
    
    @PostMapping
    public ResponseEntity<Advertisement> add(@Valid @RequestBody Advertisement advertisement, 
            UriComponentsBuilder uriComponentsBuilder) throws URISyntaxException {
        throwIfIdNotNull(advertisement.getId());
        
        Advertisement savedAdvertisement = adRepository.save(advertisement);
       
        UriComponents uriComponents = uriComponentsBuilder.path(PATH + "/{id}")
                .buildAndExpand(savedAdvertisement.getId());
        return ResponseEntity.created(URI.create(uriComponents.getPath())).body(savedAdvertisement);
    }
    
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        adRepository.deleteAll();
    }
    
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) throws NotFoundException {
        throwIfNonexisting(id);
        adRepository.deleteById(id);
    }
    
    @PutMapping("/{id}")
    public Advertisement update(@PathVariable("id") Long id, @RequestBody Advertisement updatedAd) throws NotFoundException {
        throwIfInconsistent(id, updatedAd.getId());
        throwIfNonexisting(id);
        return adRepository.save(updatedAd);
    }
    
    public static HttpHeaders buildLinkHeader(Page<?> page, String path) {
        StringBuilder linkHeader = new StringBuilder();
        if(page.hasPrevious()) {
            int prevNumber = page.getNumber() - 1;
            linkHeader.append("<").append(path).append(prevNumber).append(">; rel=\"previous\"");
            if(!page.isLast())
                linkHeader.append(", ");
        }
        if(page.hasNext()) {
            int nextNumber = page.getNumber() + 1;
            linkHeader.append("<").append(path).append(nextNumber).append(">; rel=\"next\"");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LINK, linkHeader.toString());
        return headers;
    }
    
    private static void throwIfIdNotNull(final Long id) {
        if (id != null && id.intValue() != 0) {
            String message = String
                    .format("Remove 'id' property from request or use PUT method to update resource with id = %d", id);
            throw new BadRequestException(message);
        }
    }
    
    private void throwIfNonexisting(Long id) throws NotFoundException {
        if (!adRepository.existsById(id)) {
            throw new NotFoundException(id + " not found");
        }
    }
    
    private void throwIfInconsistent(Long expected, Long actual) {
        if(!expected.equals(actual)) {
            String message = String.format(
                    "bad request, inconsistent IDs between request and object: request id = %d, object id = %d", expected, actual);
            throw new BadRequestException(message);
        }
    }
    
    public static class AdvertisementList {
        @JsonProperty("value")
        public List<Advertisement> advertisements = new ArrayList<>();
        
        public AdvertisementList(Iterable<Advertisement> ads) {
            ads.forEach(advertisements::add);
        }
    }
}
