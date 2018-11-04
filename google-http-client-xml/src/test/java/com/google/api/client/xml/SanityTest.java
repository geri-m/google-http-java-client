package com.google.api.client.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.google.api.client.util.ArrayMap;
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

    Object map = new ArrayMap<String, String>();
    Object list = new ArrayList<String>();
    Collection<String> listCollection = new ArrayList<String>();
    // need to better understand this.
    // assertFalse(map instanceof Class<List<String>>);
    assertFalse(map instanceof ParameterizedType);
    assertFalse(list instanceof ParameterizedType);
    assertFalse(listCollection instanceof ParameterizedType);


    Object arr = new String[]{};
    assertFalse(arr instanceof ParameterizedType);
  }

}
