package com.macro.mall.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class ModuleMarkerTest {

  @Test
  void markerReservesThePersistencePackageWithoutBeingInstantiable() {
    assertEquals("com.macro.mall.persistence", PersistenceModule.class.getPackageName());
    assertTrue(Modifier.isFinal(PersistenceModule.class.getModifiers()));

    Constructor<?>[] constructors = PersistenceModule.class.getDeclaredConstructors();
    assertEquals(1, constructors.length);
    assertFalse(constructors[0].canAccess(null));
  }
}
