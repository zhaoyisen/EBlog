package com.eblog.post;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("posts")
public class PostEntity {
  private Long id;
  private Long authorId;
  private String format;
  private String title;
  private String slug;
  private String summary;
  private String contentMarkdown;
  private String tagsCsv;
  private String category;
  private String status;
  private String moderationStatus;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Integer viewCount;
  private Boolean isPinned;
  private Boolean isFeatured;

  public Integer getViewCount() {
    return viewCount;
  }

  public void setViewCount(Integer viewCount) {
    this.viewCount = viewCount;
  }

  public Boolean getIsPinned() {
    return isPinned;
  }

  public void setIsPinned(Boolean isPinned) {
    this.isPinned = isPinned;
  }

  public Boolean getIsFeatured() {
    return isFeatured;
  }

  public void setIsFeatured(Boolean isFeatured) {
    this.isFeatured = isFeatured;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public void setAuthorId(Long authorId) {
    this.authorId = authorId;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getContentMarkdown() {
    return contentMarkdown;
  }

  public void setContentMarkdown(String contentMarkdown) {
    this.contentMarkdown = contentMarkdown;
  }

  public String getTagsCsv() {
    return tagsCsv;
  }

  public void setTagsCsv(String tagsCsv) {
    this.tagsCsv = tagsCsv;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getModerationStatus() {
    return moderationStatus;
  }

  public void setModerationStatus(String moderationStatus) {
    this.moderationStatus = moderationStatus;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
