 package com.eblog.feed;

 import com.eblog.api.common.ApiResponse;
 import com.eblog.post.PostEntity;
 import com.eblog.post.PostMapper;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
 import org.springframework.http.MediaType;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBean(PostMapper.class)
public class FeedController {

  private final PostMapper postMapper;
  private final String baseUrl;

  public FeedController(PostMapper postMapper, @Value("${app.base-url}") String baseUrl) {
    this.postMapper = postMapper;
    this.baseUrl = baseUrl;
  }

  @GetMapping(value = "/feed", produces = MediaType.APPLICATION_ATOM_XML_VALUE)
  public String rss() {
    List<PostEntity> posts = postMapper.selectList(
      new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostEntity>()
        .eq(PostEntity::getStatus, "PUBLISHED")
        .eq(PostEntity::getModerationStatus, "APPROVED")
        .orderByDesc(PostEntity::getCreatedAt)
        .last("LIMIT 50")
    );

    StringBuilder rss = new StringBuilder();
    rss.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    rss.append("<rss version=\"2.0\">\n");
    rss.append("  <channel>\n");
    rss.append("    <title>EBlog</title>\n");
    rss.append("    <link>").append(baseUrl).append("</link>\n");
    rss.append("    <description>多用户技术博客平台</description>\n");
    rss.append("    <language>zh-cn</language>\n");
    
    for (PostEntity post : posts) {
      rss.append("    <item>\n");
      rss.append("      <title>").append(escapeXml(post.getTitle())).append("</title>\n");
      rss.append("      <link>").append(baseUrl).append("/posts/").append(post.getSlug()).append("</link>\n");
      if (post.getSummary() != null && !post.getSummary().isEmpty()) {
        rss.append("      <description>").append(escapeXml(post.getSummary())).append("</description>\n");
      }
      rss.append("      <pubDate>").append(post.getCreatedAt().toString()).append("</pubDate>\n");
      rss.append("    </item>\n");
    }
    
    rss.append("  </channel>\n");
    rss.append("</rss>");
    return rss.toString();
  }

  @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public String sitemap() {
    List<PostEntity> posts = postMapper.selectList(
      new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostEntity>()
        .eq(PostEntity::getStatus, "PUBLISHED")
        .eq(PostEntity::getModerationStatus, "APPROVED")
        .select(PostEntity::getSlug)
        .last("LIMIT 50000")
    );

    StringBuilder sitemap = new StringBuilder();
    sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
    
    sitemap.append("  <url>\n");
    sitemap.append("    <loc>").append(baseUrl).append("/posts</loc>\n");
    sitemap.append("    <changefreq>daily</changefreq>\n");
    sitemap.append("  </url>\n");
    
    for (PostEntity post : posts) {
      sitemap.append("  <url>\n");
      sitemap.append("    <loc>").append(baseUrl).append("/posts/").append(post.getSlug()).append("</loc>\n");
      sitemap.append("    <lastmod>").append(post.getUpdatedAt().toString()).append("</lastmod>\n");
      sitemap.append("  </url>\n");
    }
    
    sitemap.append("</urlset>");
    return sitemap.toString();
  }

  private String escapeXml(String input) {
    if (input == null) {
      return "";
    }
    return input.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&apos;");
  }
}
