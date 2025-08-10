package com.myshop.productservice.service;

import com.myshop.productservice.dto.NewPhotos;
import com.myshop.productservice.dto.UpdateAvatar;
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

    private final kafkaProducer  kafkaProducer;

    private final PhotosRepository photosRepository;

    public PhotoService(AvatarRepository avatarRepository, ProductRepository productRepository, kafkaProducer kafkaProducer, PhotosRepository photosRepository) {
        this.avatarRepository = avatarRepository;
        this.productRepository = productRepository;
        this.kafkaProducer = kafkaProducer;
        this.photosRepository = photosRepository;
    }

    @Transactional
    public UpdateAvatar setAvatar(UpdateAvatar updateAvatar) {
        Optional<Product> temp = productRepository.findById(updateAvatar.getId());

        if(temp.isEmpty()) {
            throw new RuntimeException("Product not found");
        }

        if(updateAvatar.getAvatar().getProduct() == null) {
            avatarRepository.setUrlByProductId(updateAvatar.getAvatar().getUrl(), updateAvatar.getId());
        }
        else {
            avatarRepository.save(updateAvatar.getAvatar());
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

    public List<Photos> getPhotos(List<UUID> ids){
        if(ids.isEmpty()){
            return List.of();
        }
        return photosRepository.findAllById(ids);
    }

    public ResponseEntity<String> deletePhoto(UUID id){
        photosRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> addPhotos(NewPhotos newPhotos){
        Optional<Product> temp = productRepository.findById(newPhotos.getProductId());
        if(temp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        Product product = temp.get();
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
