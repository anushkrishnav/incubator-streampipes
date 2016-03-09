package de.fzi.cep.sepa.esper.pattern.streamstopped;

import de.fzi.cep.sepa.model.impl.graph.SepaInvocation;
import de.fzi.cep.sepa.runtime.param.BindingParameters;

public class StreamStoppedParameter extends BindingParameters {

	private String topic;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StreamStoppedParameter(SepaInvocation graph, String topic) {
		super(graph);
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

}