package com.eblog.api.common;

public enum ErrorCode {
  BAD_REQUEST("BAD_REQUEST", "请求参数错误"),
  UNAUTHORIZED("UNAUTHORIZED", "未登录或登录已失效"),
  FORBIDDEN("FORBIDDEN", "无权限"),
  RATE_LIMITED("RATE_LIMITED", "请求过于频繁，请稍后再试"),
  INVITE_CODE_INVALID("INVITE_CODE_INVALID", "邀请码无效"),
  EMAIL_CODE_INVALID("EMAIL_CODE_INVALID", "邮箱验证码错误或已过期"),
  DUPLICATE_EMAIL("DUPLICATE_EMAIL", "邮箱已注册"),
  PASSWORD_RESET_TOKEN_INVALID("PASSWORD_RESET_TOKEN_INVALID", "重置令牌无效或已过期"),
  USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在"),
  POST_NOT_FOUND("POST_NOT_FOUND", "文章不存在"),
  COMMENT_NOT_FOUND("COMMENT_NOT_FOUND", "评论不存在"),
  TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "请求过于频繁"),
  NOT_FOUND("NOT_FOUND", "资源不存在"),
  CONFLICT("CONFLICT", "资源冲突或已存在"),
  INTERNAL_ERROR("INTERNAL_ERROR", "服务器内部错误");

  private final String code;
  private final String message;

  ErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
