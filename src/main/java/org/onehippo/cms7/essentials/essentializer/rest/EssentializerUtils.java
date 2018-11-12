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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.hippoecm.repository.util.JcrUtils;
import org.onehippo.cms7.essentials.essentializer.rest.data.*;
import org.onehippo.cms7.essentials.plugin.sdk.utils.EssentialConst;
import org.onehippo.cms7.essentials.plugin.sdk.utils.MavenModelUtils;
import org.onehippo.cms7.essentials.sdk.api.model.Module;
import org.onehippo.cms7.essentials.sdk.api.service.PlaceholderService;
import org.onehippo.cms7.essentials.sdk.api.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.util.ImmutableMap;

import static java.util.stream.Collectors.toList;

public final class EssentializerUtils {

    public static final String LICENSE_COMMUNITY = "community";
    public static final String LICENSE_ENTERPRISE = "enterprise";
    private static final Logger log = LoggerFactory.getLogger(EssentializerUtils.class);

    public static final String DEFUALT_ESSENTIALS_VERSION = "4.4.0-1";
    public static final Set<String> INCLUDED_FILE_EXTENSIONS = new ImmutableSet.Builder<String>()
            .add(".java").add(".css").add(".js").add(".properties").add(".txt")
            .add(".xml").add(".xhtml").add(".html").add(".htm").add(".cnd").add(".json")
            .add(".sass").add(".less").add(".xsl").add(".xslt").add(".dtd")
            .add(".png").add(".jpg").add(".jpeg").add(".gif").add("svg").add(".tiff").add(".tif").add(".bmp").add(".ico").add(".ai").add(".psd")
            .add(".ttf").add(".woff").add(".woff2").add(".eot")
            .add(".pdf").add(".zip").add(".jar").add(".keystore").add(".jks").add(".p12").add(".pfx").add(".pem").add(".der").add(".crt").add(".cer")
            .add(".ftl").add(".jsp").add(".jspf").add(".jspx").add(".tag")
            .add(".yaml")
            .build();

    /**
     * NOTE: binary and files we don't want to process (e.g. put some placeholders)
     */
    public static final Set<String> BINARY_EXTENSIONS = new ImmutableSet.Builder<String>()
            .add(".png").add(".jpg").add(".jpeg").add(".gif").add("svg").add(".tiff").add(".tif").add(".bmp").add(".ico").add(".ai").add(".psd")
            .add(".ttf").add(".woff").add(".woff2").add(".eot")
            .add(".pdf").add(".zip").add(".jar").add(".keystore").add(".jks").add(".p12").add(".pfx").add(".pem").add(".der").add(".crt").add(".cer")
            .build();

    public static final Set<String> EXCLUDE_SITEMAP_ITEMS = new ImmutableSet.Builder<String>()
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.css").add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.CSS")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.gif").add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.GIF")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.ico").add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.ICO")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.jpeg").add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.JPEG")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.jpg").add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.JPG")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.js").add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.JS")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.jsp").add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.JSP")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.pdf").add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.PDF")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.png").add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.PNG")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.svg").add("/hst:hst/hst:configurations/hst:default/hst:sitemap/_any_.SVG")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/binaries")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/favicon.ico")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/login")
            .add("/hst:hst/hst:configurations/hst:default/hst:sitemap/webfiles")
            .build();

    public static final Set<String> EXCLUDED_FILES = new ImmutableSet.Builder<String>()
            .add("pom.xml")
            .build();

    public static final Map<String, String> CONTENT_PATHS = new ImmutableMap.Builder<String, String>()
            .put("/content/documents", "contentDocuments")
            .put("/content/gallery", "contentGallery")
            .put("/content/assets", "contentAssets")
            .put("/content/taxonomies", "contentTaxonomies")
            .put("/content/eforms", "contentEforms")
            .put("/content/urlrewriter", "contentUrlRewriter")
            .build();
    public static final Set<String> EXCLUDED_KEYS = new ImmutableSet.Builder<String>()
            .add(PlaceholderService.DATE_CURRENT_MM)
            .add(PlaceholderService.DATE_CURRENT_MONTH)
            .add(PlaceholderService.DATE_CURRENT_YEAR)
            .add(PlaceholderService.DATE_CURRENT_YYYY)
            .add(PlaceholderService.DATE_NEXT_MM)
            .add(PlaceholderService.DATE_NEXT_YYYY)
            .add(WriteUtils.PH_ESSENTIALS_VERSION)
            .add(WriteUtils.PH_PLUGIN_VERSION)
            .add(WriteUtils.PH_PLUGIN_ID)
            .add(WriteUtils.PH_PLUGIN_NAME)

            .add("currentMonth")
            .build();


    private EssentializerUtils() {
    }

    public static DataWrapper fetchData(final ServiceContext context) throws RepositoryException, IOException {
        final DataWrapper data = new DataWrapper();
        data.setEssentialsVersion(extractEssentialsVersion(context));
        data.setCatalogComponents(fetchCatalogComponents(context));
        data.setComponents(fetchComponents(context));
        data.setPages(fetchPages(context));
        data.setSitemapItems(fetchSitemapItems(context));
        data.setTemplates(fetchTemplates(context));
        data.setDocumentTypes(fetchDocumentTypes(context));
        data.setFiles(fetchFiles(context));
        data.setWebFiles(fetchWebFiles(context));
        data.setMenuItems(fetchMenuItems(context));
        data.setPageContainers(fetchPageContainers(context));
        data.setMenus(fetchMenus(context));
        data.setMounts(fetchMounts(context));
        data.setSites(fetchSites(context));
        final List<DependencyWrapper> dependencies = fetchDependencies(context);
        data.setDependencies(dependencies);
        final List<DependencyWrapper> sharedDependencies = dependencies
                .stream()
                .filter(dependencyWrapper -> dependencyWrapper.getModule() == Module.PROJECT)
                .collect(toList());
        data.setSharedDependencies(sharedDependencies);
        data.setYamlFiles(fetchYamFiles(context));
        data.setYamlBinaryFiles(fetchYamBinaryFiles(context));
        data.setContent(fetchContent(context));
        return data;
    }

    private static List<PageContainerWrapper> fetchPageContainers(final ServiceContext context) throws RepositoryException {
        final List<PageContainerWrapper> templates = new ArrayList<>();
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("hst:hst/hst:configurations//element(*, hst:containercomponentfolder)", Query.XPATH);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            final String name = node.getName();
            if (name.equals("hst:containers")) {
                continue;
            }
            final PageContainerWrapper wrapper = new PageContainerWrapper();
            wrapper.setPath(node.getPath());
            wrapper.setName(name);
            templates.add(wrapper);
        }

        return templates;
    }

    public static List<ContentWrapper> fetchContent(final ServiceContext context) throws RepositoryException {
        final List<ContentWrapper> content = new ArrayList<>();
        final Session session = context.session;
        for (String path : CONTENT_PATHS.keySet()) {
            if (session.nodeExists(path)) {
                final Node node = session.getNode(path);
                // add document root nodes
                addNodes(node, content);
            }
            processSubNodes(content, session, path);
        }
        return content;
    }

    private static void processSubNodes(final List<ContentWrapper> content, final Session session, final String path) throws RepositoryException {
        if (session.nodeExists(path)) {
            final Node root = session.getNode(path);
            final NodeIterator documentNodes = root.getNodes();

            while (documentNodes.hasNext()) {
                final Node node = documentNodes.nextNode();
                addNodes(node, content);

            }
        }
    }

    private static void addNodes(final Node root, final List<ContentWrapper> documents) throws RepositoryException {

        final NodeIterator nodes = root.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            final ContentWrapper contentWrapper = new ContentWrapper();
            contentWrapper.setName(node.getName());
            contentWrapper.setPath(node.getPath());
            documents.add(contentWrapper);
        }
    }

    public static List<TemplateWrapper> fetchTemplates(final ServiceContext context) throws RepositoryException {
        final List<TemplateWrapper> templates = new ArrayList<>();
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("hst:hst/hst:configurations//element(*, hst:template)", Query.XPATH);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            final TemplateWrapper wrapper = new TemplateWrapper();
            wrapper.setPath(node.getPath());
            wrapper.setName(node.getName());
            wrapper.setRenderPath(JcrUtils.getStringProperty(node, "hst:renderpath", null));
            wrapper.setDescription(JcrUtils.getStringProperty(node, "hst:description", null));
            templates.add(wrapper);
        }

        return templates;
    }

    public static TemplateWrapper fetchTemplateForName(final ServiceContext context, final String name) throws RepositoryException {
        if (name == null) {
            return null;
        }

        final List<TemplateWrapper> templateWrappers = fetchTemplates(context);
        for (TemplateWrapper templateWrapper : templateWrappers) {
            if (name.equals(templateWrapper.getName())) {
                return templateWrapper;
            }
        }
        return null;
    }


    /**
     * Collect project files
     *
     * @param extensions set of extensions e.g. java
     * @return project files
     */
    public static List<Path> getProjectFiles(final Set<String> extensions, final ServiceContext context) throws IOException {
        return Files.walk(new File(getBaseProjectDirectory()).toPath())
                    .filter(s -> matches(s.toString(), extensions, context))
                    .sorted()
                    .collect(toList());
    }

    public static List<Path> getAllProjectFiles(final Set<String> extensions, final ServiceContext context) throws IOException {
        return Files.walk(new File(getBaseProjectDirectory()).toPath())
                    .filter(s -> matchesAll(s.toString(), extensions, context))
                    .sorted()
                    .collect(toList());
    }

    private static boolean matches(final String file, final Set<String> extensions, final ServiceContext context) {
        if (isExcluded(file)) {
            return false;
        }
        // allow non project files
        // check if inside site/cms/
        /*final String sitePath = context.projectService.getBasePathForModule(Module.SITE).toString();
        final String cmsPath = context.projectService.getBasePathForModule(Module.CMS).toString();
        if (!file.contains(sitePath) && !file.contains(cmsPath)) {
            // only site files
            return false;
        }*/

        return isValidExtension(file, extensions);

    }

    private static boolean isValidExtension(final String file, final Set<String> extensions) {
        final int idx = file.lastIndexOf('.');
        if (idx > -1) {
            final String extension = file.substring(idx).toLowerCase();
            return extensions.contains(extension);
        }
        return false;
    }

    private static boolean matchesAll(final String file, final Set<String> extensions, final ServiceContext context) {
        if (isExcluded(file)) {
            return false;
        }
        return isValidExtension(file, extensions);

    }

    private static boolean isExcluded(final String file) {
        final File aFile = new File(file);
        if (!aFile.exists() || aFile.isDirectory()) {
            return true;
        }
        if (file.contains(WriteUtils.FS + "classes" + WriteUtils.FS)
                || file.contains(".git")
                || file.contains(".svn")
                || file.contains(".idea")
                || file.contains(WriteUtils.FS+ "overlays" + WriteUtils.FS)
                || file.contains(WriteUtils.FS + "target" + WriteUtils.FS)) {
            log.debug("Skipping excluded folder {}", file);
            return true;
        }
        return false;
    }


    public static String getBaseProjectDirectory() {
        final String basePath = System.getProperty(EssentialConst.PROJECT_BASEDIR_PROPERTY);

        if (StringUtils.isNotBlank(basePath)) {
            return basePath;
        }
        throw new IllegalStateException("System property 'project.basedir' was null or empty. Please start your application with -D=project.basedir=/project/path");
    }

    public static List<DocumentTypeWrapper> fetchDocumentTypes(final ServiceContext context) throws RepositoryException {

        final List<DocumentTypeWrapper> paths = new ArrayList<>();
        final String namespace = context.settingsService.getSettings().getProjectNamespace();
        final NodeIterator nodes = context.session.getNode("/hippo:namespaces/" + namespace).getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            if (!node.getName().equals("basedocument")) {
                final DocumentTypeWrapper documentType = new DocumentTypeWrapper();
                final Node nodeType = node.getNode("hipposysedit:nodetype/hipposysedit:nodetype");
                final String[] superTypes = JcrUtils.getMultipleStringProperty(nodeType, "hipposysedit:supertype", ArrayUtils.EMPTY_STRING_ARRAY);
                documentType.setSuperTypes(superTypes);
                documentType.setName(node.getName());
                documentType.setPath(node.getPath());
                paths.add(documentType);
            }
        }

        return paths;
    }


    public static List<ComponentWrapper> fetchComponents(final ServiceContext context) throws RepositoryException {
        final List<ComponentWrapper> components = new ArrayList<>();
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("hst:hst/hst:configurations//element(*, hst:abstractcomponent)", Query.XPATH);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            final ComponentWrapper wrapper = new ComponentWrapper();
            final String path = node.getPath();
            if (!path.contains("hst:component")) {
                log.debug("Skipping component: {}", path);
                continue;
            }
            wrapper.setPath(path);
            wrapper.setName(node.getName());
            final String template = JcrUtils.getStringProperty(node, "hst:template", null);
            wrapper.setTemplateWrapper(fetchTemplateForName(context, template));
            wrapper.setTemplateWrapper(fetchTemplateForName(context, template));
            components.add(wrapper);
        }
        return components;
    }


    public static List<CatalogComponentWrapper> fetchCatalogComponents(final ServiceContext context) throws RepositoryException {
        final List<CatalogComponentWrapper> components = new ArrayList<>();
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("hst:hst/hst:configurations//element(*, hst:containeritemcomponent)", Query.XPATH);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            final CatalogComponentWrapper wrapper = new CatalogComponentWrapper();
            final String path = node.getPath();
            if (path.contains("hst:workspace")) {
                log.debug("Skipping workspace component: {}", path);
                continue;
            }
            wrapper.setPath(path);
            final String className = JcrUtils.getStringProperty(node, "hst:componentclassname", null);
            if (Strings.isNullOrEmpty(className)) {
                log.warn("Empty component classname for: {}, skipping", path);
                continue;
            }
            wrapper.setLabel(JcrUtils.getStringProperty(node, "hst:label", "TODO: My component"));
            wrapper.setIconPath(JcrUtils.getStringProperty(node, "hst:iconpath", "images/missing.png"));
            wrapper.setType(JcrUtils.getStringProperty(node, "hst:xtype", "hst.item"));
            final String template = JcrUtils.getStringProperty(node, "hst:template", null);
            wrapper.setTemplateWrapper(fetchTemplateForName(context, template));
            wrapper.setTemplateWrapper(fetchTemplateForName(context, template));
            wrapper.setComponentClassName(className);
            wrapper.setName(node.getName());
            components.add(wrapper);
        }

        return components;
    }

    public static List<PageWrapper> fetchPages(final ServiceContext context) throws RepositoryException {
        final List<PageWrapper> components = new ArrayList<>();
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("hst:hst/hst:configurations//element(*, hst:component)", Query.XPATH);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            if (!node.getParent().getPrimaryNodeType().getName().equals("hst:pages")) {
                // only root pages
                continue;
            }
            final PageWrapper wrapper = new PageWrapper();
            wrapper.setName(node.getName());
            wrapper.setPath(node.getPath());
            components.add(wrapper);
        }

        return components;
    }

    public static List<SitemapItemWrapper> fetchSitemapItems(final ServiceContext context) throws RepositoryException {
        final List<SitemapItemWrapper> sitemapItems = new ArrayList<>();
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("hst:hst/hst:configurations//element(*, hst:sitemapitem)", Query.XPATH);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            final String path = node.getPath();
            if (path.contains("hst:workspace") || node.getParent().getPrimaryNodeType().getName().equals("hst:sitemapitem")) {
                // skip workspace items  and child items
                continue;
            }
            if (EXCLUDE_SITEMAP_ITEMS.contains(path)) {
                // exclude defaults
                continue;
            }
            final SitemapItemWrapper wrapper = new SitemapItemWrapper();
            wrapper.setName(node.getName());
            wrapper.setPath(path);
            sitemapItems.add(wrapper);
        }

        return sitemapItems;
    }

    public static List<MenuItemWrapper> fetchMenuItems(final ServiceContext context) throws RepositoryException {
        final List<MenuItemWrapper> menuItems = new ArrayList<>();
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("hst:hst/hst:configurations//element(*, hst:sitemenuitem)", Query.XPATH);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            final String path = node.getPath();
            final MenuItemWrapper wrapper = new MenuItemWrapper();
            wrapper.setName(node.getName());
            wrapper.setPath(path);
            menuItems.add(wrapper);
        }
        return menuItems;
    }

    public static List<MenuWrapper> fetchMenus(final ServiceContext context) throws RepositoryException {
        final List<MenuWrapper> menus = new ArrayList<>();
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("hst:hst/hst:configurations//element(*, hst:sitemenu)", Query.XPATH);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            final String path = node.getPath();
            final MenuWrapper wrapper = new MenuWrapper();
            wrapper.setName(node.getName());
            wrapper.setPath(path);
            menus.add(wrapper);
        }
        return menus;
    }

    public static List<MountWrapper> fetchMounts(final ServiceContext context) throws RepositoryException {
        final List<MountWrapper> mounts = new ArrayList<>();
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("hst:hst/hst:hosts//element(*, hst:mount)", Query.XPATH);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            final String path = node.getPath();
            final MountWrapper wrapper = new MountWrapper();
            wrapper.setName(node.getName());
            wrapper.setPath(path);
            mounts.add(wrapper);
        }
        return mounts;
    }

    public static List<SiteWrapper> fetchSites(final ServiceContext context) throws RepositoryException {
        final List<SiteWrapper> sites = new ArrayList<>();
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("hst:hst/hst:sites//element(*, hst:site)", Query.XPATH);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            final String path = node.getPath();
            final SiteWrapper wrapper = new SiteWrapper();
            wrapper.setName(node.getName());
            wrapper.setPath(path);
            sites.add(wrapper);
        }
        return sites;
    }


    private static List<YamlWrapper> fetchYamFiles(final ServiceContext context) throws IOException {
        final List<Path> projectFiles = getAllProjectFiles(INCLUDED_FILE_EXTENSIONS, context);

        final List<YamlWrapper> files = new ArrayList<>();
        for (Path projectFile : projectFiles) {
            final File file = projectFile.toFile();
            final String name = file.getName();
            if (EXCLUDED_FILES.contains(name)) {
                log.debug("Excluding name from project files: {}", name);
                continue;
            }
            final String fileExtension = com.google.common.io.Files.getFileExtension(file.getAbsolutePath());
            if ("yaml".equals(fileExtension)) {
                files.add(new YamlWrapper(name, file.getAbsolutePath()));
            }

        }
        return files;
    }

    private static List<YamlBinaryWrapper> fetchYamBinaryFiles(final ServiceContext context) throws IOException {
        final List<Path> projectFiles = getAllProjectFiles(INCLUDED_FILE_EXTENSIONS, context);
        final String root = context.projectService.getBasePathForModule(Module.REPOSITORY_DATA).toFile().getAbsolutePath();

        final List<YamlBinaryWrapper> files = new ArrayList<>();
        for (Path projectFile : projectFiles) {
            final File file = projectFile.toFile();
            final String name = file.getName();
            if (!file.getAbsolutePath().startsWith(root)) {
                log.debug("Excluding name from binary yaml files: {}", name);
                continue;
            }

            if (EXCLUDED_FILES.contains(name)) {
                log.debug("Excluding name from binary yaml files: {}", name);
                continue;
            }
            final String fileExtension = '.' + com.google.common.io.Files.getFileExtension(file.getAbsolutePath());
            if (BINARY_EXTENSIONS.contains(fileExtension)) {
                files.add(new YamlBinaryWrapper(name, file.getAbsolutePath()));
            }

        }
        return files;
    }

    private static List<DependencyWrapper> fetchDependencies(final ServiceContext context) {
        final ProjectService projectService = context.projectService;
        final List<DependencyWrapper> dependencyWrappers = new ArrayList<>();
        final List<Module> modules = ImmutableList.of(Module.PROJECT, Module.SITE, Module.CMS);
        for (Module module : modules) {
            final File file = projectService.getPomPathForModule(module).toFile();
            if (file != null) {
                final Model model = MavenModelUtils.readPom(file);
                if (model != null) {
                    final List<Dependency> dependencies = model.getDependencies();
                    final DependencyManagement dependencyManagement = model.getDependencyManagement();
                    if (dependencyManagement != null) {
                        final List<Dependency> managementDependencies = dependencyManagement.getDependencies();
                        if (managementDependencies != null) {
                            dependencies.addAll(managementDependencies);
                        }
                    }


                    for (Dependency dependency : dependencies) {
                        final DependencyWrapper wrapper = new DependencyWrapper();
                        // angular tracking id
                        final String name = dependency.getGroupId() + " : " + dependency.getArtifactId() + " (" + module.getName() + ')';
                        wrapper.setName(name);
                        wrapper.setId(hash(name));
                        wrapper.setArtifactId(dependency.getArtifactId());
                        wrapper.setGroupId(dependency.getGroupId());
                        wrapper.setScope(dependency.getScope());
                        wrapper.setVersion(dependency.getVersion());
                        wrapper.setModule(module);
                        dependencyWrappers.add(wrapper);
                    }

                }
            }
        }


        return dependencyWrappers;
    }

    private static String hash(final String value) {
        if (value == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(value.getBytes());
    }

    private static String extractEssentialsVersion(final ServiceContext context) {
        final Module module = Module.PROJECT;
        final File pom = context.projectService.getPomPathForModule(Module.PROJECT).toFile();
        final Model model = MavenModelUtils.readPom(pom);
        if (model != null) {
            final Properties properties = model.getProperties();
            if (properties != null) {
                final String version = (String) properties.get("essentials.version");
                if (!Strings.isNullOrEmpty(version)) {
                    log.info("Found essentials version: {}", version);
                    return version;
                }

            }
        }

        log.error("Unable to load model for pom.xml of module '{}'.", module.getName());
        return DEFUALT_ESSENTIALS_VERSION;
    }

    public static List<WebFileWrapper> fetchWebFiles(final ServiceContext context) throws RepositoryException {
        // /webfiles
        final QueryManager queryManager = context.session.getWorkspace().getQueryManager();
        @SuppressWarnings("deprecation")
        final Query query = queryManager.createQuery("webfiles//element(*, nt:file)", Query.XPATH);
        final QueryResult queryResult = query.execute();
        final NodeIterator nodes = queryResult.getNodes();
        final List<WebFileWrapper> files = new ArrayList<>();
        while (nodes.hasNext()) {
            final Node node = nodes.nextNode();
            files.add(new WebFileWrapper(node.getName(), node.getPath()));
        }
        return files;
    }

    public static List<FileWrapper> fetchFiles(final ServiceContext context) throws IOException {
        final List<Path> projectFiles = getProjectFiles(INCLUDED_FILE_EXTENSIONS, context);

        final List<FileWrapper> files = new ArrayList<>();
        for (Path projectFile : projectFiles) {
            final File file = projectFile.toFile();
            final String name = file.getName();
            if (EXCLUDED_FILES.contains(name)) {
                log.debug("Excluding name from project files: {}", name);
                continue;
            }
            files.add(new FileWrapper(name, file.getAbsolutePath()));
        }
        return files;
    }
}
