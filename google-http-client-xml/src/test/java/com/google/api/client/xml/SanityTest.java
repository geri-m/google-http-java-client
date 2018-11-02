package com.google.api.client.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.google.api.client.util.ClassInfo;

/**
 * These are some tests, to assert how certain methods used in the {@link Xml} are working.
 */

public class SanityTest {

  @Test
  public void testCreateClassInfo(){
    Object destination = new Object();
    ClassInfo classInfo = ClassInfo.of(destination.getClass());
    assertEquals(classInfo.getIgnoreCase(), false);
  }

  @Test
  public void testInstanceOf(){
    Object destination = new Object();
    assertTrue(destination instanceof Object);
    assertFalse(destination instanceof Void);
  }

}
