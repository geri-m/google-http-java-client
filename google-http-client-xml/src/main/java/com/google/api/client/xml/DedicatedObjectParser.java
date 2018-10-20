package com.google.api.client.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import com.google.api.client.util.FieldInfo;

public class DedicatedObjectParser extends Xml {

  public DedicatedObjectParser(){
    super();
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
   */

  public static void parseAttributeOrTextContentDerived(String stringValue,
                                                  Field field,
                                                  Type valueType,
                                                  List<Type> context,
                                                  Object destination) {
    // TODO: Figure out, when Field could be null.
    if (field != null && destination != null) {
      Object value = parseValue(valueType, context, stringValue);
      setValue(field, destination, value);
    }
  }

  public static void setValue(Field field,
                              Object destination,
                              Object value){
    FieldInfo.setFieldValue(field, destination, value);
  }

}
