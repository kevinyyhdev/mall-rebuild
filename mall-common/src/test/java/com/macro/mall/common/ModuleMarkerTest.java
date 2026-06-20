package com.macro.mall.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class ModuleMarkerTest {

  @Test
  void markerReservesTheCommonPackageWithoutBeingInstantiable() {
    assertEquals("com.macro.mall.common", CommonModule.class.getPackageName());
    assertTrue(Modifier.isFinal(CommonModule.class.getModifiers()));

    Constructor<?>[] constructors = CommonModule.class.getDeclaredConstructors();
    assertEquals(1, constructors.length);
    assertFalse(constructors[0].canAccess(null));
  }
}
