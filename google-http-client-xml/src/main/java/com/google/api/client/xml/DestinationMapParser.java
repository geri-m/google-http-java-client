package com.google.api.client.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class DestinationMapParser extends Xml {

  public DestinationMapParser(){
    super();
  }

  public static void parseAttributeOrTextContentDerived(String stringValue,
                                                  Field field,
                                                  Type valueType,
                                                  List<Type> context,
                                                  Map<String, Object> destinationMap,
                                                  String name) {
    if (destinationMap != null && name != null) {
      valueType = field == null ? valueType : field.getGenericType();
      Object value = parseValue(valueType, context, stringValue);
      destinationMap.put(name, value);
    }
  }


}
