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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
 * @author Gerald Madlmayr
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

  public static class AnyTypeMissingField {
    @Key("@attr")
    public Object attr;
    @Key
    public Object elem;
    @Key
    public ValueType value;
  }

  public static class AnyTypeAdditionalField {
    @Key("@attr")
    public Object attr;
    @Key
    public Object elem;
    @Key
    public Object rep;
    @Key
    public Object additionalField;
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

  private static final String ANY_TYPE_MISSING_XML ="<?xml version=\"1.0\"?><any attr=\"value\" xmlns=\"http://www.w3" +
      ".org/2005/Atom\"><elem>content</elem><value>content</value></any>";

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


  @SuppressWarnings("cast")
  @Test
  public void testParse_anyTypeMissingField() throws Exception {
    AnyTypeMissingField xml = new AnyTypeMissingField();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertTrue(xml.value instanceof ValueType);
    assertTrue(xml.value.content instanceof String);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_MISSING_XML, out.toString());
  }


  @SuppressWarnings("cast")
  @Test
  public void testParse_anyTypeAdditionalField() throws Exception {
    AnyTypeAdditionalField xml = new AnyTypeAdditionalField();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertTrue(xml.value instanceof ValueType);
    assertNull(xml.additionalField);
    assertTrue(xml.rep.toString(), xml.rep instanceof ArrayList<?>);
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
  public void testParseSimpleTypeAsValueString() throws Exception {
    SimpleType xml = new SimpleType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals("test", xml.value);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\">test</any>", out.toString());
  }

  public static class SimpleTypeNumeric {
    @Key("text()")
    public int value;
  }

  private static final String SIMPLE_XML_NUMERIC = "<any>1</any>";

  @Test
  public void testParseSimpleTypeAsValueInteger() throws Exception {
    SimpleTypeNumeric xml = new SimpleTypeNumeric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML_NUMERIC));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(1, xml.value);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\">1</any>", out.toString());
  }


  private static final String START_WITH_TEXT = "<?xml version=\"1.0\"?>start_with_text</any>";

  @Test
  public void testWithTextFail() throws Exception {
    SimpleType xml = new SimpleType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(START_WITH_TEXT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    try{
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      fail();
    } catch (final Exception e){
      assertEquals("only whitespace content allowed before start tag and not s (position: START_DOCUMENT seen <?xml version=\"1.0\"?>s... @1:22)", e.getMessage().trim());
    }
  }

  private static final String START_MISSING_END_ELEMENT = "<?xml version=\"1.0\"?><any xmlns=\"\">missing_end_element";

  @Test
  public void testWithMissingEndElementFail() throws Exception {
    SimpleType xml = new SimpleType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(START_MISSING_END_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    try{
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      fail();
    } catch (final Exception e){
      assertEquals("no more data available - expected end tag </any> to close start tag <any> from line 1, parser stopped on START_TAG seen ...<any " +
          "xmlns=\"\">missing_end_element... @1:54", e.getMessage().trim());
    }
  }

  private static final String START_WITH_END_ELEMENT = "<?xml version=\"1.0\"?></p><any xmlns=\"\">start_with_end_elemtn</any>";

  @Test
  public void testWithEndElementStarting() throws Exception {
    SimpleType xml = new SimpleType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(START_WITH_END_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    try{
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      fail();
    } catch (final Exception e){
      assertEquals("expected start tag name and not / (position: START_DOCUMENT seen <?xml version=\"1.0\"?></... @1:23)", e.getMessage().trim());
    }
  }

  private static final String START_WITH_END_ELEMENT_NESTED = "<?xml version=\"1.0\"?><any xmlns=\"\"></p>start_with_end_element_nested</any>";


  @Test
  public void testWithEndElementNested() throws Exception {
    SimpleType xml = new SimpleType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(START_WITH_END_ELEMENT_NESTED));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    try{
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      fail();
    } catch (final Exception e){
      assertEquals("end tag name </p> must match start tag name <any> from line 1 (position: START_TAG seen ...<any xmlns=\"\"></p>... @1:39)", e.getMessage().trim());
    }
  }


  @Test
  public void testFailMappingOfDataType() throws Exception {
    SimpleTypeNumeric xml = new SimpleTypeNumeric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    try{
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      fail();
    } catch (final Exception e){
      assertEquals("For input string: \"test\"", e.getMessage().trim());
    }
  }

  private static class AnyTypeInf {
    @Key
    private double dblInfNeg;
    @Key
    private double dblInfPos;
    @Key
    private float fltInfNeg;
    @Key
    private float fltInfPos;
  }

  private static final String INF_TEST = "<?xml version=\"1.0\"?><any xmlns=\"\"><dblInfNeg>-INF</dblInfNeg><dblInfPos>INF</dblInfPos><fltInfNeg>-INF</fltInfNeg><fltInfPos>INF</fltInfPos></any>";

  @Test
  public void testParseInfiniteValues() throws Exception {
    AnyTypeInf xml = new AnyTypeInf();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(INF_TEST));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(Double.NEGATIVE_INFINITY, xml.dblInfNeg, 0.0001);
    assertEquals(Double.POSITIVE_INFINITY, xml.dblInfPos, 0.0001);
    assertEquals(Float.NEGATIVE_INFINITY, xml.fltInfNeg, 0.0001);
    assertEquals(Float.POSITIVE_INFINITY, xml.dblInfPos, 0.0001);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(INF_TEST, out.toString());
  }


  private static class AllType {
    @Key
    private int integer;
    @Key
    private String str;
    @Key
    private GenericXml genericXml;
    @Key
    private XmlEnumTest.AnyEnum anyEnum;
    @Key
    private String[] stringArray;
    @Key
    private Collection<Integer> integerCollection;
  }

  private static final String ALL_TYPE = "<?xml version=\"1.0\"?><any xmlns=\"\">"
      +"<integer/><str/><genericXml/><anyEnum/><stringArray/><integerCollection/>"
      +"</any>";

  @Test
  public void testParseEmptyElements() throws Exception {
    AllType xml = new AllType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ALL_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(0, xml.integer);
    assertNotNull(xml.genericXml);
    assertNotNull(xml.integerCollection);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\"><genericXml /><integer>0</integer></any>", out.toString());
  }

  @Test
  public void testParseIncorrectMapping() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ALL_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("","");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertNull(xml.elem);
    assertNull(xml.value);
    assertNull(xml.rep);
    assertNull(xml.rep);

    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\" />", out.toString());
  }

  private static final String ANY_TYPE_XML_NESTED_ARRAY =
      "<?xml version=\"1.0\"?><any attr=\"value\" xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<elem>content</elem><rep><p>rep1</p><p>rep2</p></rep><rep><p>rep3</p><p>rep4</p></rep><value>content</value></any>";

  @SuppressWarnings("cast")
  @Test
  public void testParseAnyTypeWithNestedArray() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML_NESTED_ARRAY));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertTrue(xml.rep.toString(), xml.rep instanceof ArrayList<?>);
    assertTrue(xml.value instanceof ValueType);
    assertTrue(xml.value.content instanceof String);
    assertEquals(1, ((ArrayList<?>)xml.elem).size());
    assertEquals(2, ((ArrayList<?>)xml.rep).size());
    assertEquals(1, ((ArrayList<?>)xml.rep).toArray(new ArrayMap[]{})[0].size());
    assertEquals(1, ((ArrayList<?>)xml.rep).toArray(new ArrayMap[]{})[1].size());


    assertEquals("rep1", ((ArrayList<?>)((ArrayList<?>)xml.rep).toArray(new ArrayMap[]{})[0].get("p")).toArray(new ArrayMap[]{})[0].getValue(0));
    assertEquals("rep2", ((ArrayList<?>)((ArrayList<?>)xml.rep).toArray(new ArrayMap[]{})[0].get("p")).toArray(new ArrayMap[]{})[1].getValue(0));
    assertEquals("rep3", ((ArrayList<?>)((ArrayList<?>)xml.rep).toArray(new ArrayMap[]{})[1].get("p")).toArray(new ArrayMap[]{})[0].getValue(0));
    assertEquals("rep4", ((ArrayList<?>)((ArrayList<?>)xml.rep).toArray(new ArrayMap[]{})[1].get("p")).toArray(new ArrayMap[]{})[1].getValue(0));

    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML_NESTED_ARRAY, out.toString());
  }

}

