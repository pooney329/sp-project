package com.test.service;

import com.test.config.exception.NotFoundException;
import com.test.dto.ProductDto;
import com.test.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<ProductDto> getProductList() {
        return productMapper.findAll();
    }

    public ProductDto getProduct(Long id) {
        ProductDto product = productMapper.findById(id);
        if (product == null) {
            throw new NotFoundException("존재하지 않는 상품입니다.");
        }
        return product;
    }
}
