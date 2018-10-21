package com.google.api.client.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import com.google.api.client.util.ClassInfo;

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

  public static void parseAttributesFromElement(final ParserParameter parameter, final ClassInfo classInfo, final GenericXml genericXml) {
    if (parameter.destination != null) {
      int attributeCount = parameter.parser.getAttributeCount();
      for (int i = 0; i < attributeCount; i++) {
        // TODO(yanivi): can have repeating attribute values, e.g. "@a=value1 @a=value2"?
        // You can't. Attribute names are unique per element. (?)
        String attributeName = parameter.parser.getAttributeName(i);
        String attributeNamespace = parameter.parser.getAttributeNamespace(i);
        String attributeAlias = attributeNamespace.length() == 0
            ? "" : parameter.namespaceDictionary.getNamespaceAliasForUriErrorOnUnknown(attributeNamespace);
        String fieldName = getFieldName(true, attributeAlias, attributeName);
        Field field = classInfo == null ? null : classInfo.getField(fieldName);
        if(field != null)
          throw new RuntimeException("parseAttributesFromElement (GenericXML) sanity check");

        parameter.valueType = field == null ? parameter.valueType : field.getGenericType();

        parseAttributeOrTextContent(parameter.parser.getAttributeValue(i),
              parameter.valueType,
              parameter.context,
              genericXml, fieldName);

      }
    }
  }

}
