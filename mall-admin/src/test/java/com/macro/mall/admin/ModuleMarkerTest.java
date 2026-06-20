package com.macro.mall.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class ModuleMarkerTest {

  @Test
  void markerReservesTheAdminPackageWithoutBeingInstantiable() {
    assertEquals("com.macro.mall.admin", AdminModule.class.getPackageName());
    assertTrue(Modifier.isFinal(AdminModule.class.getModifiers()));

    Constructor<?>[] constructors = AdminModule.class.getDeclaredConstructors();
    assertEquals(1, constructors.length);
    assertFalse(constructors[0].canAccess(null));
  }
}
