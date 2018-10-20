package com.google.api.client.xml;

import java.lang.reflect.Type;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

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
  public static void parseAttributeOrTextContent(String stringValue,
                                                 Type valueType,
                                                 List<Type> context,
                                                 GenericXml genericXml,
                                                 String name) {
    if (genericXml != null && name != null) {
      Object value = parseValue(valueType, context, stringValue);
      setValue(genericXml, name, value);

    }
  }
  /**
   * Sets the value of a given field or map entry.
   *
   * @param value value
   * @param genericXml generic XML or {@code null} if not applicable
   * @param name key name
   */

  public static void setValue(GenericXml genericXml, String name, Object value){
    genericXml.set(name, value);
  }


  public static void initForGenericXml(final XmlPullParser parser,
                                        final XmlNamespaceDictionary namespaceDictionary,
                                        final GenericXml genericXml) {
    genericXml.namespaceDictionary = namespaceDictionary;
    String name = parser.getName();
    String namespace = parser.getNamespace();
    String alias = namespaceDictionary.getNamespaceAliasForUriErrorOnUnknown(namespace);
    genericXml.name = alias.length() == 0 ? name : alias + ":" + name;
  }


}
