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

import java.util.ArrayList;
import java.util.List;

import org.onehippo.cms7.essentials.essentializer.rest.EssentializerUtils;
import org.onehippo.cms7.essentials.sdk.api.model.rest.PluginDescriptor;

import com.google.common.collect.ImmutableList;

public class DataWrapper {


    private String pluginVersion = "1.0.0-SNAPSHOT";
    private String pluginDescription = "Essentials plugin";
    private String pluginName = "Essentials plugin";
    private String groupId = "org.onehippo.essentials";
    private String artifactId = "essentials-plugin";

    private String essentialsVersion;
    private String pluginType;
    private String license;
    private List<String> licenseTypes;
    private String pluginId = "myPlugin";
    private List<String> pluginTypes;
    private String targetDirectory;
    private boolean createInterface;


    private List<PageContainerWrapper> pageContainers = new ArrayList<>();
    private List<PageContainerWrapper> selectedPageContainers;
    private List<PageWrapper> pages = new ArrayList<>();
    private List<PageWrapper> selectedPages;
    private List<ContentWrapper> content;
    private List<ContentWrapper> selectedContent;

    private List<SitemapItemWrapper> selectedSitemapItems;
    private List<SitemapItemWrapper> sitemapItems = new ArrayList<>();
    private List<DependencyWrapper> dependencies;
    private List<DependencyWrapper> selectedDependencies;
    private List<DependencyWrapper> sharedDependencies;
    private List<DependencyWrapper> selectedSharedDependencies;
    private List<YamlWrapper> yamlFiles;
    private List<YamlWrapper> selectedYamlFiles;
    private List<YamlBinaryWrapper> yamlBinaryFiles;
    private List<YamlBinaryWrapper> selectedYamlBinaryFiles;
    private List<MenuItemWrapper> menuItems = new ArrayList<>();
    private List<MenuItemWrapper> selectedMenuItems;
    private List<MenuWrapper> menus = new ArrayList<>();
    private List<MenuWrapper> selectedMenus;
    private List<SiteWrapper> sites = new ArrayList<>();
    private List<SiteWrapper> selectedSites;
    private List<MountWrapper> mounts = new ArrayList<>();
    private List<MountWrapper> selectedMounts;


    private List<String> selectedPluginDependencies;
    private List<String> selectedInstalledPluginDependencies;
    private List<String> pluginDependencies;
    private List<String> installedPluginDependencies;
    private List<CatalogComponentWrapper> catalogComponents = new ArrayList<>();
    private List<ComponentWrapper> components = new ArrayList<>();
    private List<ComponentWrapper> selectedComponents;
    private List<TemplateWrapper> templates = new ArrayList<>();
    private List<WebFileWrapper> webFiles;
    private List<DocumentTypeWrapper> documentTypes;
    private List<FileWrapper> files;
    // selected parts:
    private List<CatalogComponentWrapper> selectedCatalogComponents;
    private List<TemplateWrapper> selectedTemplates;
    private List<WebFileWrapper> selectedWebFiles;
    private List<DocumentTypeWrapper> selectedDocumentTypes;
    private List<FileWrapper> selectedFiles;

    public List<String> getSelectedInstalledPluginDependencies() {
        return selectedInstalledPluginDependencies;
    }

    public void setSelectedInstalledPluginDependencies(final List<String> selectedInstalledPluginDependencies) {
        this.selectedInstalledPluginDependencies = selectedInstalledPluginDependencies;
    }

    public List<String> getInstalledPluginDependencies() {
        installedPluginDependencies = getPluginDependencies();
        return installedPluginDependencies;
    }

    public void setInstalledPluginDependencies(final List<String> installedPluginDependencies) {
        this.installedPluginDependencies = installedPluginDependencies;
    }


    public List<PageContainerWrapper> getPageContainers() {
        return pageContainers;
    }

    public void setPageContainers(final List<PageContainerWrapper> pageContainers) {
        this.pageContainers = pageContainers;
    }

    public List<PageContainerWrapper> getSelectedPageContainers() {
        return selectedPageContainers;
    }

    public void setSelectedPageContainers(final List<PageContainerWrapper> selectedPageContainers) {
        this.selectedPageContainers = selectedPageContainers;
    }

    public List<SiteWrapper> getSites() {
        return sites;
    }

    public void setSites(final List<SiteWrapper> sites) {
        this.sites = sites;
    }

    public List<SiteWrapper> getSelectedSites() {
        return selectedSites;
    }

    public void setSelectedSites(final List<SiteWrapper> selectedSites) {
        this.selectedSites = selectedSites;
    }

    public List<MountWrapper> getMounts() {
        return mounts;
    }

    public void setMounts(final List<MountWrapper> mounts) {
        this.mounts = mounts;
    }

    public List<MountWrapper> getSelectedMounts() {
        return selectedMounts;
    }

    public void setSelectedMounts(final List<MountWrapper> selectedMounts) {
        this.selectedMounts = selectedMounts;
    }

    public List<MenuWrapper> getMenus() {
        return menus;
    }

    public void setMenus(final List<MenuWrapper> menus) {
        this.menus = menus;
    }

    public List<MenuWrapper> getSelectedMenus() {
        return selectedMenus;
    }

    public void setSelectedMenus(final List<MenuWrapper> selectedMenus) {
        this.selectedMenus = selectedMenus;
    }

    public List<YamlBinaryWrapper> getYamlBinaryFiles() {
        return yamlBinaryFiles;
    }

    public void setYamlBinaryFiles(final List<YamlBinaryWrapper> yamlBinaryFiles) {
        this.yamlBinaryFiles = yamlBinaryFiles;
    }

    public List<YamlBinaryWrapper> getSelectedYamlBinaryFiles() {
        return selectedYamlBinaryFiles;
    }

    public void setSelectedYamlBinaryFiles(final List<YamlBinaryWrapper> selectedYamlBinaryFiles) {
        this.selectedYamlBinaryFiles = selectedYamlBinaryFiles;
    }

    public List<YamlWrapper> getYamlFiles() {
        return yamlFiles;
    }

    public void setYamlFiles(final List<YamlWrapper> yamlFiles) {
        this.yamlFiles = yamlFiles;
    }

    public List<YamlWrapper> getSelectedYamlFiles() {
        return selectedYamlFiles;
    }

    public void setSelectedYamlFiles(final List<YamlWrapper> selectedYamlFiles) {
        this.selectedYamlFiles = selectedYamlFiles;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(final String pluginId) {
        this.pluginId = pluginId;
    }

    public List<FileWrapper> getFiles() {
        return files;
    }

    public void setFiles(final List<FileWrapper> files) {
        this.files = files;
    }

    public List<FileWrapper> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(final List<FileWrapper> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    public List<DocumentTypeWrapper> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(final List<DocumentTypeWrapper> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public List<WebFileWrapper> getWebFiles() {
        return webFiles;
    }

    public void setWebFiles(final List<WebFileWrapper> webFiles) {
        this.webFiles = webFiles;
    }


    public List<ComponentWrapper> getComponents() {
        return components;
    }

    public void setComponents(final List<ComponentWrapper> components) {
        this.components = components;
    }

    public List<ComponentWrapper> getSelectedComponents() {
        return selectedComponents;
    }

    public void setSelectedComponents(final List<ComponentWrapper> selectedComponents) {
        this.selectedComponents = selectedComponents;
    }

    public List<CatalogComponentWrapper> getCatalogComponents() {
        return catalogComponents;
    }

    public void setCatalogComponents(final List<CatalogComponentWrapper> catalogComponents) {
        this.catalogComponents = catalogComponents;
    }

    public List<CatalogComponentWrapper> getSelectedCatalogComponents() {
        return selectedCatalogComponents;
    }

    public void setSelectedCatalogComponents(final List<CatalogComponentWrapper> selectedCatalogComponents) {
        this.selectedCatalogComponents = selectedCatalogComponents;
    }

    public List<TemplateWrapper> getTemplates() {
        return templates;
    }

    public void setTemplates(final List<TemplateWrapper> templates) {
        this.templates = templates;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(final String pluginName) {
        this.pluginName = pluginName;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(final String artifactId) {
        this.artifactId = artifactId;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(final String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }


    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(final String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public String getPluginDescription() {
        return pluginDescription;
    }

    public void setPluginDescription(final String pluginDescription) {
        this.pluginDescription = pluginDescription;
    }

    public List<TemplateWrapper> getSelectedTemplates() {
        return selectedTemplates;
    }

    public void setSelectedTemplates(final List<TemplateWrapper> selectedTemplates) {
        this.selectedTemplates = selectedTemplates;
    }

    public List<WebFileWrapper> getSelectedWebFiles() {
        return selectedWebFiles;
    }

    public void setSelectedWebFiles(final List<WebFileWrapper> selectedWebFiles) {
        this.selectedWebFiles = selectedWebFiles;
    }

    public List<DocumentTypeWrapper> getSelectedDocumentTypes() {
        return selectedDocumentTypes;
    }

    public void setSelectedDocumentTypes(final List<DocumentTypeWrapper> selectedDocumentTypes) {
        this.selectedDocumentTypes = selectedDocumentTypes;
    }

    public boolean isCreateInterface() {
        return createInterface;
    }

    public void setCreateInterface(final boolean createInterface) {
        this.createInterface = createInterface;
    }

    public String getEssentialsVersion() {
        return essentialsVersion;
    }

    public void setEssentialsVersion(final String essentialsVersion) {
        this.essentialsVersion = essentialsVersion;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }


    public List<String> getLicenseTypes() {
        if (licenseTypes == null) {
            licenseTypes = ImmutableList.of(EssentializerUtils.LICENSE_COMMUNITY, EssentializerUtils.LICENSE_ENTERPRISE);
        }
        return licenseTypes;
    }


    public void setLicenseTypes(final List<String> licenseTypes) {
        this.licenseTypes = licenseTypes;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(final String license) {
        this.license = license;
    }


    public List<String> getSelectedPluginDependencies() {
        return selectedPluginDependencies;
    }

    public void setSelectedPluginDependencies(final List<String> selectedPluginDependencies) {
        this.selectedPluginDependencies = selectedPluginDependencies;
    }

    public List<String> getPluginDependencies() {
        if (pluginDependencies == null) {
            pluginDependencies = ImmutableList.of(
                    "skeleton",
                    "enterpriseProject",
                    "videoComponentPlugin",
                    "urlrewriterPlugin",
                    "sitemapPlugin",
                    "taggingPlugin",
                    "taxonomyPlugin",
                    "seoPlugin",
                    "selectionPlugin",
                    "searchPlugin",
                    "robotsPlugin",
                    "relevanceCollectorsBundlePlugin",
                    "luceneIndexExporter",
                    "bannerPlugin",
                    "projectsPlugin",
                    "pollPlugin",
                    "simpleContent",
                    "relatedDocumentsPlugin",
                    "imageComponentPlugin",
                    "relevancePlugin",
                    "personalization",
                    "crisp");
        }


        return pluginDependencies;
    }

    public void setPluginDependencies(final List<String> pluginDependencies) {
        this.pluginDependencies = pluginDependencies;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(final String pluginType) {
        this.pluginType = pluginType;
    }

    public List<String> getPluginTypes() {
        if (pluginTypes == null) {
            pluginTypes = ImmutableList.of(PluginDescriptor.TYPE_TOOL, PluginDescriptor.TYPE_FEATURE);
        }
        return pluginTypes;
    }

    public void setPluginTypes(final List<String> pluginTypes) {
        this.pluginTypes = pluginTypes;
    }

    public List<PageWrapper> getSelectedPages() {
        return selectedPages;
    }

    public void setSelectedPages(final List<PageWrapper> selectedPages) {
        this.selectedPages = selectedPages;
    }

    public List<PageWrapper> getPages() {
        return pages;
    }

    public void setPages(final List<PageWrapper> pages) {
        this.pages = pages;
    }

    public List<SitemapItemWrapper> getSelectedSitemapItems() {
        return selectedSitemapItems;
    }

    public void setSelectedSitemapItems(final List<SitemapItemWrapper> selectedSitemapItems) {
        this.selectedSitemapItems = selectedSitemapItems;
    }

    public List<SitemapItemWrapper> getSitemapItems() {
        return sitemapItems;
    }

    public void setSitemapItems(final List<SitemapItemWrapper> sitemapItems) {
        this.sitemapItems = sitemapItems;
    }

    public List<DependencyWrapper> getDependencies() {
        return dependencies;
    }

    public void setDependencies(final List<DependencyWrapper> dependencies) {
        this.dependencies = dependencies;
    }

    public List<DependencyWrapper> getSelectedDependencies() {
        return selectedDependencies;
    }

    public void setSelectedDependencies(final List<DependencyWrapper> selectedDependencies) {
        this.selectedDependencies = selectedDependencies;
    }

    public List<DependencyWrapper> getSharedDependencies() {
        return sharedDependencies;
    }

    public void setSharedDependencies(final List<DependencyWrapper> sharedDependencies) {
        this.sharedDependencies = sharedDependencies;
    }

    public List<DependencyWrapper> getSelectedSharedDependencies() {
        return selectedSharedDependencies;
    }

    public void setSelectedSharedDependencies(final List<DependencyWrapper> selectedSharedDependencies) {
        this.selectedSharedDependencies = selectedSharedDependencies;
    }

    public List<MenuItemWrapper> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(final List<MenuItemWrapper> menuItems) {
        this.menuItems = menuItems;
    }

    public List<MenuItemWrapper> getSelectedMenuItems() {
        return selectedMenuItems;
    }

    public void setSelectedMenuItems(final List<MenuItemWrapper> selectedMenuItems) {
        this.selectedMenuItems = selectedMenuItems;
    }

    public List<ContentWrapper> getContent() {
        return content;
    }

    public void setContent(final List<ContentWrapper> content) {
        this.content = content;
    }

    public List<ContentWrapper> getSelectedContent() {
        return selectedContent;
    }

    public void setSelectedContent(final List<ContentWrapper> selectedContent) {
        this.selectedContent = selectedContent;
    }

    public void addCatalogComponents(final List<CatalogComponentWrapper> collection) {
        catalogComponents.addAll(collection);
    }

    public void addComponents(final List<ComponentWrapper> collection) {
        components.addAll(collection);
    }

    public void addPages(final List<PageWrapper> collection) {
        pages.addAll(collection);
    }

    public void addSitemapItems(final List<SitemapItemWrapper> collection) {
        sitemapItems.addAll(collection);
    }

    public void addTemplates(final List<TemplateWrapper> collection) {
        templates.addAll(collection);
    }

    public void addMenuItems(final List<MenuItemWrapper> collection) {
        menuItems.addAll(collection);
    }

    public void addPageContainers(final List<PageContainerWrapper> collection) {
        pageContainers.addAll(collection);
    }

    public void addMenus(final List<MenuWrapper> collection) {
        menus.addAll(collection);
    }

    public void addMounts(final List<MountWrapper> collection) {
        mounts.addAll(collection);
    }

    public void addSites(final List<SiteWrapper> collections) {
        sites.addAll(collections);
    }
}
