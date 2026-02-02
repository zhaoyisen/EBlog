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
public class TagService {
    private final TagMapper tagMapper;

    public TagService(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    public List<TagEntity> list() {
        return tagMapper.selectList(
            new LambdaQueryWrapper<TagEntity>()
                .orderByAsc(TagEntity::getName)
        );
    }

    public Page<TagEntity> list(int page, int size) {
        int safePage = Math.max(page, 1) - 1;
        int safeSize = Math.min(Math.max(size, 1), 100);
        return tagMapper.selectPage(
            new Page<>(safePage, safeSize),
            new LambdaQueryWrapper<TagEntity>().orderByAsc(TagEntity::getName)
        );
    }

    public TagEntity get(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return tagMapper.selectById(id);
    }

    public TagEntity getBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return null;
        }
        return tagMapper.selectOne(
            new LambdaQueryWrapper<TagEntity>().eq(TagEntity::getSlug, slug.trim())
        );
    }

    public List<TagEntity> getPopular(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return tagMapper.selectList(
            new LambdaQueryWrapper<TagEntity>()
                .orderByDesc(TagEntity::getPostCount)
                .last("LIMIT " + safeLimit)
        );
    }

    @Transactional
    public TagEntity create(String name, String slug) {
        if (!isAdmin()) {
            return null;
        }
        if (isBlank(name)) {
            return null;
        }

        TagEntity existing = tagMapper.selectOne(
            new LambdaQueryWrapper<TagEntity>().eq(TagEntity::getName, name.trim())
        );
        if (existing != null) {
            return null;
        }

        TagEntity entity = new TagEntity();
        entity.setName(name.trim());
        entity.setSlug(isBlank(slug) ? SlugGenerator.slugify(name) : slug.trim());
        entity.setPostCount(0);
        entity.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        tagMapper.insert(entity);
        return entity;
    }

    @Transactional
    public ErrorCode update(Long id, String name, String slug) {
        if (!isAdmin()) {
            return ErrorCode.FORBIDDEN;
        }
        if (id == null || id <= 0) {
            return ErrorCode.BAD_REQUEST;
        }

        TagEntity existing = tagMapper.selectById(id);
        if (existing == null) {
            return ErrorCode.NOT_FOUND;
        }

        if (!isBlank(name) && !name.trim().equals(existing.getName())) {
            TagEntity duplicate = tagMapper.selectOne(
                new LambdaQueryWrapper<TagEntity>()
                    .eq(TagEntity::getName, name.trim())
                    .ne(TagEntity::getId, id)
            );
            if (duplicate != null) {
                return ErrorCode.CONFLICT;
            }
            existing.setName(name.trim());
        }

        if (!isBlank(slug) && !slug.trim().equals(existing.getSlug())) {
            TagEntity duplicate = tagMapper.selectOne(
                new LambdaQueryWrapper<TagEntity>()
                    .eq(TagEntity::getSlug, slug.trim())
                    .ne(TagEntity::getId, id)
            );
            if (duplicate != null) {
                return ErrorCode.CONFLICT;
            }
            existing.setSlug(slug.trim());
        }

        tagMapper.updateById(existing);
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

        TagEntity existing = tagMapper.selectById(id);
        if (existing == null) {
            return ErrorCode.NOT_FOUND;
        }

        if (existing.getPostCount() != null && existing.getPostCount() > 0) {
            return ErrorCode.CONFLICT;
        }

        tagMapper.deleteById(id);
        return null;
    }

    @Transactional
    public void incrementPostCount(Long tagId) {
        if (tagId != null) {
            TagEntity tag = tagMapper.selectById(tagId);
            if (tag != null) {
                int count = tag.getPostCount() == null ? 0 : tag.getPostCount();
                tag.setPostCount(count + 1);
                tagMapper.updateById(tag);
            }
        }
    }

    @Transactional
    public void decrementPostCount(Long tagId) {
        if (tagId != null) {
            TagEntity tag = tagMapper.selectById(tagId);
            if (tag != null) {
                int count = tag.getPostCount() == null ? 0 : tag.getPostCount();
                tag.setPostCount(Math.max(0, count - 1));
                tagMapper.updateById(tag);
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
