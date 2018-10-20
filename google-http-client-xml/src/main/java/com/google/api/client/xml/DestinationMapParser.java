package com.google.api.client.xml;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class DestinationMapParser extends Xml {

  public DestinationMapParser(){
    super();
  }


  /**
   * Parses the string value of an attribute value or text content.
   *
   * @param stringValue string value
   * @param valueType value type (class, parameterized type, or generic array type) or {@code null}
   *        for none
   * @param context context list, going from least specific to most specific type context, for
   *        example container class and its field
   * @param destinationMap destination map or {@code null} if not applicable
   * @param name key name
   */

  public static void parseAttributeOrTextContent(String stringValue,
                                                 Type valueType,
                                                 List<Type> context,
                                                 Map<String, Object> destinationMap,
                                                 String name) {
    if (destinationMap != null && name != null) {
      Object value = parseValue(valueType, context, stringValue);
      setValue(destinationMap, name, value);
    }
  }

  public static void setValue(Map<String, Object> destinationMap,String name,  Object value){
    destinationMap.put(name, value);
  }


}
