/*
 * Copyright (c) 2010 Google Inc.
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
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

/**
 * Tests {@link GenericXml}.
 *
 * @author Yaniv Inbar
 * @author Gerald Madlmayr
 */
public class GenericXmlTest{

  public GenericXmlTest() {
  }

  private static final String XML =
      "<?xml version=\"1.0\"?><feed xmlns=\"http://www.w3.org/2005/Atom\" "
          + "xmlns:gd=\"http://schemas.google.com/g/2005\"><atom:entry "
          + "xmlns=\"http://schemas.google.com/g/2005\" "
          + "xmlns:atom=\"http://www.w3.org/2005/Atom\" "
          + "gd:etag=\"abc\"><atom:title>One</atom:title></atom:entry>"
          + "<entry gd:etag=\"def\"><title>Two</title></entry></feed>";


  public static class AnyGenericType {
    @Key("@attr")
    public Object attr;
    @Key
    public GenericXml elem;
  }


  private static final String ANY_GENERIC_TYPE_XML =
      "<?xml version=\"1.0\"?><any attr=\"value\" xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<elem><rep attr=\"param1\">rep1</rep><rep attr=\"param2\">rep2</rep><value>content</value></elem></any>";

  @SuppressWarnings("cast")
  @Test
  public void testParse_anyGenericType() throws Exception {
    AnyGenericType xml = new AnyGenericType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_GENERIC_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    Collection<GenericXml> repList = (Collection<GenericXml>) xml.elem.get("rep");
    assertEquals(2, repList.size());
    Collection<GenericXml> repValue = (Collection<GenericXml>) xml.elem.get("value");
    assertEquals(1, repValue.size());
    // 1st rep element
    assertEquals("@attr", ((Map.Entry)repList.toArray(new ArrayMap[]{})[0].entrySet().toArray(new Map.Entry[]{})[0]).getKey());
    assertEquals("param1", ((Map.Entry)repList.toArray(new ArrayMap[]{})[0].entrySet().toArray(new Map.Entry[]{})[0]).getValue());
    assertEquals("text()", ((Map.Entry)repList.toArray(new ArrayMap[]{})[0].entrySet().toArray(new Map.Entry[]{})[1]).getKey());
    assertEquals("rep1", ((Map.Entry)repList.toArray(new ArrayMap[]{})[0].entrySet().toArray(new Map.Entry[]{})[1]).getValue());
    // 2nd rep element
    assertEquals("@attr", ((Map.Entry)repList.toArray(new ArrayMap[]{})[1].entrySet().toArray(new Map.Entry[]{})[0]).getKey());
    assertEquals("param2", ((Map.Entry)repList.toArray(new ArrayMap[]{})[1].entrySet().toArray(new Map.Entry[]{})[0]).getValue());
    assertEquals("text()", ((Map.Entry)repList.toArray(new ArrayMap[]{})[1].entrySet().toArray(new Map.Entry[]{})[1]).getKey());
    assertEquals("rep2", ((Map.Entry)repList.toArray(new ArrayMap[]{})[1].entrySet().toArray(new Map.Entry[]{})[1]).getValue());
    // value element
    assertEquals("text()", ((Map.Entry)repValue.toArray(new ArrayMap[]{})[0].entrySet().toArray(new Map.Entry[]{})[0]).getKey());
    assertEquals("content", ((Map.Entry)repValue.toArray(new ArrayMap[]{})[0].entrySet().toArray(new Map.Entry[]{})[0]).getValue());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_GENERIC_TYPE_XML, out.toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testParse() throws Exception {
    GenericXml xml = new GenericXml();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    ArrayMap<String, String> expected =
        ArrayMap.of("gd", "http://schemas.google.com/g/2005", "", "http://www.w3.org/2005/Atom");
    assertEquals(expected, namespaceDictionary.getAliasToUriMap());
    assertEquals("feed", xml.name);
    Collection<GenericXml> foo = (Collection<GenericXml>) xml.get("entry");
    assertEquals(2, foo.size());
    ArrayMap<String, String> singleElementOne =ArrayMap.of("text()", "One");
    List<ArrayMap<String, String>> testOne = new ArrayList<ArrayMap<String, String>>();
    testOne.add(singleElementOne);
    assertEquals("abc", foo.toArray(new ArrayMap[]{})[0].get("@gd:etag"));
    assertEquals(testOne, foo.toArray(new ArrayMap[]{})[0].get("title"));
    ArrayMap<String, String> singleElementTwo =ArrayMap.of("text()", "Two");
    List<ArrayMap<String, String>> testTwo = new ArrayList<ArrayMap<String, String>>();
    testTwo.add(singleElementTwo);
    assertEquals("def", foo.toArray(new ArrayMap[]{})[1].get("@gd:etag"));
    assertEquals(testTwo, foo.toArray(new ArrayMap[]{})[1].get("title"));
  }

  private static final String COLLECTION_TYPE =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<rep>rep1</rep><rep>rep2</rep></any>";

  public static class CollectionTypeAsGenericXml extends GenericXml {
    @Key
    public Collection<String> rep;
  }

  @Test
  public void testParse_collectionTypeAsGenericXml() throws Exception {
    CollectionTypeAsGenericXml xml = new CollectionTypeAsGenericXml();
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


  public static class ArrayTypeWithClassTypeAsGenericXml extends GenericXml {
    @Key
    public XmlTest.AnyType[] rep;
  }

  private static final String ARRAY_TYPE_WITH_CLASS_TYPE =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">" +
          "<rep><elem>content1</elem><rep>rep10</rep><rep>rep11</rep><value>content</value></rep>" +
          "<rep><elem>content2</elem><rep>rep20</rep><rep>rep21</rep><value>content</value></rep>" +
          "<rep><elem>content3</elem><rep>rep30</rep><rep>rep31</rep><value>content</value></rep>" +
          "</any>";

  @Test
  public void testParse_arrayTypeWithClassTypeAsGenericXml() throws Exception {
    ArrayTypeWithClassTypeAsGenericXml xml = new ArrayTypeWithClassTypeAsGenericXml();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ARRAY_TYPE_WITH_CLASS_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertTrue(xml.rep instanceof XmlTest.AnyType[]);
    XmlTest.AnyType[] rep = xml.rep;
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

  private static final String SIMPLE_XML_NUMERIC = "<any xmlns=\"\">1</any>";

  @Test
  public void testParseSimpleInteger() throws Exception {
    GenericXml xml = new GenericXml();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML_NUMERIC));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals ("text()", ((Map.Entry<String, String>)xml.entrySet().toArray()[0]).getKey());
    assertEquals ("1", ((Map.Entry<String, String>)xml.entrySet().toArray()[0]).getValue());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\">1</any>", out.toString());
  }


  private static final String SIMPLE_XML = "<any xmlns=\"\">test</any>";

  @Test
  public void testParseSimpleString() throws Exception {
    GenericXml xml = new GenericXml();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals ("text()", ((Map.Entry<String, String>)xml.entrySet().toArray()[0]).getKey());
    assertEquals ("test", ((Map.Entry<String, String>)xml.entrySet().toArray()[0]).getValue());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\">test</any>", out.toString());
  }

  private static final String COLLECTION_TYPE_WITH_ENUM =
      "<?xml version=\"1.0\"?><any xmlns=\"\">"
          + "<rep>ENUM_1</rep><rep>ENUM_2</rep></any>";

  public static class CollectionGenericType extends GenericXml {
    @Key
    public Collection<XmlEnumTest.AnyEnum> rep;
  }

  @Test
  public void testParse_collectionTypeWithEnum() throws Exception {
    CollectionGenericType xml = new CollectionGenericType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(COLLECTION_TYPE_WITH_ENUM));
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
    assertEquals(COLLECTION_TYPE_WITH_ENUM, out.toString());
  }

  public static class ArrayGenericType extends GenericXml {
    @Key
    public XmlEnumTest.AnyEnum[] rep;
  }

  @Test
  public void testParseArrayTypeWithEnum() throws Exception {
    ArrayGenericType xml = new ArrayGenericType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(COLLECTION_TYPE_WITH_ENUM));
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
    assertEquals(COLLECTION_TYPE_WITH_ENUM, out.toString());
  }

}
