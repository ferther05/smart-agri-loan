package com.agriloan.repository;

import com.agriloan.domain.Product;
import com.agriloan.domain.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByEnabledTrueOrderByRateAsc();

    List<Product> findByEnabledTrueAndTypeOrderByRateAsc(ProductType type);

    List<Product> findByEnabledTrueAndHotTrueOrderByApplyCountDesc();
}
