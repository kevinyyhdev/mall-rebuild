package com.macro.mall.security;

/** Marker for the package owned by the security module. */
public final class SecurityModule {

  private SecurityModule() {
    throw new AssertionError("Module marker must not be instantiated");
  }
}
