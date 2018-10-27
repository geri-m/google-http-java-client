package com.google.api.client.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.google.api.client.util.ClassInfo;

public class MapParser extends Xml<Map<String,Object>> {

  protected Map<String, Object> destinationMap;

  @SuppressWarnings("unchecked")
  public MapParser(final ParserParameter parameter) {
    super(parameter);
    this.destinationMap = (Map) parameter.destination;
  }


  @Override
  public void setDestination(final Map<String, Object> destinationMap) {
    throw new RuntimeException("Destination Map Must not be assigned after Constructor");
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
  public void parseAttributeOrTextContent(String stringValue,  Object name) {
    if (destinationMap != null && name != null) {
      Object value = parseValue(parameter.valueType, parameter.context, stringValue);
      setValue(name, value);
    }
  }

  /**
   * Sets the value of a given field or map entry.
   *
   * @param value          value
   //* @param destinationMap destination map or {@code null} if not applicable
   * @param name           key name
   */

  @Override
  public void setValue(Object name, Object value) {
    destinationMap.put((String)name, value);
  }

  @Override
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
        parseAttributeOrTextContent(parameter.parser.getAttributeValue(i),  fieldName);
      }
    }
  }

  @Override
  public void mapCollection(final Class<?> fieldClass, final String fieldName, final Map<String, Object> mapValue) {
    // map but not GenericXml: store as ArrayList of elements
    @SuppressWarnings("unchecked") Collection<Object> list = (Collection<Object>)
        destinationMap.get(fieldName);
    if (list == null) {
      list = new ArrayList<Object>(1);
      destinationMap.put(fieldName, list);
    }
    list.add(mapValue);


  }


  @Override
  public void mapArrayWithClassTypeSetValue(final Object fieldName, final Object value) {
    setValue(fieldName, value);
  }


}
