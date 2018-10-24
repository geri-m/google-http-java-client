package com.google.api.client.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;

public class DedicatedObjectParser extends Xml<Field> {

  private Field field;

  public DedicatedObjectParser(final ParserParameter parameter, final Field field, final ClassInfo classInfo) {
    this(parameter, classInfo);
    this.field = field;
  }


  public DedicatedObjectParser(final ParserParameter parameter, final ClassInfo classInfo) {
    super(parameter, classInfo);
  }

  @Override
  public void setDestination(final Field field) {
    this.field = field;
  }


  /**
   * Parses the string value of an attribute value or text content.
   *  @param stringValue string value
   //* @param context     context list, going from least specific to most specific type context, for example container class and its field
   //* @param valueType   value type (class, parameterized type, or generic array type) or {@code null} for none
   * @param field       field to set or {@code null} if not applicable
   * @param destination destination object or {@code null} for none
   */

  @Override
  public void parseAttributeOrTextContent(String stringValue, Field field, Object destination) {
    // TODO: Figure out, when Field could be null.
    if (field != null && destination != null) {
      Object value = parseValue(parameter.valueType, parameter.context, stringValue);
      setValue(field, destination, value);
    }
  }

  public void parseAttributeOrTextContent(String stringValue, Object destination) {
    parseAttributeOrTextContent(stringValue, this.field, destination);
  }

  /**
   * Sets the value of a given field or map entry.
   *
   * @param value       value
   * @param field       field to set or {@code null} if not applicable
   * @param destination destination object or {@code null} for none
   */


  public static void setValue(Field field, Object destination, Object value) {
    FieldInfo.setFieldValue(field, destination, value);
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
        String fieldName = getFieldName(true, attributeAlias,  attributeName);
        Field field = classInfo == null ? null : classInfo.getField(fieldName);
        parameter.valueType = field == null ? parameter.valueType : field.getGenericType();
        parseAttributeOrTextContent(parameter.parser.getAttributeValue(i), field, parameter.destination);
      }
    }
  }
}
