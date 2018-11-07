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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Session;

import org.onehippo.cms7.essentials.essentializer.rest.data.DataWrapper;
import org.onehippo.cms7.essentials.sdk.api.service.ContentTypeService;
import org.onehippo.cms7.essentials.sdk.api.service.JcrService;
import org.onehippo.cms7.essentials.sdk.api.service.PlaceholderService;
import org.onehippo.cms7.essentials.sdk.api.service.ProjectService;
import org.onehippo.cms7.essentials.sdk.api.service.SettingsService;
import org.onehippo.cms7.essentials.sdk.api.service.TemplateQueryService;

public class ServiceContext {
    public final JcrService jcrService;
    public final PlaceholderService placeholderService;
    public final ProjectService projectService;
    public final ContentTypeService contentTypeService;
    public final TemplateQueryService templateQueryService;
    public final SettingsService settingsService;
    public final Session session;
    public final DataWrapper data;
    public final Map<String, Object> placeholderData;
    public final Map<String, String> versionInstructionData;

    public ServiceContext(final JcrService jcrService, final PlaceholderService placeholderService, final ProjectService projectService,
                          final ContentTypeService contentTypeService, final TemplateQueryService templateQueryService, final SettingsService settingsService, final Session session,  final DataWrapper data) {
        this.jcrService = jcrService;
        this.placeholderService = placeholderService;
        this.projectService = projectService;
        this.contentTypeService = contentTypeService;
        this.templateQueryService = templateQueryService;
        this.settingsService = settingsService;
        this.session = session;
        this.data = data;
        this.placeholderData = WriteUtils.createPlaceHolders(placeholderService, data);
        this.versionInstructionData = new HashMap<>();
    }
}
