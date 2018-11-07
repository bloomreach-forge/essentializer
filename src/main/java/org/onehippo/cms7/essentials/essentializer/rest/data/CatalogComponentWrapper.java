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

package org.onehippo.cms7.essentials.essentializer.rest.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CatalogComponentWrapper {
    private static final Logger log = LoggerFactory.getLogger(CatalogComponentWrapper.class);
    
    private String path;
    private String name;
    private String label;
    private String type;
    private String iconPath;
    private String componentClassName;
    private String componentSourcePath;
    
    private TemplateWrapper templateWrapper;
    
    public void setPath(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    public String getIconPath() {
        return iconPath;
    }

    public TemplateWrapper getTemplateWrapper() {
        return templateWrapper;
    }

    public void setTemplateWrapper(final TemplateWrapper templateWrapper) {
        this.templateWrapper = templateWrapper;
    }

    public String getComponentClassName() {
        return componentClassName;
    }

    public void setComponentClassName(final String componentClassName) {
        this.componentClassName = componentClassName;
    }

    public String getComponentSourcePath() {
        return componentSourcePath;
    }

    public void setComponentSourcePath(final String componentSourcePath) {
        this.componentSourcePath = componentSourcePath;
    }
}
