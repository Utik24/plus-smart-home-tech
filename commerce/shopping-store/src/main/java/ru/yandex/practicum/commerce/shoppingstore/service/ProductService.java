package ru.yandex.practicum.commerce.shoppingstore.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.shoppingstore.entity.Product;
import ru.yandex.practicum.commerce.shoppingstore.entity.ProductCategory;
import ru.yandex.practicum.commerce.shoppingstore.entity.ProductState;
import ru.yandex.practicum.commerce.shoppingstore.entity.dto.ProductDto;
import ru.yandex.practicum.commerce.shoppingstore.enums.QuantityState;
import ru.yandex.practicum.commerce.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.commerce.shoppingstore.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductDto> getProductsByCategory(ProductCategory productCategory, Pageable pageable) throws FeignException {
        Page<Product> products = productRepository.findByProductCategory(productCategory, pageable);
        return products.map(productMapper::toProductDto);
    }

    @Transactional
    public ProductDto addProduct(ProductDto productDto) throws FeignException {
        Product product = productMapper.toProduct(productDto);
        Product savedProduct = productRepository.save(product);
        return productMapper.toProductDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) throws FeignException {
        UUID productId = productDto.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));
        updateProductFilds(product, productDto);
        return productMapper.toProductDto(productRepository.save(product));
    }

    @Transactional
    public boolean removeProductFromStore(UUID productId) throws FeignException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public Boolean setQuantityState(UUID productId, QuantityState quantityState) throws FeignException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));
        product.setQuantityState(quantityState);
        productRepository.save(product);
        return true;
    }

    public ProductDto getProductById(UUID productId) throws FeignException {
        Product product = productRepository.findById(productId).orElseThrow(() ->{
            return new RuntimeException("Продукт не найден");
        });
        return productMapper.toProductDto(product);
    }

    private void updateProductFilds(Product product, ProductDto productDto) throws FeignException {
        if (productDto.getProductName() != null) {
            product.setProductName(productDto.getProductName());
        }
        if (productDto.getDescription() != null) {
            product.setDescription(productDto.getDescription());
        }
        if (productDto.getImageSrc() != null) {
            product.setImageSrc(productDto.getImageSrc());
        }
        if (productDto.getQuantityState() != null) {
            product.setQuantityState(productDto.getQuantityState());
        }
        if (productDto.getProductState() != null) {
            product.setProductState(productDto.getProductState());
        }
        if (productDto.getRating() > 0) {
            product.setRating(productDto.getRating());
        }
        if (productDto.getProductCategory() != null) {
            product.setProductCategory(productDto.getProductCategory());
        }
        if (productDto.getPrice() != null && productDto.getPrice().compareTo(BigDecimal.ONE) >= 0) {
            product.setPrice(productDto.getPrice());
        }
    }

}
