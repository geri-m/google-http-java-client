package com.google.api.client.xml;


import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

public class GenericXmlParser extends Xml {


  public GenericXmlParser(){
    super();
  }

  public static void parseAttributeOrTextContentDerived(String stringValue,
                                                  Field field,
                                                  Type valueType,
                                                  List<Type> context,
                                                  GenericXml genericXml,
                                                  String name) {
    if (genericXml != null && name != null) {
      valueType = field == null ? valueType : field.getGenericType();
      Object value = parseValue(valueType, context, stringValue);
      genericXml.set(name, value);
    }
  }


}
