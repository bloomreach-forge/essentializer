/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cms7.essentials.essentializer.rest;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.onehippo.cms7.essentials.essentializer.rest.data.DataWrapper;
import org.onehippo.cms7.essentials.sdk.api.model.rest.UserFeedback;
import org.onehippo.cms7.essentials.sdk.api.service.ContentTypeService;
import org.onehippo.cms7.essentials.sdk.api.service.JcrService;
import org.onehippo.cms7.essentials.sdk.api.service.PlaceholderService;
import org.onehippo.cms7.essentials.sdk.api.service.ProjectService;
import org.onehippo.cms7.essentials.sdk.api.service.SettingsService;
import org.onehippo.cms7.essentials.sdk.api.service.TemplateQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onehippo.cms7.essentials.essentializer.rest.EssentializerUtils.fetchData;


@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
@Path("essentializer")
public class EssentializerResource {

    private static final Logger log = LoggerFactory.getLogger(EssentializerResource.class);

    @Inject
    private JcrService jcrService;
    @Inject
    private PlaceholderService placeholderService;
    @Inject
    private ProjectService projectService;
    @Inject
    private ContentTypeService contentTypeService;
    @Inject
    private TemplateQueryService templateQueryService;
    @Inject
    private SettingsService settingsService;

    @GET
    @Path("/")
    public DataWrapper initialize(@Context ServletContext servletContext, @Context HttpServletResponse response) throws Exception {

        final Session session = jcrService.createSession();
        try {
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.error("No session found");
                return new DataWrapper();
            }
            final ServiceContext context = createServiceContext(session, new DataWrapper());
            final Set<String> hstRoots = fetchHstRoots(context);
            context.hstRoots.addAll(hstRoots);
            return fetchData(context);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }


    @POST
    @Path("/")
    public UserFeedback create(final DataWrapper data, @Context ServletContext servletContext) {
        log.info("data {}", data);
        final Session session = jcrService.createSession();
        try {

            final ServiceContext context = createServiceContext(session, data);
            final Set<String> hstRoots = fetchHstRoots(context);
            context.hstRoots.addAll(hstRoots);
            return WriteUtils.createPlugin(context);
        } catch (Exception e) {
            log.error("Error creating plugin", e);
            return new UserFeedback().addError("Error creating plugin:" + e.getMessage());
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    private Set<String> fetchHstRoots(final ServiceContext context) throws RepositoryException {
        final Node node = context.session.getNode("/");
        final NodeIterator nodes = node.getNodes();
        final Set<String> ourSet = new HashSet<>();
        while (nodes.hasNext()) {
            final Node n = nodes.nextNode();
            if (n.isNodeType("hst:hst")) {
                ourSet.add(n.getName());

            }
        }
        return ourSet;
    }

    private ServiceContext createServiceContext(final Session session, final DataWrapper data) {
        return new ServiceContext(jcrService, placeholderService, projectService, contentTypeService, templateQueryService, settingsService, session, data);
    }


}
