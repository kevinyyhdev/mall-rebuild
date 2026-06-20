package com.macro.mall.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class ModuleMarkerTest {

  @Test
  void markerReservesTheSearchPackageWithoutBeingInstantiable() {
    assertEquals("com.macro.mall.search", SearchModule.class.getPackageName());
    assertTrue(Modifier.isFinal(SearchModule.class.getModifiers()));

    Constructor<?>[] constructors = SearchModule.class.getDeclaredConstructors();
    assertEquals(1, constructors.length);
    assertFalse(constructors[0].canAccess(null));
  }
}
