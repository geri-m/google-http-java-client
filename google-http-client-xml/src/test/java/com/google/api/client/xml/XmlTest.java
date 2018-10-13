/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Key;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

/**
 * Tests {@link Xml}.
 *
 * @author Yaniv Inbar
 */
public class XmlTest {

  public static class AnyType {
    @Key("@attr")
    public Object attr;
    @Key
    public Object elem;
    @Key
    public Object rep;
    @Key
    public ValueType value;
  }


  public static class ValueType {
    @Key("text()")
    public Object content;
  }

  private static final String ANY_TYPE_XML =
      "<?xml version=\"1.0\"?><any attr=\"value\" xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<elem>content</elem><rep>rep1</rep><rep>rep2</rep><value>content</value></any>";

  @SuppressWarnings("cast")
  @Test
  public void testParse_anyType() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertTrue(xml.rep.toString(), xml.rep instanceof ArrayList<?>);
    assertTrue(xml.value instanceof ValueType);
    assertTrue(xml.value.content instanceof String);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML, out.toString());
  }

  // this test only ensures, that there is no exception during paring with a NULL destination
  @Test
  public void testParse_anyTypeWithNullDestination() throws Exception {
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, null, namespaceDictionary, null);
  }

  @Test
  public void testParse_anyTypeWithCustomParser() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, new Xml.CustomizeParser());
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertTrue(xml.rep.toString(), xml.rep instanceof ArrayList<?>);
    assertTrue(xml.value instanceof ValueType);
    assertTrue(xml.value.content instanceof String);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML, out.toString());
  }


  public static class AnyTypePrimitive {
    @Key("text()")
    public int value;
  }

  private static final String ANY_TYPE_XML_PRIMITIVE =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">1</any>";

  @Test
  public void testParse_anyTypePrimitive() throws Exception {
    AnyTypePrimitive xml = new AnyTypePrimitive();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML_PRIMITIVE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, new Xml.CustomizeParser());
    assertEquals(1, xml.value);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML_PRIMITIVE, out.toString());
  }

  public static class ArrayType extends GenericXml {
    @Key
    public Map<String, String>[] rep;
  }

  private static final String ARRAY_TYPE =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<rep>rep1</rep><rep>rep2</rep></any>";

  @Test
  public void testParse_arrayType() throws Exception {
    ArrayType xml = new ArrayType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ARRAY_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    Map<String, String>[] rep = xml.rep;
    assertEquals(2, rep.length);
    ArrayMap<String, String> map0 = (ArrayMap<String, String>) rep[0];
    assertEquals(1, map0.size());
    assertEquals("rep1", map0.get("text()"));
    ArrayMap<String, String> map1 = (ArrayMap<String, String>) rep[1];
    assertEquals(1, map1.size());
    assertEquals("rep2", map1.get("text()"));
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ARRAY_TYPE, out.toString());
  }

  public static class ArrayTypeWithPrimitive extends GenericXml {
    @Key
    public int[] rep;
  }

  private static final String ARRAY_TYPE_WITH_PRIMITIVE =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<rep>1</rep><rep>2</rep></any>";


  @Test
  public void testParse_arrayTypeWithPrimitive() throws Exception {
    assertEquals(ARRAY_TYPE_WITH_PRIMITIVE, testStandardXml(ARRAY_TYPE_WITH_PRIMITIVE));
  }


  private static final String ARRAY_TYPE_WITH_PRIMITIVE_ADDED_NESTED =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<rep>1<nested>something</nested></rep><rep>2</rep></any>";

  @Test
  public void testParse_arrayTypeWithPrimitiveWithNestedElement() throws Exception {
    assertEquals(ARRAY_TYPE_WITH_PRIMITIVE, testStandardXml(ARRAY_TYPE_WITH_PRIMITIVE_ADDED_NESTED));
  }

  private String testStandardXml(final String xmlString) throws Exception {
    ArrayTypeWithPrimitive xml = new ArrayTypeWithPrimitive();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(xmlString));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    int[] rep = xml.rep;
    assertNotNull(rep);
    assertEquals(2, rep.length);
    assertEquals(1, rep[0]);
    assertEquals(2, rep[1]);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    return out.toString();
  }

  public static class ArrayTypeWithClassType {
    @Key
    public AnyType[] rep;
  }

  private static final String ARRAY_TYPE_WITH_CLASS_TYPE =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">" +
          "<rep><elem>content1</elem><rep>rep10</rep><rep>rep11</rep><value>content</value></rep>" +
          "<rep><elem>content2</elem><rep>rep20</rep><rep>rep21</rep><value>content</value></rep>" +
          "<rep><elem>content3</elem><rep>rep30</rep><rep>rep31</rep><value>content</value></rep>" +
          "</any>";

  @Test
  public void testParse_arrayTypeWithClassType() throws Exception {
    ArrayTypeWithClassType xml = new ArrayTypeWithClassType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ARRAY_TYPE_WITH_CLASS_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertTrue(xml.rep instanceof AnyType[]);
    AnyType[] rep = xml.rep;
    assertNotNull(rep);
    assertEquals(3, rep.length);
    ArrayList<ArrayMap<String, String>> elem0 = (ArrayList<ArrayMap<String, String>>) rep[0].elem;
    assertEquals(1, elem0.size());
    assertEquals("content1", elem0.get(0).get("text()"));
    ArrayList<ArrayMap<String, String>> elem1 = (ArrayList<ArrayMap<String, String>>) rep[1].elem;
    assertEquals(1, elem1.size());
    assertEquals("content2", elem1.get(0).get("text()"));
    ArrayList<ArrayMap<String, String>> elem2 = (ArrayList<ArrayMap<String, String>>) rep[2].elem;
    assertEquals(1, elem2.size());
    assertEquals("content3", elem2.get(0).get("text()"));

    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ARRAY_TYPE_WITH_CLASS_TYPE, out.toString());
  }

  private static final String NESTED_NS =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<app:edited xmlns:app='http://www.w3.org/2007/app'>2011-08-09T04:38:14.017Z"
          + "</app:edited></any>";

  private static final String NESTED_NS_SERIALIZED =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\" "
          + "xmlns:app=\"http://www.w3.org/2007/app\"><app:edited>2011-08-09T04:38:14.017Z"
          + "</app:edited></any>";

  @Test
  public void testParse_nestedNs() throws Exception {
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(NESTED_NS));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    GenericXml xml = new GenericXml();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // GenericXml anyValue = (GenericXml) xml.get("any");
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(NESTED_NS_SERIALIZED, out.toString());
  }

  private static final String COLLECTION_TYPE =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<rep>rep1</rep><rep>rep2</rep></any>";

  public static class CollectionType {
    @Key
    public Collection<String> rep;
  }

  @Test
  public void testParse_collectionType() throws Exception {
    CollectionType xml = new CollectionType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(COLLECTION_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(2, xml.rep.size());
    assertEquals("rep1", xml.rep.toArray(new String[]{})[0]);
    assertEquals("rep2", xml.rep.toArray(new String[]{})[1]);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(COLLECTION_TYPE, out.toString());
  }

  public static class SimpleType {
    @Key("text()")
    public String value;
  }

  private static final String SIMPLE_XML = "<any>test</any>";

  @Test
  public void testParse_simpleType() throws Exception {
    SimpleType xml = new SimpleType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\">test</any>", out.toString());
  }

}
