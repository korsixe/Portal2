package com.mipt.portal.repository;

import com.mipt.portal.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, CustomCategoryRepository {

  List<Category> findAllByParentIsNullOrderByNameAsc();

  List<Category> findAllByParentIdOrderByNameAsc(Long categoryId);
}
