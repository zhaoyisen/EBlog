package com.eblog.metadata;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eblog.post.PostEntity;
import com.eblog.post.PostMapper;
import com.eblog.post.TagParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MetadataSyncRunner implements CommandLineRunner {

    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    public MetadataSyncRunner(PostMapper postMapper, CategoryMapper categoryMapper, TagMapper tagMapper) {
        this.postMapper = postMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Select all PUBLISHED and APPROVED posts
        List<PostEntity> posts = postMapper.selectList(
            Wrappers.<PostEntity>lambdaQuery()
                .eq(PostEntity::getStatus, "PUBLISHED")
                .eq(PostEntity::getModerationStatus, "APPROVED")
        );

        Map<String, Integer> categoryCounts = new HashMap<>();
        Map<String, Integer> tagCounts = new HashMap<>();

        for (PostEntity post : posts) {
            // Category
            String cat = post.getCategory();
            if (cat != null && !cat.trim().isEmpty()) {
                categoryCounts.merge(cat.trim(), 1, Integer::sum);
            }

            // Tags
            if (post.getTagsCsv() != null) {
                List<String> tags = TagParser.parseTags(post.getTagsCsv());
                for (String tag : tags) {
                    tagCounts.merge(tag, 1, Integer::sum);
                }
            }
        }

        syncCategories(categoryCounts);
        syncTags(tagCounts);
    }

    private void syncCategories(Map<String, Integer> counts) {
        List<CategoryEntity> existing = categoryMapper.selectList(null);
        Map<String, CategoryEntity> existingMap = new HashMap<>();
        for (CategoryEntity e : existing) {
            existingMap.put(e.getName(), e);
        }

        // Update existing or Insert new
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String name = entry.getKey();
            Integer count = entry.getValue();
            
            if (existingMap.containsKey(name)) {
                CategoryEntity e = existingMap.get(name);
                String slug = TagParser.slugify(name);
                boolean changed = false;
                
                if (!count.equals(e.getPostCount())) {
                    e.setPostCount(count);
                    changed = true;
                }
                if (slug != null && !slug.equals(e.getSlug())) {
                    e.setSlug(slug);
                    changed = true;
                }
                
                if (changed) {
                    categoryMapper.updateById(e);
                }
                existingMap.remove(name);
            } else {
                CategoryEntity e = new CategoryEntity();
                e.setName(name);
                e.setSlug(TagParser.slugify(name));
                e.setPostCount(count);
                e.setCreatedAt(LocalDateTime.now());
                categoryMapper.insert(e);
            }
        }

        // Set remaining to 0
        for (CategoryEntity e : existingMap.values()) {
            if (e.getPostCount() != 0) {
                e.setPostCount(0);
                categoryMapper.updateById(e);
            }
        }
    }

    private void syncTags(Map<String, Integer> counts) {
        List<TagEntity> existing = tagMapper.selectList(null);
        Map<String, TagEntity> existingMap = new HashMap<>();
        for (TagEntity e : existing) {
            existingMap.put(e.getName(), e);
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String name = entry.getKey();
            Integer count = entry.getValue();
            
            if (existingMap.containsKey(name)) {
                TagEntity e = existingMap.get(name);
                String slug = TagParser.slugify(name);
                boolean changed = false;

                if (!count.equals(e.getPostCount())) {
                    e.setPostCount(count);
                    changed = true;
                }
                if (slug != null && !slug.equals(e.getSlug())) {
                    e.setSlug(slug);
                    changed = true;
                }

                if (changed) {
                    tagMapper.updateById(e);
                }
                existingMap.remove(name);
            } else {
                TagEntity e = new TagEntity();
                e.setName(name);
                e.setSlug(TagParser.slugify(name));
                e.setPostCount(count);
                e.setCreatedAt(LocalDateTime.now());
                tagMapper.insert(e);
            }
        }

        for (TagEntity e : existingMap.values()) {
            if (e.getPostCount() != 0) {
                e.setPostCount(0);
                tagMapper.updateById(e);
            }
        }
    }
}
