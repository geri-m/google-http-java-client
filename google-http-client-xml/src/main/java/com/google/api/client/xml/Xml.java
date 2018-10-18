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

import com.google.api.client.http.HttpMediaType;
import com.google.api.client.util.ArrayValueMap;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Charsets;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Types;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * {@link Beta} <br/>
 * XML utilities.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Beta
public class Xml {
  /**
   * {@code "application/xml; charset=utf-8"} media type used as a default for XML parsing.
   *
   * <p>
   * Use {@link HttpMediaType#equalsIgnoreParameters} for comparing media types.
   * </p>
   *
   * @since 1.10
   */
  public static final String MEDIA_TYPE =
      new HttpMediaType("application/xml").setCharsetParameter(Charsets.UTF_8).build();

  /** Text content. */
  static final String TEXT_CONTENT = "text()";

  /** XML pull parser factory. */
  private static XmlPullParserFactory factory;

  private static synchronized XmlPullParserFactory getParserFactory()
      throws XmlPullParserException {
    if (factory == null) {
      factory = XmlPullParserFactory.newInstance(
          System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
      factory.setNamespaceAware(true);
    }
    return factory;

  }

  /**
   * Returns a new XML serializer.
   *
   * @throws IllegalArgumentException if encountered an {@link XmlPullParserException}
   */
  public static XmlSerializer createSerializer() {
    try {
      return getParserFactory().newSerializer();
    } catch (XmlPullParserException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /** Returns a new XML pull parser. */
  public static XmlPullParser createParser() throws XmlPullParserException {
    return getParserFactory().newPullParser();
  }

  /**
   * Shows a debug string representation of an element data object of key/value pairs.
   * <p>
   * It will make up something for the element name and XML namespaces. If those are known, it is
   * better to use {@link XmlNamespaceDictionary#toStringOf(String, Object)}.
   *
   * @param element element data object of key/value pairs ({@link GenericXml}, {@link Map}, or any
   *        object with public fields)
   */
  public static String toStringOf(Object element) {
    return new XmlNamespaceDictionary().toStringOf(null, element);
  }

  /**
   * Parses the string value of an attribute value or text content.
   *
   * @param stringValue string value
   * @param field field to set or {@code null} if not applicable
   * @param valueType value type (class, parameterized type, or generic array type) or {@code null}
   *        for none
   * @param context context list, going from least specific to most specific type context, for
   *        example container class and its field
   * @param destination destination object or {@code null} for none
   * @param genericXml generic XML or {@code null} if not applicable
   * @param destinationMap destination map or {@code null} if not applicable
   * @param name key name
   */
  private static void parseAttributeOrTextContent(String stringValue,
                                                  Field field,
                                                  Type valueType,
                                                  List<Type> context,
                                                  Object destination,
                                                  GenericXml genericXml,
                                                  Map<String, Object> destinationMap,
                                                  String name) {
    if (field != null || genericXml != null || destinationMap != null) {
      valueType = field == null ? valueType : field.getGenericType();
      Object value = parseValue(valueType, context, stringValue);
      setValue(value, field, destination, genericXml, destinationMap, name);
    }
  }

  /**
   * Sets the value of a given field or map entry.
   *
   * @param value value
   * @param field field to set or {@code null} if not applicable
   * @param destination destination object or {@code null} for none
   * @param genericXml generic XML or {@code null} if not applicable
   * @param destinationMap destination map or {@code null} if not applicable
   * @param name key name
   */
  private static void setValue(Object value,
                               Field field,
                               Object destination,
                               GenericXml genericXml,
                               Map<String, Object> destinationMap,
                               String name) {
    if (field != null) {
      FieldInfo.setFieldValue(field, destination, value);
    } else if (genericXml != null) {
      genericXml.set(name, value);
    } else {
      destinationMap.put(name, value);
    }
  }

  /**
   * Customizes the behavior of XML parsing. Subclasses may override any methods they need to
   * customize behavior.
   *
   * <p>
   * Implementation has no fields and therefore thread-safe, but sub-classes are not necessarily
   * thread-safe.
   * </p>
   */
  public static class CustomizeParser {
    /**
     * Returns whether to stop parsing when reaching the start tag of an XML element before it has
     * been processed. Only called if the element is actually being processed. By default, returns
     * {@code false}, but subclasses may override.
     *
     * @param namespace XML element's namespace URI
     * @param localName XML element's local name
     */
    public boolean stopBeforeStartTag(String namespace, String localName) {
      return false;
    }

    /**
     * Returns whether to stop parsing when reaching the end tag of an XML element after it has been
     * processed. Only called if the element is actually being processed. By default, returns
     * {@code false}, but subclasses may override.
     *
     * @param namespace XML element's namespace URI
     * @param localName XML element's local name
     */
    public boolean stopAfterEndTag(String namespace, String localName) {
      return false;
    }
  }

  /**
   * Parses an XML element using the given XML pull parser into the given destination object.
   *
   * <p>
   * Requires the the current event be {@link XmlPullParser#START_TAG} (skipping any initial
   * {@link XmlPullParser#START_DOCUMENT}) of the element being parsed. At normal parsing
   * completion, the current event will either be {@link XmlPullParser#END_TAG} of the element being
   * parsed, or the {@link XmlPullParser#START_TAG} of the requested {@code atom:entry}.
   * </p>
   *
   * @param parser XML pull parser
   * @param destination optional destination object to parser into or {@code null} to ignore XML
   *        content
   * @param namespaceDictionary XML namespace dictionary to store unknown namespaces
   * @param customizeParser optional parser customizer or {@code null} for none
   */
  public static void parseElement(XmlPullParser parser, Object destination,
                                  XmlNamespaceDictionary namespaceDictionary,
                                  CustomizeParser customizeParser)
      throws IOException, XmlPullParserException {
    ArrayList<Type> context = new ArrayList<Type>();
    if (destination != null) {
      context.add(destination.getClass());
    }
    parseElementInternal(new ParserParameter(parser, context, destination, null, namespaceDictionary,
        customizeParser));
  }

  private static class ParserParameter{

    XmlPullParser parser;
    ArrayList<Type> context;
    Object destination;
    Type valueType;
    XmlNamespaceDictionary namespaceDictionary;
    CustomizeParser customizeParser;

    public ParserParameter(XmlPullParser parser,
                           ArrayList<Type> context,
                           Object destination,
                           Type valueType,
                           XmlNamespaceDictionary namespaceDictionary,
                           CustomizeParser customizeParser){


      this.parser = parser;
      this.context = context;
      this.destination = destination;
      this.valueType = valueType;
      this.namespaceDictionary = namespaceDictionary;
      this.customizeParser = customizeParser;
    }

  }

  /**
   * Returns whether the customize parser has requested to stop or reached end of document.
   * Otherwise, identical to
   * {@link #parseElement(XmlPullParser, Object, XmlNamespaceDictionary, CustomizeParser)}.
   *
   * @param parameter Parameter Object passsed to the Method
   * @return
   * @throws IOException
   * @throws XmlPullParserException
   */
  @SuppressWarnings("unchecked")
  private static boolean parseElementInternal(final ParserParameter parameter)
      throws IOException, XmlPullParserException {
    // TODO(yanivi): method is too long; needs to be broken down into smaller methods and comment
    // better

    // if the destination is a GenericXML then we are going the set the generic XML.
    GenericXml genericXml = parameter.destination instanceof GenericXml ? (GenericXml) parameter.destination : null;

    // if the destination is GenericXML and the destination is a Map, create a destination Map.
    Map<String, Object> destinationMap =
        genericXml == null && parameter.destination instanceof Map<?, ?> ? Map.class.cast(parameter.destination) : null;

    // if there is a class, we want to put the data into, create the class Info for this
    ClassInfo classInfo =
        destinationMap != null || parameter.destination == null ? null : ClassInfo.of(parameter.destination.getClass());

    // if we are the very beginning of the document, get the next element/event
    if (parameter.parser.getEventType() == XmlPullParser.START_DOCUMENT) {
      parameter.parser.next();
    }

    parseNamespacesForElement(parameter.parser, parameter.namespaceDictionary);

    // if we have a generic XML, set the namespace
    initForGenericXml(parameter.parser, parameter.namespaceDictionary, genericXml);

    // if we have a dedicated destination, parse the attributes into the object map
    parseAttributes(parameter, genericXml, destinationMap, classInfo);
    Field field;
    ArrayValueMap arrayValueMap = new ArrayValueMap(parameter.destination);

    // is stopped is required for the ATOM Parser, just in case the parsing
    //  isStopped during parsing at some time.
   // boolean isStopped = false;

    DoubleBooleanResult result = new DoubleBooleanResult(false, false);

    // TODO(yanivi): support Void type as "ignore" element/attribute
    main: while (true) {
      int event = parameter.parser.next();
      //boolean breakFromMain = false;
      switch (event) {
        // Never reached while Testing
        case XmlPullParser.END_DOCUMENT:
          result.isStopped = true;
          result.breakFromMain = true;
          break;
        case XmlPullParser.END_TAG:
          result.isStopped = parameter.customizeParser != null
              && parameter.customizeParser.stopAfterEndTag(parameter.parser.getNamespace(), parameter.parser.getName());
          result.breakFromMain = true;
          break;
        case XmlPullParser.TEXT:
          // parse text content
          if (parameter.destination != null) {
            field = classInfo == null ? null : classInfo.getField(TEXT_CONTENT);
            parseAttributeOrTextContent(parameter.parser.getText(),
                field,
                parameter.valueType,
                parameter.context,
                parameter.destination,
                genericXml,
                destinationMap,
                TEXT_CONTENT);
          }
          break;
        case XmlPullParser.START_TAG:
          if (parameter.customizeParser != null
              && parameter.customizeParser.stopBeforeStartTag(parameter.parser.getNamespace(), parameter.parser.getName())) {
            result.isStopped = true;
            result.breakFromMain = true;
            break;
          }
          // not sure how the case looks like, when this happens.
          if (parameter.destination == null) {
            // we ignore the result, as we can't map it to anything. we parse for sanity?
            parseTextContentForElement(parameter.parser, parameter.context, true, null);
          } else {
            // element
            parseNamespacesForElement(parameter.parser, parameter.namespaceDictionary);
            String namespace = parameter.parser.getNamespace();
            String alias = parameter.namespaceDictionary.getNamespaceAliasForUriErrorOnUnknown(namespace);

            //  get the "real" field name of the
            String fieldName = getFieldName(false, alias, namespace, parameter.parser.getName());

            // fetch the field from the classInfo
            field = classInfo == null ? null : classInfo.getField(fieldName);
            Type fieldType = field == null ? parameter.valueType : field.getGenericType();
            fieldType = Data.resolveWildcardTypeOrTypeVariable(parameter.context, fieldType);
            // field type is now class, parameterized type, or generic array type
            // resolve a parameterized type to a class
            Class<?> fieldClass = fieldType instanceof Class<?> ? (Class<?>) fieldType : null;
            if (fieldType instanceof ParameterizedType) {
              fieldClass = Types.getRawClass((ParameterizedType) fieldType);
            }
            boolean isArray = Types.isArray(fieldType);
            // text content
            boolean ignore = field == null && destinationMap == null && genericXml == null;
            // is the field an Enum
            boolean isEnum = fieldClass != null && fieldClass.isEnum();
            // Primitive or Enum
            if (ignore || Data.isPrimitive(fieldType) || isEnum) {
              result = mapCommonObject(parameter, genericXml, field, destinationMap, fieldName, ignore);
              // Handle as Map or Nested Class
            } else if (fieldType == null || fieldClass != null
                && Types.isAssignableToOrFrom(fieldClass, Map.class)) {
              result.isStopped = mapAsClassOrObjectType(parameter, destinationMap, field, fieldName, fieldType, fieldClass);
            } else if (isArray || Types.isAssignableToOrFrom(fieldClass, Collection.class)) {
              result.isStopped = mapAsArrayOrCollection(parameter, genericXml, destinationMap, field, arrayValueMap,
                  fieldName, fieldType, isArray);
            } else {
              result.isStopped = mapArrayWithClassType(parameter, genericXml, destinationMap, field, fieldName, fieldType,
                  fieldClass);
            }
          }

          // Never reached while Testing
          if(parameter.parser.getEventType() == XmlPullParser.END_DOCUMENT){
            result.isStopped = true;
          }

          // Never reached while Testing
          if (result.isStopped) {
            result.breakFromMain = true;
          }

          break; // break Switch;

        // Never reached while Testing
        default:
          throw new RuntimeException("Default in Main Switch");
      } // end -- switch (event)

      if (result.breakFromMain) {
        break;
      }

    } // end -- main: while (true)
    arrayValueMap.setValues();

    return result.isStopped;
  }

  private static class DoubleBooleanResult {

    boolean breakFromMain;
    boolean isStopped;

    public DoubleBooleanResult(final boolean breakFromMain,
  final boolean isStopped){
      this.breakFromMain = breakFromMain;
      this.isStopped = isStopped;
  }
  }

  private static DoubleBooleanResult mapCommonObject(ParserParameter parameter,
                                                     final GenericXml genericXml,
                                                     final Field field,
                                                     final Map<String, Object> destinationMap,
                                                     final String fieldName,
                                                     final boolean ignore) throws IOException, XmlPullParserException {
    boolean isStopped = false;
    boolean breakFromMainNext = false;
    int level = 1;
    while (level != 0) {
      switch (parameter.parser.next()) {
        // Never reached while Testing
        case XmlPullParser.END_DOCUMENT:
          isStopped = true;
          // This break is somehow hard to deal with; at least for now.
          breakFromMainNext = true;
        case XmlPullParser.START_TAG:
          level++;
          break;
        case XmlPullParser.END_TAG:
          level--;
          break;
        case XmlPullParser.TEXT:
          if (!ignore && level == 1) {
            parseAttributeOrTextContent(parameter.parser.getText(),
                field,
                parameter.valueType,
                parameter.context,
                parameter.destination,
                genericXml,
                destinationMap,
                fieldName);
          }
          break;
        // Never reached while Testing
        default:
          throw new RuntimeException("Default in Object Switch");

      } // switch
      if(breakFromMainNext){
        break;
      }
    }

   return new DoubleBooleanResult(breakFromMainNext, isStopped);


  }

  private static void parseAttributes(final ParserParameter parameter,
                                      final GenericXml genericXml,
                                      final Map<String, Object> destinationMap,
                                      final ClassInfo classInfo) {
    if (parameter.destination != null) {
      int attributeCount = parameter.parser.getAttributeCount();
      for (int i = 0; i < attributeCount; i++) {
        // TODO(yanivi): can have repeating attribute values, e.g. "@a=value1 @a=value2"?
        // You can't. Attribute names are unique per element. (?)
        String attributeName = parameter.parser.getAttributeName(i);
        String attributeNamespace = parameter.parser.getAttributeNamespace(i);
        String attributeAlias = attributeNamespace.length() == 0
            ? "" : parameter.namespaceDictionary.getNamespaceAliasForUriErrorOnUnknown(attributeNamespace);
        String fieldName = getFieldName(true, attributeAlias, attributeNamespace, attributeName);
        Field field = classInfo == null ? null : classInfo.getField(fieldName);
        parseAttributeOrTextContent(parameter.parser.getAttributeValue(i),
            field,
            parameter.valueType,
            parameter.context,
            parameter.destination,
            genericXml,
            destinationMap,
            fieldName);
      }
    }
  }

  private static boolean mapAsClassOrObjectType(final ParserParameter parameter,
                                                final Map<String, Object> destinationMap,
                                                final Field field,
                                                final String fieldName,
                                                final Type fieldType,
                                                final Class<?> fieldClass)
      throws IOException, XmlPullParserException {
    final boolean isStopped; // store the element as a map
    Map<String, Object> mapValue = Data.newMapInstance(fieldClass);
    int contextSize = parameter.context.size();
    if (fieldType != null) {
      parameter.context.add(fieldType);
    }
    Type subValueType = fieldType != null && Map.class.isAssignableFrom(fieldClass)
        ? Types.getMapValueParameter(fieldType) : null;
    subValueType = Data.resolveWildcardTypeOrTypeVariable(parameter.context, subValueType);
    isStopped = parseElementInternal(new ParserParameter(parameter.parser,
        parameter.context,
        mapValue, // destination; never null
        subValueType,
        parameter.namespaceDictionary,
        parameter.customizeParser));
    if (fieldType != null) {
      parameter.context.remove(contextSize);
    }
    if (destinationMap != null) {
      // map but not GenericXml: store as ArrayList of elements
      @SuppressWarnings("unchecked") Collection<Object> list = (Collection<Object>)
          destinationMap.get(fieldName);
      if (list == null) {
        list = new ArrayList<Object>(1);
        destinationMap.put(fieldName, list);
      }
      list.add(mapValue);
    } else if (field != null) {
      // not a map: store in field value
      FieldInfo fieldInfo = FieldInfo.of(field);
      if (fieldClass == Object.class) {
        // field is an Object: store as ArrayList of element maps
        @SuppressWarnings("unchecked")
        Collection<Object> list = (Collection<Object>) fieldInfo.getValue(parameter.destination);
        if (list == null) {
          list = new ArrayList<Object>(1);
          fieldInfo.setValue(parameter.destination, list);
        }
        list.add(mapValue);
      } else {
        // field is a Map: store as a single element map
        fieldInfo.setValue(parameter.destination, mapValue);
      }
    } else {
      // GenericXml: store as ArrayList of elements
      GenericXml atom = (GenericXml) parameter.destination;
      @SuppressWarnings("unchecked")
      Collection<Object> list = (Collection<Object>) atom.get(fieldName);
      if (list == null) {
        list = new ArrayList<Object>(1);
        atom.set(fieldName, list);
      }
      list.add(mapValue);
    }
    // Handle as Array
    return isStopped;
  }

  private static boolean mapAsArrayOrCollection(final ParserParameter parameter,
                                                final GenericXml genericXml,
                                                final Map<String, Object> destinationMap,
                                                final Field field,
                                                final ArrayValueMap arrayValueMap,
                                                final String fieldName,
                                                final Type fieldType, final boolean isArray)
      throws XmlPullParserException, IOException {
    // TODO(yanivi): some duplicate code here; isolate into reusable methods
    boolean isStopped = false;
    FieldInfo fieldInfo = FieldInfo.of(field);
    Object elementValue = null;
    Type subFieldType = isArray
        ? Types.getArrayComponentType(fieldType) : Types.getIterableParameter(fieldType);
    Class<?> rawArrayComponentType =
        Types.getRawArrayComponentType(parameter.context, subFieldType);
    subFieldType = Data.resolveWildcardTypeOrTypeVariable(parameter.context, subFieldType);
    Class<?> subFieldClass =
        subFieldType instanceof Class<?> ? (Class<?>) subFieldType : null;
    if (subFieldType instanceof ParameterizedType) {
      subFieldClass = Types.getRawClass((ParameterizedType) subFieldType);
    }
    boolean isSubEnum = subFieldClass != null && subFieldClass.isEnum();

    // Array mit Primitive oder Enum Type
    if (Data.isPrimitive(subFieldType) || isSubEnum) {
      // this could return null, but is not covered by a test!
      elementValue = parseTextContentForElement(parameter.parser, parameter.context, false, subFieldType);
    } else if (subFieldType == null || subFieldClass != null
        && Types.isAssignableToOrFrom(subFieldClass, Map.class)) {
      // returns never null
      elementValue = Data.newMapInstance(subFieldClass);
      int contextSize = parameter.context.size();
      if (subFieldType != null) {
        parameter.context.add(subFieldType);
      }
      Type subValueType = subFieldType != null
          && Map.class.isAssignableFrom(subFieldClass) ? Types.getMapValueParameter(
          subFieldType) : null;
      subValueType = Data.resolveWildcardTypeOrTypeVariable(parameter.context, subValueType);
      isStopped = parseElementInternal(new ParserParameter(parameter.parser,
          parameter.context,
          elementValue, // destination, never null!
          subValueType,
          parameter.namespaceDictionary,
          parameter.customizeParser));
      if (subFieldType != null) {
        parameter.context.remove(contextSize);
      }
    } else {
      // never null.
      elementValue = Types.newInstance(rawArrayComponentType);
      int contextSize = parameter.context.size();
      parameter.context.add(fieldType);
      // Not Yet Covered by Tests
      isStopped = parseElementInternal(new ParserParameter(parameter.parser,
          parameter.context,
          elementValue, // destination; never null.
          null,
          parameter.namespaceDictionary,
          parameter.customizeParser));
      parameter.context.remove(contextSize);
    }
    if (isArray) {
      // array field: add new element to array value map
      if (field == null) {
        arrayValueMap.put(fieldName, rawArrayComponentType, elementValue);
      } else {
        arrayValueMap.put(field, rawArrayComponentType, elementValue);
      }
    } else {
      mapToCollection(parameter.destination, genericXml, destinationMap, field, fieldName, fieldType,
          fieldInfo, elementValue);
    }
    return isStopped;
  }

  private static void mapToCollection(final Object destination,
                                      final GenericXml genericXml,
                                      final Map<String, Object> destinationMap,
                                      final Field field,
                                      final String fieldName,
                                      final Type fieldType,
                                      final FieldInfo fieldInfo,
                                      final Object elementValue) {
    // collection: add new element to collection  NOT YET Covered!
    @SuppressWarnings("unchecked") Collection<Object> collectionValue = (Collection<Object>)
        (field == null ? destinationMap.get(fieldName) : fieldInfo.getValue(destination));
    if (collectionValue == null) {
      collectionValue = Data.newCollectionInstance(fieldType);
      setValue(collectionValue,
          field,
          destination,
          genericXml,
          destinationMap,
          fieldName);
    }
    collectionValue.add(elementValue);
  }

  private static boolean mapArrayWithClassType(final ParserParameter parameter,
                                               final GenericXml genericXml,
                                               final Map<String, Object> destinationMap,
                                               final Field field, final String fieldName,
                                               final Type fieldType, final Class<?> fieldClass)
      throws IOException, XmlPullParserException {

    final boolean isStopped; // not an array/iterable or a map, but we do have a field
    Object value = Types.newInstance(fieldClass);
    int contextSize = parameter.context.size();
    parameter.context.add(fieldType);
    isStopped = parseElementInternal(new ParserParameter(parameter.parser,
        parameter.context,
        value, // destination; never null.
        null,
        parameter.namespaceDictionary,
        parameter.customizeParser));
    parameter.context.remove(contextSize);
    setValue(value, field, parameter.destination, genericXml, destinationMap, fieldName);
    return isStopped;
  }

  private static void initForGenericXml(final XmlPullParser parser,
                                        final XmlNamespaceDictionary namespaceDictionary,
                                        final GenericXml genericXml) {
    if(genericXml != null){
      genericXml.namespaceDictionary = namespaceDictionary;
      String name = parser.getName();
      String namespace = parser.getNamespace();
      String alias = namespaceDictionary.getNamespaceAliasForUriErrorOnUnknown(namespace);
      genericXml.name = alias.length() == 0 ? name : alias + ":" + name;
    }
  }

  private static String getFieldName(
      boolean isAttribute, String alias, String namespace, String name) {
    if (!isAttribute && alias.length() == 0) {
      return name;
    }
    StringBuilder buf = new StringBuilder(2 + alias.length() + name.length());
    if (isAttribute) {
      buf.append('@');
    }
    if (alias.length() != 0) {
      buf.append(alias).append(':');
    }
    return buf.append(name).toString();
  }

  private static Object parseTextContentForElement(
      XmlPullParser parser, List<Type> context, boolean ignoreTextContent, Type textContentType)
      throws XmlPullParserException, IOException {
    Object result = null;
    int level = 1;
    while (level != 0) {
      switch (parser.next()) {
        case XmlPullParser.END_DOCUMENT:
          level = 0;
          break;
        case XmlPullParser.START_TAG:
          level++;
          break;
        case XmlPullParser.END_TAG:
          level--;
          break;
        case XmlPullParser.TEXT:
          if (!ignoreTextContent && level == 1) {
            result = parseValue(textContentType, context, parser.getText());
          }
          break;
        default:
          break;
      }
    }
    return result;
  }

  private static Object parseValue(Type valueType, List<Type> context, String value) {
    valueType = Data.resolveWildcardTypeOrTypeVariable(context, valueType);
    if (valueType == Double.class || valueType == double.class) {
      if (value.equals("INF")) {
        return new Double(Double.POSITIVE_INFINITY);
      }
      if (value.equals("-INF")) {
        return new Double(Double.NEGATIVE_INFINITY);
      }
    }
    if (valueType == Float.class || valueType == float.class) {
      if (value.equals("INF")) {
        return Float.POSITIVE_INFINITY;
      }
      if (value.equals("-INF")) {
        return Float.NEGATIVE_INFINITY;
      }
    }
    return Data.parsePrimitiveValue(valueType, value);
  }

  /**
   * Parses the namespaces declared on the current element into the namespace dictionary.
   *
   * @param parser XML pull parser
   * @param namespaceDictionary namespace dictionary
   */
  private static void parseNamespacesForElement(
      XmlPullParser parser, XmlNamespaceDictionary namespaceDictionary)
      throws XmlPullParserException {
    int eventType = parser.getEventType();
    Preconditions.checkState(eventType == XmlPullParser.START_TAG,
        "expected start of XML element, but got something else (event type %s)", eventType);
    int depth = parser.getDepth();
    int nsStart = parser.getNamespaceCount(depth - 1);
    int nsEnd = parser.getNamespaceCount(depth);
    for (int i = nsStart; i < nsEnd; i++) {
      String namespace = parser.getNamespaceUri(i);
      // if namespace isn't already in our dictionary, add it now
      if (namespaceDictionary.getAliasForUri(namespace) == null) {
        String prefix = parser.getNamespacePrefix(i);
        String originalAlias = prefix == null ? "" : prefix;
        // find an available alias
        String alias = originalAlias;
        int suffix = 1;
        while (namespaceDictionary.getUriForAlias(alias) != null) {
          suffix++;
          alias = originalAlias + suffix;
        }
        namespaceDictionary.set(alias, namespace);
      }
    }
  }

  private Xml() {
  }
}
