package com.test.mapper;

import com.test.dto.ProductDto;

import java.util.List;

public interface ProductMapper {

    List<ProductDto> findAll();

    ProductDto findById(Long id);

    void decreaseStock(Long productId, int quantity);
}
