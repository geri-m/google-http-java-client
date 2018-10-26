package com.google.api.client.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

public class XmlListTest {


  public static class ArrayWithClassType {
    @Key
    public XmlTest.AnyType[] rep;
  }

  public static class CollectionWithClassType {
    @Key
    public Collection<XmlTest.AnyType> rep;
  }

  public static class ListnWithClassType {
    @Key
    public List<XmlTest.AnyType> rep;
  }

  private static final String ARRAY_TYPE_WITH_CLASS_TYPE =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">" +
          "<rep><elem>content1</elem><rep>rep10</rep><rep>rep11</rep><value>value1</value></rep>" +
          "<rep><elem>content2</elem><rep>rep20</rep><rep>rep21</rep><value>value2</value></rep>" +
          "<rep><elem>content3</elem><rep>rep30</rep><rep>rep31</rep><value>value3</value></rep>" +
          "</any>";

  /**
   * The purpose of this test is to map an XML with an array of Objects correctly. The parse will
   * use a {@link DedicatedObjectParser} to map the {@link ArrayWithClassType} at first, then
   * another {@link DedicatedObjectParser} for the {@link XmlTest.AnyType} Objects in the Array, but the
   * will switch to the {@link MapParser} as the members of {@link XmlTest.AnyType} are of type {@link
   * Object}, with the exception of the {@link XmlTest.ValueType}.
   */
  @Test
  public void testParseArrayTypeWithClassType() throws Exception {
    ArrayWithClassType xml = new ArrayWithClassType();
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

  @Test
  public void testParseCollectionWithClassType() throws Exception {
    CollectionWithClassType xml = new CollectionWithClassType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ARRAY_TYPE_WITH_CLASS_TYPE));
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
    assertEquals(ARRAY_TYPE_WITH_CLASS_TYPE, out.toString());
  }

  private static final String MULTIPLE_STRING_ELEMENT =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<rep>rep1</rep><rep>rep2</rep></any>";

  public static class CollectionTypeString {
    @Key
    public Collection<String> rep;
  }

  public static class ArrayTypeString {
    @Key
    public String[] rep;
  }

  public static class ListTypeString {
    @Key
    public List<String> rep;
  }

  /**
   * The Purpose of this test is to map a given list of elements (Strings) to a {@link Collection}
   * of Strings. This test uses the {@link DedicatedObjectParser} only, as all field/types are
   * specified.
   */
  @Test
  public void testParseCollectionTypeString() throws Exception {
    CollectionTypeString xml = new CollectionTypeString();
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
   * This test uses the {@link DedicatedObjectParser} only, as all field/types are specified.
   */
  @Test
  public void testParseArrayTypeString() throws Exception {
    ArrayTypeString xml = new ArrayTypeString();
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
   * The Purpose of this test is to map a given list of elements (Strings) to a {@link List}
   * of Strings. This test uses the {@link DedicatedObjectParser} only, as all field/types are
   * specified.
   */
  @Test
  public void testParseListTypeString() throws Exception {
    ListTypeString xml = new ListTypeString();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTIPLE_STRING_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(2, xml.rep.size());
    assertEquals("rep1", xml.rep.get(0));
    assertEquals("rep2", xml.rep.get(1));
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTIPLE_STRING_ELEMENT, out.toString());
  }


  private static final String MULTIPLE_INTEGER_ELEMENT =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<rep>1</rep><rep>2</rep></any>";

  public static class CollectionTypeInteger {
    @Key
    public Collection<Integer> rep;
  }

  public static class ArrayTypeInteger {
    @Key
    public Integer[] rep;
  }

  public static class ArrayTypeInt {
    @Key
    public int[] rep;
  }

  public static class ListTypeInteger {
    @Key
    public List<Integer> rep;
  }

  /**
   * The Purpose of this test is to map a given list of elements (Strings) to a {@link Collection}
   * of Strings. This test uses the {@link DedicatedObjectParser} only, as all field/types are
   * specified.
   */
  @Test
  public void testParseCollectionTypeInteger() throws Exception {
    CollectionTypeInteger xml = new CollectionTypeInteger();
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
   * This test uses the {@link DedicatedObjectParser} only, as all field/types are specified.
   */
  @Test
  public void testParseArrayTypeInteger() throws Exception {
    ArrayTypeInteger xml = new ArrayTypeInteger();
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
   * The Purpose of this test is to map a given list of elements (Strings) to a {@link List}
   * of Strings. This test uses the {@link DedicatedObjectParser} only, as all field/types are
   * specified.
   */
  @Test
  public void testParseListTypeInteger() throws Exception {
    ListTypeInteger xml = new ListTypeInteger();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MULTIPLE_INTEGER_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(2, xml.rep.size());
    assertEquals(1, xml.rep.get(0).intValue());
    assertEquals(2, xml.rep.get(1).intValue());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(MULTIPLE_INTEGER_ELEMENT, out.toString());
  }


  /**
   * The Purpose of this test is to map a given list of elements (int) to a {@link List}
   * of Strings. This test uses the {@link DedicatedObjectParser} only, as all field/types are
   * specified.
   */
  @Test
  public void testParseArrayTypeInt() throws Exception {
    ArrayTypeInt xml = new ArrayTypeInt();
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


}
