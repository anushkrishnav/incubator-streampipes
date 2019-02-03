/*
Copyright 2019 FZI Forschungszentrum Informatik

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.streampipes.model.runtime;

import org.streampipes.model.constants.PropertySelectorConstants;
import org.streampipes.model.runtime.field.AbstractField;
import org.streampipes.model.runtime.field.PrimitiveField;
import org.streampipes.model.schema.EventSchema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event {

  private SourceInfo sourceInfo;
  private SchemaInfo schemaInfo;
  private Map<String, AbstractField> fieldMap;

  public Event(Map<String, AbstractField> fieldMap, SourceInfo
          sourceInfo, SchemaInfo schemaInfo) {
    this.fieldMap = fieldMap;
    this.sourceInfo = sourceInfo;
    this.schemaInfo = schemaInfo;
  }

  public Event() {
    this.fieldMap = new HashMap<>();
  }

  public Map<String, AbstractField> getFields() {
    return fieldMap;
  }

  public Map<String, Object> getRaw() {
    return new EventConverter(this).toMap(false);
  }

  public SourceInfo getSourceInfo() {
    return sourceInfo;
  }

  public SchemaInfo getSchemaInfo() {
    return schemaInfo;
  }

  public Event merge(Event otherEvent, EventSchema outputSchema) {
    return EventFactory.fromEvents(this, otherEvent, outputSchema);
  }

  public AbstractField getFieldByRuntimeName(String runtimeName) {
    // TODO this currently only works for first-level properties
    return fieldMap
            .entrySet()
            .stream()
            .map(Map.Entry::getValue)
            .filter(entry -> entry.getFieldNameIn().equals(runtimeName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Field " + runtimeName + " not found"));
  }

  public AbstractField getFieldBySelector(String fieldSelector) {
    return getFieldBySelector(fieldSelector, fieldMap);
  }

  private AbstractField getFieldBySelector(String fieldSelector, Map<String, AbstractField>
          currentFieldMap) {
    if (currentFieldMap.containsKey(fieldSelector)) {
      return currentFieldMap.get(fieldSelector);
    } else {
      return getFieldBySelector(fieldSelector, getNestedItem(fieldSelector, currentFieldMap));
    }
  }

  private Map<String, AbstractField> getNestedItem(String fieldSelector, Map<String,
          AbstractField> currentFieldMap) {
    String key = currentFieldMap.keySet().stream().filter(fieldSelector::startsWith)
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Key not found"));
    return currentFieldMap.get(key).getAsComposite().getRawValue();
  }

  public void updateFieldBySelector(String selector, AbstractField field) {
    if (fieldMap.containsKey(selector)) {
      fieldMap.put(selector, field);
    } else {
      updateFieldMap(fieldMap.get(makeSelector(selector, 2))
              .getAsComposite()
              .getRawValue(), selector, 2, field);
    }
  }

  private void updateFieldMap(Map<String, AbstractField> currentFieldMap,
                                                  String selector, Integer position,
                                                  AbstractField field) {
    if (currentFieldMap.containsKey(selector)) {
      currentFieldMap.put(selector, field);
    } else {
        updateFieldMap(currentFieldMap.get(makeSelector(selector, position + 1))
                .getAsComposite()
                .getRawValue(), selector, 2, field);
    }
  }

  private String makeSelector(String selector, int position) {
    String[] selectorParts = selector.split(PropertySelectorConstants.PROPERTY_DELIMITER);
    StringBuilder selectorBuilder = new StringBuilder();
    for (int i = 0; i < position; i++) {
      selectorBuilder.append(selectorParts[i]);
      if (i != (position - 1)) {
        selectorBuilder.append(PropertySelectorConstants.PROPERTY_DELIMITER);
      }
    }

    return selectorBuilder.toString();
  }

  private String makeSelector(String prefix, String runtimeName) {
    return prefix + PropertySelectorConstants.PROPERTY_DELIMITER + runtimeName;
  }

  public void updateFieldBySelector(String selector, Integer value) {
    getFieldBySelector(selector).getAsPrimitive().setValue(value);
  }

  public void updateFieldBySelector(String selector, String value) {
    getFieldBySelector(selector).getAsPrimitive().setValue(value);
  }

  public void updateFieldBySelector(String selector, Float value) {
    getFieldBySelector(selector).getAsPrimitive().setValue(value);
  }

  public void updateFieldBySelector(String selector, Boolean value) {
    getFieldBySelector(selector).getAsPrimitive().setValue(value);
  }

  public void addField(AbstractField field) {
    this.fieldMap.put(makeKey(field), field);
  }

  public void addField(String runtimeName, Integer value) {
   addPrimitive(runtimeName, value);
  }

  public void addField(String runtimeName, Float value) {
    addPrimitive(runtimeName, value);
  }

  public void addField(String runtimeName, Boolean value) {
    addPrimitive(runtimeName, value);
  }

  public void addField(String runtimeName, String value) {
    addPrimitive(runtimeName, value);
  }

  private void addPrimitive(String runtimeName, Object value) {
    this.fieldMap.put(runtimeName, new PrimitiveField(runtimeName, runtimeName, value));
  }

  public void addFieldAtPosition(String baseSelector, AbstractField field) {
    getFieldBySelector(baseSelector).getAsComposite().addField
            (makeSelector(baseSelector, field.getFieldNameIn()), field);
  }

  private String makeKey(AbstractField field) {
    return sourceInfo.getSelectorPrefix()
            + PropertySelectorConstants.PROPERTY_DELIMITER
            + field.getFieldNameIn();
  }

  public Event getSubset(List<String> fieldSelectors) {
    return EventFactory.makeSubset(this, fieldSelectors);
  }

}
