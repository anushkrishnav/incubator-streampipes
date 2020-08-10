/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.streampipes.model.staticproperty;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.fogsy.empire.annotations.RdfsClass;
import org.apache.streampipes.vocabulary.StreamPipes;

import javax.persistence.Entity;

@RdfsClass(StreamPipes.ONE_OF_STATIC_PROPERTY)
@Entity
@JsonSubTypes({
        @JsonSubTypes.Type(RuntimeResolvableOneOfStaticProperty.class),
})
public class OneOfStaticProperty extends SelectionStaticProperty {

  private static final long serialVersionUID = 3483290363677184344L;

  public OneOfStaticProperty() {
    super(StaticPropertyType.OneOfStaticProperty);
  }

  public OneOfStaticProperty(StaticPropertyType staticPropertyType) {
    super(staticPropertyType);
  }

  public OneOfStaticProperty(StaticPropertyType staticPropertyType, String internalName,
                             String label, String description) {
    super(staticPropertyType, internalName, label, description);
  }

  public OneOfStaticProperty(StaticPropertyType staticPropertyType, String internalName,
                             String label, String description, boolean horizontalRendering) {
    super(staticPropertyType, internalName, label, description, horizontalRendering);
  }

  public OneOfStaticProperty(OneOfStaticProperty other) {
    super(other);
  }

  public OneOfStaticProperty(String internalName, String label, String description) {
    super(StaticPropertyType.OneOfStaticProperty, internalName, label, description);
  }

  public OneOfStaticProperty(String internalName, String label, String description, boolean horizontalRendering) {
    super(StaticPropertyType.OneOfStaticProperty, internalName, label, description, horizontalRendering);
  }


}
