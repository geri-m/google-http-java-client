package com.google.api.client.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import com.google.api.client.util.ClassInfo;

public class MapParser extends Xml {

  protected final Map<String, Object> destinationMap;

  public MapParser(final ParserParameter parameter, final Map<String, Object> destinationMap, final ClassInfo classInfo) {
    super(parameter, classInfo);
    this.destinationMap = destinationMap;
  }


  /**
   * Parses the string value of an attribute value or text content.
   *
   * @param stringValue    string value
  // * @param valueType      value type (class, parameterized type, or generic array type) or {@code null} for none
  // * @param context        context list, going from least specific to most specific type context, for example container class and its field
  // * @param destinationMap destination map or {@code null} if not applicable
   * @param name           key name
   */

  @Override
  public void parseAttributeOrTextContent(String stringValue,  Field field, Object name) {
    if (destinationMap != null && name != null) {
      Object value = parseValue(parameter.valueType, parameter.context, stringValue);
      setValue(destinationMap, (String)name, value);
    }
  }

  /**
   * Sets the value of a given field or map entry.
   *
   * @param value          value
   * @param destinationMap destination map or {@code null} if not applicable
   * @param name           key name
   */

  public static void setValue(Map<String, Object> destinationMap, String name, Object value) {
    destinationMap.put(name, value);
  }

  public  void parseAttributesFromElement() {
    if (parameter.destination != null) {
      int attributeCount = parameter.parser.getAttributeCount();
      for (int i = 0; i < attributeCount; i++) {
        // TODO(yanivi): can have repeating attribute values, e.g. "@a=value1 @a=value2"?
        // You can't. Attribute names are unique per element. (?)
        String attributeName = parameter.parser.getAttributeName(i);
        String attributeNamespace = parameter.parser.getAttributeNamespace(i);
        String attributeAlias = attributeNamespace.length() == 0 ? "" : parameter.namespaceDictionary.getNamespaceAliasForUriErrorOnUnknown(attributeNamespace);
        String fieldName = getFieldName(true, attributeAlias, attributeName);
        Field field = classInfo == null ? null : classInfo.getField(fieldName);
        if(field != null)
          throw new RuntimeException("parseAttributesFromElement (Destination Map) sanity check");
        parameter.valueType = field == null ? parameter.valueType : field.getGenericType();
        parseAttributeOrTextContent(parameter.parser.getAttributeValue(i),  null, fieldName);
      }
    }
  }
}