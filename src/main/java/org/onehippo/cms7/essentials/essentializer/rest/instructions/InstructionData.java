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

package org.onehippo.cms7.essentials.essentializer.rest.instructions;

public class InstructionData {

    private TYPE type;
    private String path;
    private String name;
    private String data;
    private Object rawData;

    

    public InstructionData(final TYPE type, final String path) {
        this.path = path;
        this.type = type;
    }

    public InstructionData(final TYPE type, final String path, final String data) {
        this.type = type;
        this.path = path;
        this.data = data;
    }


    public Object getRawData() {
        return rawData;
    }

    public void setRawData(final Object rawData) {
        this.rawData = rawData;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public TYPE getType() {
        return type == null ? TYPE.UNKNOWN : type;
    }

    public void setType(final TYPE type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public enum TYPE {
        PROJECT_FILE, SITE_BINARY, SITE_TEXT, SITE_JAVA, UNKNOWN,
        CATALOG_COMPONENT_XML,
        WEBFILE, BINARY_WEBFILE, TEXT_WEBFILE, FTL_FILE, FTL_WEBFILE,
        REPOSITORY_XML,
        DEPENDENCY,
        SHARED_DEPENDENCY,
        FILE_XML,
        DOCUMENT_TYPE_XML,
        HST_PAGE,
        HST_COMPONENT,
        HST_SITEMAP_ITEM,
        HST_SITE,
        HST_PAGE_CONTAINER,
        HST_MOUNT,
        HST_MENU_ITEM,
        HST_MENU,
        YAML_FILE,
        YAML_BINARY_FILE,
        TEMPLATE_XML


    }


    @Override
    public String toString() {
        return "InstructionData{" +
                "type=" + type +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
