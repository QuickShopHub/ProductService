package com.myshop.productservice.service;

import com.myshop.productservice.dto.UpdateAvatar;
import com.myshop.productservice.repository.Avatar;
import com.myshop.productservice.repository.AvatarRepository;
import com.myshop.productservice.repository.Product;
import com.myshop.productservice.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class AvatarService {
    private final AvatarRepository avatarRepository;

    private final ProductRepository productRepository;

    private final kafkaProducer  kafkaProducer;

    public AvatarService(AvatarRepository avatarRepository, ProductRepository productRepository, kafkaProducer kafkaProducer) {
        this.avatarRepository = avatarRepository;
        this.productRepository = productRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public UpdateAvatar updateAvatar(UpdateAvatar updateAvatar) {
        if(avatarRepository.setUrlByProductId(updateAvatar.getAvatarUrl(), updateAvatar.getId()) == 1){

            Optional<Product> product = productRepository.findById(updateAvatar.getId());

            if(product.isPresent()) {
                kafkaProducer.sendUpdate(kafkaProducer.getProductForSearchFromProduct(product.get()));
            }

            return updateAvatar;
        }




        throw new IllegalArgumentException("Product with id: " + updateAvatar.getId() + " not found");
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

    //todo добавить реализацию фоток

}
