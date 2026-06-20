package com.macro.mall.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class ModuleMarkerTest {

  @Test
  void markerReservesTheSecurityPackageWithoutBeingInstantiable() {
    assertEquals("com.macro.mall.security", SecurityModule.class.getPackageName());
    assertTrue(Modifier.isFinal(SecurityModule.class.getModifiers()));

    Constructor<?>[] constructors = SecurityModule.class.getDeclaredConstructors();
    assertEquals(1, constructors.length);
    assertFalse(constructors[0].canAccess(null));
  }
}
