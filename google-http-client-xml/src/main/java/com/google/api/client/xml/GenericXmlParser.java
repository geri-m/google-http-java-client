package com.google.api.client.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import com.google.api.client.util.ClassInfo;

public class GenericXmlParser extends Xml<GenericXml> {

  protected GenericXml genericXml;

  public GenericXmlParser(final ParserParameter parameter, final ClassInfo classInfo) {
    super(parameter, classInfo);
    this.genericXml = (GenericXml) parameter.destination;
    initForGenericXml();
  }

  @Override
  public void setDestination(final GenericXml genericXml) {
    throw new RuntimeException("GenericXml Must not be assigned after Constructor");
  }

  /**
   * Parses the string value of an attribute value or text content.
   *
   * @param stringValue string value
  // * @param valueType value type (class, parameterized type, or generic array type) or {@code null}
   *        for none
  // * @param context context list, going from least specific to most specific type context, for
   *        example container class and its field
   * @param name key name
   */
  @Override
  public  void parseAttributeOrTextContent(String stringValue,
                                                 Object name) {
    if (genericXml != null && name != null) {
      Object value = parseValue(parameter.valueType, parameter.context, stringValue);
      setValue(name, value);

    }
  }
  /**
   * Sets the value of a given field or map entry.
   *
   * @param value value
   // * @param genericXml generic XML or {@code null} if not applicable
   * @param name key name
   */

  @Override
  public  void setValue(Object name, Object value){
    genericXml.set((String)name, value);
  }


  private  void initForGenericXml() {
    genericXml.namespaceDictionary = parameter.namespaceDictionary;
    String name =  parameter.parser.getName();
    String namespace =  parameter.parser.getNamespace();
    String alias =  parameter.namespaceDictionary.getNamespaceAliasForUriErrorOnUnknown(namespace);
    genericXml.name = alias.length() == 0 ? name : alias + ":" + name;
  }

  public  void parseAttributesFromElement() {
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
                fieldName);

      }
    }
  }

  @Override
  public void mapCollection(final Class<?> fieldClass, final String fieldName, final Map<String, Object> mapValue) {
    // GenericXml: store as ArrayList of elements
    @SuppressWarnings("unchecked") Collection<Object> list = (Collection<Object>) genericXml.get(fieldName);
    if (list == null) {
      list = new ArrayList<Object>(1);
      genericXml.set(fieldName, list);
    }
    list.add(mapValue);
  }


  @Override
  public void mapArrayWithClassTypeSetValue(final Object fieldName, final Object value){
    setValue(fieldName, value);
  }

}
