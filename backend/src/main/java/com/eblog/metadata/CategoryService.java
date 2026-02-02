package com.eblog.metadata;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eblog.api.common.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryEntity> list() {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<CategoryEntity>()
                .orderByAsc(CategoryEntity::getName)
        );
    }

    public Page<CategoryEntity> list(int page, int size) {
        int safePage = Math.max(page, 1) - 1; // MyBatis-Plus 从 0 开始
        int safeSize = Math.min(Math.max(size, 1), 100);
        return categoryMapper.selectPage(
            new Page<>(safePage, safeSize),
            new LambdaQueryWrapper<CategoryEntity>().orderByAsc(CategoryEntity::getName)
        );
    }

    public CategoryEntity get(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return categoryMapper.selectById(id);
    }

    public CategoryEntity getBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return null;
        }
        return categoryMapper.selectOne(
            new LambdaQueryWrapper<CategoryEntity>().eq(CategoryEntity::getSlug, slug.trim())
        );
    }

    @Transactional
    public CategoryEntity create(String name, String description, String slug) {
        if (!isAdmin()) {
            return null;
        }
        if (isBlank(name)) {
            return null;
        }

        // 检查是否已存在同名分类
        CategoryEntity existing = categoryMapper.selectOne(
            new LambdaQueryWrapper<CategoryEntity>().eq(CategoryEntity::getName, name.trim())
        );
        if (existing != null) {
            return null;
        }

        CategoryEntity entity = new CategoryEntity();
        entity.setName(name.trim());
        entity.setDescription(description == null ? null : description.trim());
        entity.setSlug(isBlank(slug) ? SlugGenerator.slugify(name) : slug.trim());
        entity.setPostCount(0);
        entity.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        categoryMapper.insert(entity);
        return entity;
    }

    @Transactional
    public ErrorCode update(Long id, String name, String description, String slug) {
        if (!isAdmin()) {
            return ErrorCode.FORBIDDEN;
        }
        if (id == null || id <= 0) {
            return ErrorCode.BAD_REQUEST;
        }

        CategoryEntity existing = categoryMapper.selectById(id);
        if (existing == null) {
            return ErrorCode.NOT_FOUND;
        }

        // 如果修改名称，检查是否与其他分类重名
        if (!isBlank(name) && !name.trim().equals(existing.getName())) {
            CategoryEntity duplicate = categoryMapper.selectOne(
                new LambdaQueryWrapper<CategoryEntity>()
                    .eq(CategoryEntity::getName, name.trim())
                    .ne(CategoryEntity::getId, id)
            );
            if (duplicate != null) {
                return ErrorCode.CONFLICT;
            }
            existing.setName(name.trim());
        }

        if (description != null) {
            existing.setDescription(description.trim());
        }

        if (!isBlank(slug) && !slug.trim().equals(existing.getSlug())) {
            CategoryEntity duplicate = categoryMapper.selectOne(
                new LambdaQueryWrapper<CategoryEntity>()
                    .eq(CategoryEntity::getSlug, slug.trim())
                    .ne(CategoryEntity::getId, id)
            );
            if (duplicate != null) {
                return ErrorCode.CONFLICT;
            }
            existing.setSlug(slug.trim());
        }

        categoryMapper.updateById(existing);
        return null;
    }

    @Transactional
    public ErrorCode delete(Long id) {
        if (!isAdmin()) {
            return ErrorCode.FORBIDDEN;
        }
        if (id == null || id <= 0) {
            return ErrorCode.BAD_REQUEST;
        }

        CategoryEntity existing = categoryMapper.selectById(id);
        if (existing == null) {
            return ErrorCode.NOT_FOUND;
        }

        // 检查是否有关联文章
        if (existing.getPostCount() != null && existing.getPostCount() > 0) {
            return ErrorCode.CONFLICT;
        }

        categoryMapper.deleteById(id);
        return null;
    }

    @Transactional
    public void incrementPostCount(Long categoryId) {
        if (categoryId != null) {
            CategoryEntity category = categoryMapper.selectById(categoryId);
            if (category != null) {
                int count = category.getPostCount() == null ? 0 : category.getPostCount();
                category.setPostCount(count + 1);
                categoryMapper.updateById(category);
            }
        }
    }

    @Transactional
    public void decrementPostCount(Long categoryId) {
        if (categoryId != null) {
            CategoryEntity category = categoryMapper.selectById(categoryId);
            if (category != null) {
                int count = category.getPostCount() == null ? 0 : category.getPostCount();
                category.setPostCount(Math.max(0, count - 1));
                categoryMapper.updateById(category);
            }
        }
    }

    private static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            String role = a.getAuthority();
            if ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
