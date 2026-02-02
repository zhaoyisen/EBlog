package com.eblog.admin;

import com.eblog.api.common.ApiResponse;
import com.eblog.metadata.CategoryEntity;
import com.eblog.metadata.CategoryMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminCategoryController {

    private final CategoryMapper categoryMapper;

    public AdminCategoryController(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @GetMapping
    public ApiResponse<List<CategoryEntity>> list() {
        return ApiResponse.ok(categoryMapper.selectList(null));
    }

    @PostMapping
    public ApiResponse<CategoryEntity> create(@RequestBody CategoryEntity category) {
        if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
            // Simple fallback: slugify name
            category.setSlug(toSlug(category.getName()));
        }
        category.setPostCount(0);
        category.setCreatedAt(LocalDateTime.now());
        
        categoryMapper.insert(category);
        return ApiResponse.ok(category);
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryEntity> update(@PathVariable Long id, @RequestBody CategoryEntity updates) {
        CategoryEntity category = categoryMapper.selectById(id);
        if (category == null) {
            return ApiResponse.fail("404", "Category not found");
        }
        
        if (updates.getName() != null) category.setName(updates.getName());
        if (updates.getSlug() != null) category.setSlug(updates.getSlug());
        if (updates.getDescription() != null) category.setDescription(updates.getDescription());
        
        categoryMapper.updateById(category);
        return ApiResponse.ok(category);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    private String toSlug(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}
