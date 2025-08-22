package com.myshop.productservice.service;

import com.myshop.productservice.dto.NewPhotos;
import com.myshop.productservice.dto.UpdateAvatar;
import com.myshop.productservice.filter.JwtAuthFilter;
import com.myshop.productservice.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class PhotoService {
    private final AvatarRepository avatarRepository;

    private final ProductRepository productRepository;

    private final KafkaProducer kafkaProducer;

    private final PhotosRepository photosRepository;

    private final JwtAuthFilter jwtAuthFilter;

    public PhotoService(AvatarRepository avatarRepository, ProductRepository productRepository, KafkaProducer kafkaProducer, PhotosRepository photosRepository, JwtAuthFilter jwtAuthFilter) {
        this.avatarRepository = avatarRepository;
        this.productRepository = productRepository;
        this.kafkaProducer = kafkaProducer;
        this.photosRepository = photosRepository;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Transactional
    public UpdateAvatar setAvatar(UpdateAvatar updateAvatar) {
        Optional<Product> temp;
        if(updateAvatar.getId() != null) {
            temp = productRepository.findById(updateAvatar.getId());
            if(temp.isEmpty()) {
                throw new RuntimeException("Product not found");
            }
            if(!jwtAuthFilter.tryDo(temp.get().getIdVendor())){
                throw new RuntimeException("You don't have access");
            }
        }


        if(updateAvatar.getAvatar().getProduct() == null) {
            avatarRepository.setUrlByProductId(updateAvatar.getAvatar().getUrl(), updateAvatar.getId());
        }
        else {
            avatarRepository.save(updateAvatar.getAvatar());
        }
        temp = productRepository.findById(updateAvatar.getAvatar().getProduct().getId());
        if(temp.isEmpty()) {
            throw new RuntimeException("Product not found");
        }
        kafkaProducer.sendUpdate(kafkaProducer.getProductForSearchFromProduct(temp.get()));
        return updateAvatar;
    }


    public List<String> getAvatar(List<UUID> ids){
        if(ids.isEmpty()){
            return List.of();
        }

        List<Avatar> temp = avatarRepository.getUrlsByProductIds(ids);

        List<String> ans = new ArrayList<>();

        for(UUID id : ids){
            for(Avatar avatar : temp){
                if(avatar.getProduct().getId().equals(id)){
                    ans.add(avatar.getUrl());
                }
            }
        }

        return ans;
    }

    public void photoForNewProduct(List<Photos>  photos, Product product){
        for(Photos photo : photos){
            photo.setProduct(product);
            photo.setId(UUID.randomUUID());
            photosRepository.save(photo);
        }
    }

    public List<Photos>  getPhotos(UUID id){

        return photosRepository.findAllByProductId(id);
    }

    public ResponseEntity<String> deletePhoto(UUID id){

        Optional<Photos> temp = photosRepository.findById(id);
        if(temp.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        if(!jwtAuthFilter.tryDo(temp.get().getProduct().getIdVendor())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        photosRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> addPhotos(NewPhotos newPhotos){
        Optional<Product> temp = productRepository.findById(newPhotos.getProductId());
        if(temp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        Product product = temp.get();

        if(!jwtAuthFilter.tryDo(product.getIdVendor())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have access");
        }

        for(String url : newPhotos.getUrls()){
            Photos photo = new Photos();
            photo.setUrl(url);
            photo.setId(UUID.randomUUID());
            photo.setProduct(product);
            photosRepository.save(photo);
        }
        return ResponseEntity.ok().build();
    }
}
