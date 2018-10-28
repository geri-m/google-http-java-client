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
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Key;

/**
 * Tests List and Arrays of {@link GenericXml}.
 *
 * @author Gerald Madlmayr
 */

public class GenericXmlListTest {


  private static final String MULTI_TYPE_WITH_CLASS_TYPE = "<?xml version=\"1.0\"?><any " +
      "xmlns=\"http://www.w3.org/2005/Atom\"><rep><elem>content1</elem><rep>rep10</rep><rep>" +
      "rep11</rep><value>value1</value></rep><rep><elem>content2</elem><rep>rep20</rep><rep>rep21"
      + "</rep><value>value2</value></rep><rep><elem>content3</elem><rep>rep30</rep><rep>rep31" +
      "</rep><value>value3</value></rep></any>";
  private static final String MULTIPLE_STRING_ELEMENT = "<?xml version=\"1.0\"?><any xmlns" +
      "=\"http://www.w3.org/2005/Atom\"><rep>rep1</rep><rep>rep2</rep></any>";
  private static final String MULTIPLE_INTEGER_ELEMENT = "<?xml version=\"1.0\"?><any xmlns" +
      "=\"http://www.w3.org/2005/Atom\"><rep>1</rep><rep>2</rep></any>";
  private static final String ARRAY_TYPE_WITH_PRIMITIVE = "<?xml version=\"1.0\"?><any xmlns" +
      "=\"http://www.w3.org/2005/Atom\"><rep>1</rep><rep>2</rep></any>";
  private static final String ARRAY_TYPE_WITH_PRIMITIVE_ADDED_NESTED = "<?xml version=\"1.0" +
      "\"?><any xmlns=\"http://www.w3.org/2005/Atom\"><rep>1<nested>something</nested></rep" +
      "><rep>2</rep></any>";
  private static final String MULTIPLE_ENUM_ELEMENT = "<?xml version=\"1.0\"?><any xmlns" +
      "=\"http://www.w3.org/2005/Atom\"><rep>ENUM_1</rep><rep>ENUM_2</rep></any>";

  /**
   * The purpose of this test is to map an XML with an array of Objects correctly.
   */
  @Test
  public void testParseArrayTypeWithClassType() throws Exception {
    ArrayWithClassTypeGeneric xml = new ArrayWithClassTypeGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTI_TYPE_WITH_CLASS_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertTrue(xml.rep instanceof XmlTest.AnyType[]);
    XmlTest.AnyType[] rep = xml.rep;
    assertNotNull(rep);
    assertEquals(3, rep.length);
    ArrayList<ArrayMap<String, String>> elem0 = (ArrayList<ArrayMap<String, String>>) rep[0].elem;
    assertEquals(1, elem0.size());
    assertEquals("content1", elem0.get(0)
        .get("text()"));
    ArrayList<ArrayMap<String, String>> elem1 = (ArrayList<ArrayMap<String, String>>) rep[1].elem;
    assertEquals(1, elem1.size());
    assertEquals("content2", elem1.get(0)
        .get("text()"));
    ArrayList<ArrayMap<String, String>> elem2 = (ArrayList<ArrayMap<String, String>>) rep[2].elem;
    assertEquals(1, elem2.size());
    assertEquals("content3", elem2.get(0)
        .get("text()"));

    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTI_TYPE_WITH_CLASS_TYPE, out.toString());
  }

  @Test
  public void testParseCollectionWithClassType() throws Exception {
    CollectionWithClassTypeGeneric xml = new CollectionWithClassTypeGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTI_TYPE_WITH_CLASS_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertTrue(xml.rep instanceof Collection);
    Collection<XmlTest.AnyType> rep = xml.rep;
    assertNotNull(rep);
    assertEquals(3, rep.size());


    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTI_TYPE_WITH_CLASS_TYPE, out.toString());
  }

  @Test
  public void testParseMultiGenericWithClassType() throws Exception {
    MultiGenericWithClassType xml = new MultiGenericWithClassType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTI_TYPE_WITH_CLASS_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type

    GenericXml[] rep = xml.rep;
    assertNotNull(rep);
    assertEquals(3, rep.length);
    assertEquals("text()", ((ArrayMap<String, String>) (rep[0].values()
        .toArray(new ArrayList[]{})[0].get(0))).getKey(0));
    assertEquals("content1", ((ArrayMap<String, String>) (rep[0].values()
        .toArray(new ArrayList[]{})[0].get(0))).getValue(0));
    assertEquals("text()", ((ArrayMap<String, String>) (rep[1].values()
        .toArray(new ArrayList[]{})[0].get(0))).getKey(0));
    assertEquals("content2", ((ArrayMap<String, String>) (rep[1].values()
        .toArray(new ArrayList[]{})[0].get(0))).getValue(0));
    assertEquals("text()", ((ArrayMap<String, String>) (rep[2].values()
        .toArray(new ArrayList[]{})[0].get(0))).getKey(0));
    assertEquals("content3", ((ArrayMap<String, String>) (rep[2].values()
        .toArray(new ArrayList[]{})[0].get(0))).getValue(0));

    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTI_TYPE_WITH_CLASS_TYPE, out.toString());
  }

  @Test
  public void testParseMultiGenericWithClassTypeGeneric() throws Exception {
    MultiGenericWithClassTypeGeneric xml = new MultiGenericWithClassTypeGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTI_TYPE_WITH_CLASS_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type

    GenericXml[] rep = xml.rep;
    assertNotNull(rep);
    assertEquals(3, rep.length);
    assertEquals("text()", ((ArrayMap<String, String>) (rep[0].values()
        .toArray(new ArrayList[]{})[0].get(0))).getKey(0));
    assertEquals("content1", ((ArrayMap<String, String>) (rep[0].values()
        .toArray(new ArrayList[]{})[0].get(0))).getValue(0));
    assertEquals("text()", ((ArrayMap<String, String>) (rep[1].values()
        .toArray(new ArrayList[]{})[0].get(0))).getKey(0));
    assertEquals("content2", ((ArrayMap<String, String>) (rep[1].values()
        .toArray(new ArrayList[]{})[0].get(0))).getValue(0));
    assertEquals("text()", ((ArrayMap<String, String>) (rep[2].values()
        .toArray(new ArrayList[]{})[0].get(0))).getKey(0));
    assertEquals("content3", ((ArrayMap<String, String>) (rep[2].values()
        .toArray(new ArrayList[]{})[0].get(0))).getValue(0));

    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTI_TYPE_WITH_CLASS_TYPE, out.toString());
  }

  /**
   * The Purpose of this test is to map a given list of elements (Strings) to a {@link Collection}
   * of Strings.
   */
  @Test
  public void testParseCollectionTypeString() throws Exception {
    CollectionTypeStringGeneric xml = new CollectionTypeStringGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTIPLE_STRING_ELEMENT));
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
    assertEquals(MULTIPLE_STRING_ELEMENT, out.toString());
  }

  /**
   * The Purpose of this test is to map a given list of elements (Strings) to a String-Array.
   */
  @Test
  public void testParseArrayTypeString() throws Exception {
    ArrayTypeStringGeneric xml = new ArrayTypeStringGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTIPLE_STRING_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(2, xml.rep.length);
    assertEquals("rep1", xml.rep[0]);
    assertEquals("rep2", xml.rep[1]);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTIPLE_STRING_ELEMENT, out.toString());
  }

  /**
   * The Purpose of this test is to map a given list of elements (Strings) to a {@link Collection}
   * of Strings.
   */
  @Test
  public void testParseCollectionTypeInteger() throws Exception {
    CollectionTypeIntegerGeneric xml = new CollectionTypeIntegerGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTIPLE_INTEGER_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(2, xml.rep.size());
    assertEquals(1, xml.rep.toArray(new Integer[]{})[0].intValue());
    assertEquals(2, xml.rep.toArray(new Integer[]{})[1].intValue());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTIPLE_INTEGER_ELEMENT, out.toString());
  }

  /**
   * The Purpose of this test is to map a given list of elements (Strings) to a String-Array.
   */
  @Test
  public void testParseArrayTypeInteger() throws Exception {
    ArrayTypeIntegerGeneric xml = new ArrayTypeIntegerGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTIPLE_INTEGER_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(2, xml.rep.length);
    assertEquals(1, xml.rep[0].intValue());
    assertEquals(2, xml.rep[1].intValue());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTIPLE_INTEGER_ELEMENT, out.toString());
  }

  /**
   * The Purpose of this test is to map a given list of elements (int) to a {@link List} of
   * Strings.
   */
  @Test
  public void testParseArrayTypeInt() throws Exception {
    ArrayTypeIntGeneric xml = new ArrayTypeIntGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTIPLE_INTEGER_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(2, xml.rep.length);
    assertEquals(1, xml.rep[0]);
    assertEquals(2, xml.rep[1]);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTIPLE_INTEGER_ELEMENT, out.toString());
  }

  @Test
  public void testParse_arrayTypeWithPrimitive() throws Exception {
    assertEquals(ARRAY_TYPE_WITH_PRIMITIVE, testStandardXml(ARRAY_TYPE_WITH_PRIMITIVE));
  }

  @Test
  public void testParse_arrayTypeWithPrimitiveWithNestedElement() throws Exception {
    assertEquals(ARRAY_TYPE_WITH_PRIMITIVE,
        testStandardXml(ARRAY_TYPE_WITH_PRIMITIVE_ADDED_NESTED));
  }

  private String testStandardXml(final String xmlString) throws Exception {
    ArrayTypeIntGeneric xml = new ArrayTypeIntGeneric();
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

  @Test
  public void testParseCollectionTypeWithEnum() throws Exception {
    CollectionTypeEnumGeneric xml = new CollectionTypeEnumGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTIPLE_ENUM_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(2, xml.rep.size());
    assertEquals(XmlEnumTest.AnyEnum.ENUM_1, xml.rep.toArray(new XmlEnumTest.AnyEnum[]{})[0]);
    assertEquals(XmlEnumTest.AnyEnum.ENUM_2, xml.rep.toArray(new XmlEnumTest.AnyEnum[]{})[1]);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTIPLE_ENUM_ELEMENT, out.toString());
  }

  @Test
  public void testParseArrayTypeWithEnum() throws Exception {
    ArrayTypeEnumGeneric xml = new ArrayTypeEnumGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTIPLE_ENUM_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(2, xml.rep.length);
    assertEquals(XmlEnumTest.AnyEnum.ENUM_1, xml.rep[0]);
    assertEquals(XmlEnumTest.AnyEnum.ENUM_2, xml.rep[1]);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTIPLE_ENUM_ELEMENT, out.toString());
  }

  public static class ArrayWithClassTypeGeneric extends GenericXml {
    @Key
    public XmlTest.AnyType[] rep;
  }

  public static class CollectionWithClassTypeGeneric extends GenericXml {
    @Key
    public Collection<XmlTest.AnyType> rep;
  }

  public static class MultiGenericWithClassType {
    @Key
    public GenericXml[] rep;
  }

  public static class MultiGenericWithClassTypeGeneric extends GenericXml {
    @Key
    public GenericXml[] rep;
  }

  public static class CollectionTypeStringGeneric extends GenericXml {
    @Key
    public Collection<String> rep;
  }

  public static class ArrayTypeStringGeneric extends GenericXml {
    @Key
    public String[] rep;
  }

  public static class CollectionTypeIntegerGeneric extends GenericXml {
    @Key
    public Collection<Integer> rep;
  }

  public static class ArrayTypeIntegerGeneric extends GenericXml {
    @Key
    public Integer[] rep;
  }

  public static class ArrayTypeIntGeneric extends GenericXml {
    @Key
    public int[] rep;
  }

  public static class CollectionTypeEnumGeneric extends GenericXml {
    @Key
    public Collection<XmlEnumTest.AnyEnum> rep;
  }

  public static class ArrayTypeEnumGeneric extends GenericXml {
    @Key
    public XmlEnumTest.AnyEnum[] rep;
  }

}
