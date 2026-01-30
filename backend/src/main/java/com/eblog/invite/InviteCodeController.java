package com.eblog.invite;

import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/invite-codes")
@ConditionalOnBean({InviteCodeMapper.class, InviteCodeUseMapper.class})
public class InviteCodeController {

  private static final int DEFAULT_LIMIT = 50;
  private static final int MAX_LIMIT = 200;

  private final InviteCodeService inviteCodeService;

  public InviteCodeController(InviteCodeService inviteCodeService) {
    this.inviteCodeService = inviteCodeService;
  }

  @PostMapping("/batch-create")
  public ApiResponse<CreateResponse> batchCreate(@RequestBody CreateRequest body) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }
    if (body == null || body.count <= 0 || body.maxUses <= 0 || body.count > 1000) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    List<String> codes = inviteCodeService.createCodes(body.count, body.maxUses, body.expiresAt);
    CreateResponse res = new CreateResponse();
    res.codes = codes;
    return ApiResponse.ok(res);
  }

  @PostMapping("/revoke")
  public ApiResponse<Object> revoke(@RequestBody RevokeRequest body) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }
    if (body == null || body.code == null || body.code.trim().isEmpty()) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    boolean revoked = inviteCodeService.revoke(body.code.trim());
    if (!revoked) {
      return ApiResponse.fail(ErrorCode.INVITE_CODE_INVALID.getCode(), ErrorCode.INVITE_CODE_INVALID.getMessage());
    }
    return ApiResponse.ok(null);
  }

  @GetMapping
  public ApiResponse<List<InviteCodeSummary>> list(
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "limit", required = false, defaultValue = "50") int limit,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }
    int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
    int safeOffset = Math.max(offset, 0);
    List<InviteCodeEntity> codes = inviteCodeService.listCodes(status, safeLimit, safeOffset);
    List<InviteCodeSummary> res = new ArrayList<>();
    for (InviteCodeEntity entity : codes) {
      InviteCodeSummary summary = new InviteCodeSummary();
      summary.code = entity.getCode();
      summary.status = entity.getStatus();
      summary.maxUses = entity.getMaxUses();
      summary.usedCount = entity.getUsedCount();
      summary.expiresAt = entity.getExpiresAt();
      summary.createdAt = entity.getCreatedAt();
      summary.revokedAt = entity.getRevokedAt();
      res.add(summary);
    }
    return ApiResponse.ok(res);
  }

  @GetMapping("/{code}/uses")
  public ApiResponse<List<InviteCodeUseView>> listUses(
      @PathVariable("code") String code,
      @RequestParam(name = "limit", required = false, defaultValue = "50") int limit,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }
    if (code == null || code.trim().isEmpty()) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    InviteCodeEntity invite = inviteCodeService.findByCode(code.trim());
    if (invite == null) {
      return ApiResponse.fail(ErrorCode.INVITE_CODE_INVALID.getCode(), ErrorCode.INVITE_CODE_INVALID.getMessage());
    }
    int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
    int safeOffset = Math.max(offset, 0);
    List<InviteCodeUseEntity> uses = inviteCodeService.listUses(invite.getId(), safeLimit, safeOffset);
    List<InviteCodeUseView> res = new ArrayList<>();
    for (InviteCodeUseEntity use : uses) {
      InviteCodeUseView view = new InviteCodeUseView();
      view.usedByUserId = use.getUsedByUserId();
      view.usedIp = use.getUsedIp();
      view.usedAt = use.getUsedAt();
      res.add(view);
    }
    return ApiResponse.ok(res);
  }

  private static boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getAuthorities() == null) {
      return false;
    }
    for (GrantedAuthority authority : auth.getAuthorities()) {
      String role = authority.getAuthority();
      if ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role)) {
        return true;
      }
    }
    return false;
  }

  public static class CreateRequest {
    public int count;
    public int maxUses;
    public LocalDateTime expiresAt;
  }

  public static class CreateResponse {
    public List<String> codes;
  }

  public static class RevokeRequest {
    public String code;
  }

  public static class InviteCodeSummary {
    public String code;
    public String status;
    public Integer maxUses;
    public Integer usedCount;
    public LocalDateTime expiresAt;
    public LocalDateTime createdAt;
    public LocalDateTime revokedAt;
  }

  public static class InviteCodeUseView {
    public Long usedByUserId;
    public String usedIp;
    public LocalDateTime usedAt;
  }
}
