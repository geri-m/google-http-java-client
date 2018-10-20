package com.google.api.client.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import com.google.api.client.util.FieldInfo;

public class DedicatedObjectParser extends Xml {

  public DedicatedObjectParser(){
    super();
  }

  public static void parseAttributeOrTextContentDerived(String stringValue,
                                                  Field field,
                                                  Type valueType,
                                                  List<Type> context,
                                                  Object destination) {
    if (field != null && destination != null) {
      valueType = field == null ? valueType : field.getGenericType();
      Object value = parseValue(valueType, context, stringValue);
      FieldInfo.setFieldValue(field, destination, value);
    }
  }
}
