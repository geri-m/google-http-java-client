package com.google.api.client.xml;

import java.lang.reflect.Type;
import java.util.List;

public class GenericXmlParser extends Xml {


  public GenericXmlParser(){
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
   * @param name key name
   */
  public static void parseAttributeOrTextContentDerived(String stringValue,
                                                  Type valueType,
                                                  List<Type> context,
                                                  GenericXml genericXml,
                                                  String name) {
    if (genericXml != null && name != null) {
      Object value = parseValue(valueType, context, stringValue);
      setValue(genericXml, name, value);

    }
  }

  public static void setValue(GenericXml genericXml, String name, Object value){
    genericXml.set(name, value);
  }


}
