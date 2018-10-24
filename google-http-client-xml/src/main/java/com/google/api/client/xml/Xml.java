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
public abstract class Xml<T> {


  protected final ParserParameter parameter;
  protected final ClassInfo classInfo;

  protected Xml(final ParserParameter parameter, final ClassInfo classInfo) {
    this.parameter = parameter;
    this.classInfo = classInfo;
  }

  public abstract void parseAttributesFromElement();

  public abstract void parseAttributeOrTextContent(String stringValue, Object name);

  public abstract void setDestination(T destination);

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

  public static class ParserParameter{

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

  private static boolean parseElementInternal(final ParserParameter parameter)
      throws IOException, XmlPullParserException {
    // TODO(yanivi): method is too long; needs to be broken down into smaller methods and comment
    // better

    // if the destination is a GenericXML then we are going the set the generic XML.
    GenericXml genericXml = parameter.destination instanceof GenericXml ? (GenericXml) parameter.destination : null;

    // if the destination is GenericXML and the destination is a Map, create a destination Map.
    @SuppressWarnings("unchecked")
    Map<String, Object> destinationMap =
        genericXml == null && parameter.destination instanceof Map<?, ?> ? Map.class.cast(parameter.destination) : null;

    // if there is a class, we want to put the data into, create the class Info for this
    ClassInfo classInfo =
        destinationMap != null || parameter.destination == null ? null : ClassInfo.of(parameter.destination.getClass());


    if(classInfo != null){
      /*
      if(genericXml != null)
        throw new RuntimeException("Generic XML Must be null!");
      */
      if(destinationMap != null)
        throw new RuntimeException("destinationMap XML Must be null!");
    }


    if(genericXml != null){
      /*
      if(classInfo != null)
        throw new RuntimeException("classInfo Musst be null!");
      */
      if(destinationMap != null)
        throw new RuntimeException("destinationMap XML Musst be null!");
    }


    if(destinationMap != null){
      if(classInfo != null)
        throw new RuntimeException("classInfo Musst be null!");

      if(genericXml != null)
        throw new RuntimeException("genericXml XML Musst be null!");
    }



    // if we are the very beginning of the document, get the next element/event
    if (parameter.parser.getEventType() == XmlPullParser.START_DOCUMENT) {
      parameter.parser.next();
    }

    // parse the namespace for the current Element.
    parseNamespacesForElement(parameter.parser, parameter.namespaceDictionary);

    final Xml parser;
    // if we have a dedicated destination
    if (!(parameter.destination instanceof GenericXml) &&  parameter.destination instanceof Map<?, ?>) {
      parser = new MapParser(parameter,  destinationMap, classInfo);
    } else if (parameter.destination instanceof GenericXml) {
      // if we have a generic XML, set the namespace
      parser = new GenericXmlParser(parameter, genericXml,  classInfo);
    } else {
      parser = new DedicatedObjectParser(parameter, classInfo);
    }

    parser.parseAttributesFromElement();

    Field field;
    ArrayValueMap arrayValueMap = new ArrayValueMap(parameter.destination);

    // is stopped is required for the ATOM Parser, just in case the parsing
    //  isStopped during parsing at some time.
    boolean isStopped = false;
    // TODO(yanivi): support Void type as "ignore" element/attribute
    main: while (true) {
      int event = parameter.parser.next();
      boolean breakFromMain = false;
      switch (event) {
        // Never reached while Testing
        case XmlPullParser.END_DOCUMENT:
          isStopped = true;
          breakFromMain = true;
          break;
        case XmlPullParser.END_TAG:
          isStopped = parameter.customizeParser != null
              && parameter.customizeParser.stopAfterEndTag(parameter.parser.getNamespace(), parameter.parser.getName());
          breakFromMain = true;
          break;
        case XmlPullParser.TEXT:
          // parse text content
          if (parameter.destination != null) {
            field = classInfo == null ? null : classInfo.getField(TEXT_CONTENT);

            if ((parser instanceof DedicatedObjectParser)) {
              parser.setDestination(field);
            }


            sanityCheck(parser, genericXml, destinationMap, field);
            parameter.valueType = field == null ? parameter.valueType : field.getGenericType();

            mapTextToElementValue(parameter, parser, field, TEXT_CONTENT);
          }
          break;
        case XmlPullParser.START_TAG:
          if (parameter.customizeParser != null
              && parameter.customizeParser.stopBeforeStartTag(parameter.parser.getNamespace(), parameter.parser.getName())) {
            isStopped = true;
            breakFromMain = true;
            break;
          }
          // not sure how the case looks like, when this happens.
          if (parameter.destination == null) {
            // we ignore the result, as we can't map it to anything. we parse for sanity
            parseTextContentForElement(parameter.parser, parameter.context, true, null);
          } else {
            // element
            parseNamespacesForElement(parameter.parser, parameter.namespaceDictionary);
            String namespace = parameter.parser.getNamespace();
            String alias = parameter.namespaceDictionary.getNamespaceAliasForUriErrorOnUnknown(namespace);

            //  get the "real" field name of the
            String fieldName = getFieldName(false, alias,  parameter.parser.getName());

            // fetch the field from the classInfo
            field = classInfo == null ? null : classInfo.getField(fieldName);

            if((parser instanceof DedicatedObjectParser)){
              parser.setDestination(field);
            }

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
              int level = 1;
              while (level != 0) {
                switch (parameter.parser.next()) {
                  // Never reached while Testing
                  case XmlPullParser.END_DOCUMENT:
                    isStopped = true;
                    // This break is somehow hard to deal with; at least for now.
                    break main;
                  case XmlPullParser.START_TAG:
                    level++;
                    break;
                  case XmlPullParser.END_TAG:
                    level--;
                    break;
                  case XmlPullParser.TEXT:
                    if (!ignore && level == 1) {
                      parameter.valueType = field == null ? parameter.valueType : field.getGenericType();
                      sanityCheck(parser, genericXml, destinationMap, field);
                      if(field == null){
                        throw new RuntimeException("Field can not be null here");
                      }

                      parser.parseAttributeOrTextContent(parameter.parser.getText(),  parameter.destination);
                    }
                    break;
                  // Never reached while Testing
                  default:
                    throw new RuntimeException("Default in Object Switch");
                }
              }
              // Handle as Map or Nested Class
            } else if (fieldType == null || fieldClass != null
                && Types.isAssignableToOrFrom(fieldClass, Map.class)) {
              isStopped = mapAsClassOrObjectType(parameter.parser, parameter.context, parameter.destination, parameter.namespaceDictionary,
                  parameter.customizeParser, destinationMap, field, fieldName, fieldType, fieldClass);
            } else if (isArray || Types.isAssignableToOrFrom(fieldClass, Collection.class)) {
              isStopped = mapAsArrayOrCollection(parameter.parser, parameter.context, parameter.destination, parameter.namespaceDictionary,
                  parameter.customizeParser, genericXml, destinationMap, field, arrayValueMap,
                  fieldName, fieldType, isArray, parser);
            } else {
              isStopped = mapArrayWithClassType(parameter.parser, parameter.context, parameter.destination, parameter.namespaceDictionary,
                  parameter.customizeParser, genericXml, destinationMap, field, fieldName, fieldType,
                  fieldClass);
            }
          }

          // Never reached while Testing
          if(parameter.parser.getEventType() == XmlPullParser.END_DOCUMENT){
            isStopped = true;
          }

          // Never reached while Testing
          if (isStopped) {
            breakFromMain = true;
          }

          break; // break Switch;

        // Never reached while Testing
        default:
          throw new RuntimeException("Default in Main Switch");
      } // end -- switch (event)

      if (breakFromMain) {
        break;
      }

    } // end -- main: while (true)
    arrayValueMap.setValues();

    return isStopped;
  }

  private static void sanityCheck(final Xml parser, final GenericXml genericXml, final Map<String, Object> destinationMap, final Field field) {
    if (field != null) {

      if (!(parser instanceof DedicatedObjectParser))
        throw new RuntimeException("Incorrect Parser");

      if (genericXml != null)
        throw new RuntimeException("Field Must  be null!");

      if (destinationMap != null)
        throw new RuntimeException("destinationMap XML Must be null!");
    }

    if (genericXml != null) {
      if (!(parser instanceof GenericXmlParser))
        throw new RuntimeException("Incorrect Parser");

      if (field != null)
        throw new RuntimeException("Field Must  be null!");

      if (destinationMap != null)
        throw new RuntimeException("destinationMap XML Must be null!");
    }


    if (destinationMap != null) {

      if (!(parser instanceof MapParser))
        throw new RuntimeException("Incorrect Parser");

      if (field != null)
        throw new RuntimeException("field Must be null!");

      if (genericXml != null)
        throw new RuntimeException("genericXml XML Must be null!");
    }
  }

  private static void mapTextToElementValue(final ParserParameter parameter, final Xml parser, final Field field, final String textContent) {
    if (field != null) {
      if(!(parser instanceof DedicatedObjectParser)){
        throw new RuntimeException("DedicatedObjectParser required");
      }
      parser.parseAttributeOrTextContent(parameter.parser.getText(),  parameter.destination);
    } else {
      parser.parseAttributeOrTextContent(parameter.parser.getText(),  textContent);
    }
  }


  private static boolean mapAsClassOrObjectType(final XmlPullParser parser,
                                                final ArrayList<Type> context,
                                                final Object destination,
                                                final XmlNamespaceDictionary namespaceDictionary,
                                                final CustomizeParser customizeParser,
                                                final Map<String, Object> destinationMap,
                                                final Field field, final String fieldName,
                                                final Type fieldType, final Class<?> fieldClass)
      throws IOException, XmlPullParserException {
    final boolean isStopped; // store the element as a map
    Map<String, Object> mapValue = Data.newMapInstance(fieldClass);
    int contextSize = context.size();
    if (fieldType != null) {
      context.add(fieldType);
    }
    Type subValueType = fieldType != null && Map.class.isAssignableFrom(fieldClass)
        ? Types.getMapValueParameter(fieldType) : null;
    subValueType = Data.resolveWildcardTypeOrTypeVariable(context, subValueType);
    isStopped = parseElementInternal(new ParserParameter(parser,
        context,
        mapValue, // destination; never null
        subValueType,
        namespaceDictionary,
        customizeParser));
    if (fieldType != null) {
      context.remove(contextSize);
    }

    // Now do the Mapping here.
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
        Collection<Object> list = (Collection<Object>) fieldInfo.getValue(destination);
        if (list == null) {
          list = new ArrayList<Object>(1);
          fieldInfo.setValue(destination, list);
        }
        list.add(mapValue);
      } else {
        // field is a Map: store as a single element map
        fieldInfo.setValue(destination, mapValue);
      }
    } else {

      if(!(destination instanceof  GenericXml)){
        throw new RuntimeException("Desintation is not a Gernic XML");
      }

      // GenericXml: store as ArrayList of elements
      GenericXml atom = (GenericXml) destination;
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

  private static boolean mapAsArrayOrCollection(final XmlPullParser parser,
                                                final ArrayList<Type> context,
                                                final Object destination,
                                                final XmlNamespaceDictionary namespaceDictionary,
                                                final CustomizeParser customizeParser,
                                                final GenericXml genericXml,
                                                final Map<String, Object> destinationMap,
                                                final Field field,
                                                final ArrayValueMap arrayValueMap,
                                                final String fieldName,
                                                final Type fieldType, final boolean isArray, final Xml parserObj)
      throws XmlPullParserException, IOException {
    boolean isStopped = false;
    // TODO(yanivi): some duplicate code here; isolate into reusable methods
    FieldInfo fieldInfo = FieldInfo.of(field);
    Object elementValue = null;
    Type subFieldType = isArray
        ? Types.getArrayComponentType(fieldType) : Types.getIterableParameter(fieldType);
    Class<?> rawArrayComponentType =
        Types.getRawArrayComponentType(context, subFieldType);
    subFieldType = Data.resolveWildcardTypeOrTypeVariable(context, subFieldType);
    Class<?> subFieldClass =
        subFieldType instanceof Class<?> ? (Class<?>) subFieldType : null;
    if (subFieldType instanceof ParameterizedType) {
      subFieldClass = Types.getRawClass((ParameterizedType) subFieldType);
    }
    boolean isSubEnum = subFieldClass != null && subFieldClass.isEnum();

    // Array mit Primitive oder Enum Type
    if (Data.isPrimitive(subFieldType) || isSubEnum) {
      // can be null
      elementValue = parseTextContentForElement(parser, context, false, subFieldType);
    } else if (subFieldType == null || subFieldClass != null
        && Types.isAssignableToOrFrom(subFieldClass, Map.class)) {
      // returns never null
      elementValue = Data.newMapInstance(subFieldClass);
      int contextSize = context.size();
      if (subFieldType != null) {
        context.add(subFieldType);
      }
      Type subValueType = subFieldType != null
          && Map.class.isAssignableFrom(subFieldClass) ? Types.getMapValueParameter(
          subFieldType) : null;
      subValueType = Data.resolveWildcardTypeOrTypeVariable(context, subValueType);
      isStopped = parseElementInternal(new ParserParameter(parser,
          context,
          elementValue, // destination, never null!
          subValueType,
          namespaceDictionary,
          customizeParser));
      if (subFieldType != null) {
        context.remove(contextSize);
      }
    } else {
      // never null.
      elementValue = Types.newInstance(rawArrayComponentType);
      int contextSize = context.size();
      context.add(fieldType);
      // Not Yet Covered by Tests
      isStopped = parseElementInternal(new ParserParameter(parser,
          context,
          elementValue, // destination; never null.
          null,
          namespaceDictionary,
          customizeParser));
      context.remove(contextSize);
    }
    if (isArray) {
      // array field: add new element to array value map
      if (field == null) {
        arrayValueMap.put(fieldName, rawArrayComponentType, elementValue);
      } else {
        arrayValueMap.put(field, rawArrayComponentType, elementValue);
      }
    } else {
      mapToCollection(destination, genericXml, destinationMap, field, fieldName, fieldType,
          fieldInfo, elementValue, parserObj);
    }
    return isStopped;
  }

  private static void mapToCollection(final Object destination, final GenericXml genericXml,
                                      final Map<String, Object> destinationMap, final Field field,
                                      final String fieldName, final Type fieldType,
                                      final FieldInfo fieldInfo, final Object elementValue, final Xml parser) {
    // collection: add new element to collection  NOT YET Covered!
    @SuppressWarnings("unchecked")
    Collection<Object> collectionValue = (Collection<Object>)
        (field == null ? destinationMap.get(fieldName) : fieldInfo.getValue(destination));
    if (collectionValue == null) {
      collectionValue = Data.newCollectionInstance(fieldType);

      if(destinationMap != null){
        throw new RuntimeException(" This should not happen, as Array != destinationMap. Remove if problem");
      }

      if (field != null) {
        DedicatedObjectParser.setValue(field, destination, collectionValue);
      } else if (genericXml != null) {
        GenericXmlParser.setValue(genericXml, fieldName, collectionValue);
      } else {
        MapParser.setValue(destinationMap, fieldName, collectionValue);
      }

    }
    collectionValue.add(elementValue);
  }

  private static boolean mapArrayWithClassType(final XmlPullParser parser,
                                               final ArrayList<Type> context,
                                               final Object destination,
                                               final XmlNamespaceDictionary namespaceDictionary,
                                               final CustomizeParser customizeParser,
                                               final GenericXml genericXml,
                                               final Map<String, Object> destinationMap,
                                               final Field field, final String fieldName,
                                               final Type fieldType, final Class<?> fieldClass)
      throws IOException, XmlPullParserException {

    final boolean isStopped; // not an array/iterable or a map, but we do have a field
    Object value = Types.newInstance(fieldClass);
    int contextSize = context.size();
    context.add(fieldType);
    isStopped = parseElementInternal(new ParserParameter(parser,
        context,
        value, // destination; never null.
        null,
        namespaceDictionary,
        customizeParser));
    context.remove(contextSize);

    if(genericXml != null){
      throw new RuntimeException(" This should not happen, as Array != Generic. Remove if problem");
    }

    if(destinationMap != null){
      throw new RuntimeException(" This should not happen, as Array != destinationMap. Remove if problem");
    }

    if (field != null) {
      DedicatedObjectParser.setValue(field, destination, value);
    } else if (genericXml != null) {
      GenericXmlParser.setValue(genericXml, fieldName, value);
    } else {
      MapParser.setValue(destinationMap, fieldName, value);
    }


    return isStopped;
  }

  // -----------------------------------------------------------------

  protected static String getFieldName(
      boolean isAttribute, String alias, String name) {
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

  // independent to Type; Derived Objects make use of this method.
  protected static Object parseValue(Type valueType, List<Type> context, String value) {
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

}
