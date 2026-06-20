package com.macro.mall.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class ModuleMarkerTest {

  @Test
  void markerReservesThePortalPackageWithoutBeingInstantiable() {
    assertEquals("com.macro.mall.portal", PortalModule.class.getPackageName());
    assertTrue(Modifier.isFinal(PortalModule.class.getModifiers()));

    Constructor<?>[] constructors = PortalModule.class.getDeclaredConstructors();
    assertEquals(1, constructors.length);
    assertFalse(constructors[0].canAccess(null));
  }
}
