package com.eblog.announcement;

import com.eblog.api.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

  private final AnnouncementMapper announcementMapper;

  public AnnouncementController(AnnouncementMapper announcementMapper) {
    this.announcementMapper = announcementMapper;
  }

  @GetMapping
  public ApiResponse<List<AnnouncementEntity>> list() {
    return ApiResponse.ok(announcementMapper.listLatest());
  }
}
