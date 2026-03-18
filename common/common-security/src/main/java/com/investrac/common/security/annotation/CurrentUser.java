package com.investrac.common.security.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Shorthand for @AuthenticationPrincipal for AuthenticatedUser.
 *
 * Instead of:
 *   @AuthenticationPrincipal AuthenticatedUser user
 *
 * Write:
 *   @CurrentUser AuthenticatedUser user
 *
 * Both work — this is purely for readability.
 *
 * Example:
 *   @GetMapping("/me")
 *   public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
 *       @CurrentUser AuthenticatedUser user) {
 *       return ResponseEntity.ok(userService.getProfile(user.getUserId()));
 *   }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
