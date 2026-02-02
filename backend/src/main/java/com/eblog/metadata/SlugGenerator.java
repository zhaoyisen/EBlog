package com.eblog.metadata;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class SlugGenerator {
    private static final Random RANDOM = new Random();

    /**
     * 生成一个随机slug
     */
    public static String randomSlug() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 将字符串转换为slug格式
     * 例如："Hello World" -> "hello-world"
     */
    public static String slugify(String input) {
        if (input == null || input.trim().isEmpty()) {
            return randomSlug();
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("[^\\p{ASCII}]", "");
        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = slug.replaceAll("[^a-z0-9\\s-]", "").trim();
        slug = slug.replaceAll("[\\s-]+", "-");
        slug = slug.replaceAll("^-+|-$+", "");

        if (slug.isEmpty()) {
            return randomSlug();
        }

        // 限制长度
        if (slug.length() > 50) {
            slug = slug.substring(0, 50);
        }

        return slug;
    }
}
