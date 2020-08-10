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

package org.apache.streampipes.rest.impl;

import org.apache.streampipes.model.message.Notifications;
import org.apache.streampipes.rest.shared.annotation.JacksonSerialized;
import org.apache.streampipes.storage.api.IPipelineCategoryStorage;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2/users/{username}/pipelinecategories")
public class PipelineCategory extends AbstractRestInterface {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@JacksonSerialized
	public Response getCategories(@PathParam("username") String username) {
		return ok(getPipelineCategoryStorage()
				.getPipelineCategories());
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@JacksonSerialized
	public Response addCategory(@PathParam("username") String username, org.apache.streampipes.model.pipeline.PipelineCategory pipelineCategory) {
		boolean success = getPipelineCategoryStorage()
				.addPipelineCategory(pipelineCategory);
		if (success) return ok(Notifications.success("Category successfully stored. "));
		else return ok(Notifications.error("Could not create category."));
	}
	
	@DELETE
	@Path("/{categoryId}")
	@Produces(MediaType.APPLICATION_JSON)
	@JacksonSerialized
	public Response removeCategory(@PathParam("username") String username, @PathParam("categoryId") String categoryId) {
		boolean success = getPipelineCategoryStorage()
				.deletePipelineCategory(categoryId);
		if (success) return ok(Notifications.success("Category successfully deleted. "));
		else return ok(Notifications.error("Could not delete category."));
	}

	private IPipelineCategoryStorage getPipelineCategoryStorage() {
		return getNoSqlStorage().getPipelineCategoryStorageApi();
	}
}
