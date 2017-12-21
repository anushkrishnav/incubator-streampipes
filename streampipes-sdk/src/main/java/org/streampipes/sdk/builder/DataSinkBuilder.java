package org.streampipes.sdk.builder;

import org.streampipes.model.DataSinkType;
import org.streampipes.model.graph.DataSinkDescription;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DataSinkBuilder extends AbstractProcessingElementBuilder<DataSinkBuilder, DataSinkDescription> {

    protected DataSinkBuilder(String id, String label, String description) {
        super(id, label, description, new DataSinkDescription());
    }

    /**
     * Creates a new data sink using the builder pattern.
     * @param id A unique identifier of the new element, e.g., com.mycompany.sink.mynewdatasink
     * @param label A human-readable name of the element. Will later be shown as the element name in the StreamPipes UI.
     * @param description A human-readable description of the element.
     */
    public static DataSinkBuilder create(String id, String label, String description)
    {
        return new DataSinkBuilder(id, label, description);
    }

    public DataSinkBuilder category(DataSinkType... categories) {
        this.elementDescription
                .setCategory(Arrays
                        .stream(categories)
                        .map(Enum::name)
                        .collect(Collectors.toList()));
        return me();
    }

    @Override
    protected DataSinkBuilder me() {
        return this;
    }
}
