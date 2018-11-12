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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hippoecm.repository.api.HippoSession;
import org.onehippo.cms7.essentials.essentializer.rest.data.*;
import org.onehippo.cms7.essentials.essentializer.rest.instructions.InstructionData;
import org.onehippo.cms7.essentials.plugin.sdk.instruction.CndInstruction;
import org.onehippo.cms7.essentials.plugin.sdk.instruction.ExecuteInstruction;
import org.onehippo.cms7.essentials.plugin.sdk.instruction.FileInstruction;
import org.onehippo.cms7.essentials.plugin.sdk.instruction.FreemarkerInstruction;
import org.onehippo.cms7.essentials.plugin.sdk.instruction.MavenDependencyInstruction;
import org.onehippo.cms7.essentials.plugin.sdk.instruction.PluginInstructionSet;
import org.onehippo.cms7.essentials.plugin.sdk.instruction.PluginInstructions;
import org.onehippo.cms7.essentials.plugin.sdk.instruction.XmlInstruction;
import org.onehippo.cms7.essentials.plugin.sdk.utils.GlobalUtils;
import org.onehippo.cms7.essentials.plugin.sdk.utils.MavenModelUtils;
import org.onehippo.cms7.essentials.plugin.sdk.utils.TemplateUtils;
import org.onehippo.cms7.essentials.sdk.api.install.Instruction;
import org.onehippo.cms7.essentials.sdk.api.model.Module;
import org.onehippo.cms7.essentials.sdk.api.model.rest.PluginDescriptor;
import org.onehippo.cms7.essentials.sdk.api.model.rest.UserFeedback;
import org.onehippo.cms7.essentials.sdk.api.service.PlaceholderService;
import org.onehippo.cms7.essentials.sdk.api.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.util.ImmutableMap;

import static java.util.Comparator.comparingInt;
import static org.onehippo.cms7.essentials.essentializer.rest.EssentializerUtils.BINARY_EXTENSIONS;
import static org.onehippo.cms7.essentials.essentializer.rest.EssentializerUtils.EXCLUDED_KEYS;
import static org.onehippo.cms7.essentials.essentializer.rest.EssentializerUtils.INCLUDED_FILE_EXTENSIONS;
import static org.onehippo.cms7.essentials.essentializer.rest.EssentializerUtils.getProjectFiles;
import static org.onehippo.cms7.essentials.essentializer.rest.instructions.InstructionData.TYPE;
import static org.onehippo.cms7.essentials.essentializer.rest.instructions.InstructionData.TYPE.*;
import static org.onehippo.cms7.essentials.plugin.sdk.utils.GlobalUtils.readStreamAsText;

public final class WriteUtils {

    private static final Logger log = LoggerFactory.getLogger(WriteUtils.class);

    public static final String FS = File.separator;
    public static final String TPL_POM = "template_pom.xml";
    public static final String TPL_POM_ENTERPRISE = "template_pom_enterprise.xml";
    public static final String TPL_ICON = "template_icon.png";
    public static final String TPL_WEB_FRAGMENT = "template_web-fragment.xml";
    /*    public static final String TPL_TEMPLATE = "template_template.xml";
        public static final String TPL_TEMPLATE_JSP = "template_template_jsp.xml";*/
    public static final String TPL_PLUGIN_HTML = "template_plugin.html";
    public static final String TPL_PLUGIN_JAVASCRIPT = "template_javascript.js";
    public static final String TPL_PLUGIN_REST = "template_java_rest.txt";
    public static final String TPL_PLUGIN_CONTEXT = "template_java_context.txt";
    public static final String TPL_PLUGIN_DATA = "template_java_data.txt";
    public static final String TPL_INSTRUCTION = "template_shared_instruction_java.txt";
    public static final String TPL_INSTRUCTION_VERSION = "template_java_versions.txt";
    // directories
    public static final String DIR_SRC = "src";
    public static final String DIR_JAVA = DIR_SRC + FS + "main" + FS + "java";
    public static final String DIR_RESOURCES = DIR_SRC + FS + "main" + FS + "resources";
    public static final String DIR_TEST = DIR_SRC + FS + "test";
    public static final String DIR_TEST_JAVA = DIR_SRC + FS + "test" + FS + "java";
    public static final String DIR_TEST_RESOURCES = DIR_SRC + FS + "test" + FS + "resources";
    public static final String DIR_META_INF = DIR_RESOURCES + FS + "META-INF";
    public static final String DIR_RESOURCES_META = DIR_META_INF + FS + "resources";
    public static final String DIR_RESOURCES_META_IMAGES = DIR_META_INF + FS + "resources" + FS + "images";
    // placeholders
    public static final String PH_ESSENTIALS_VERSION = "essentialsVersion";
    public static final String PH_PLUGIN_VERSION = "pluginVersion";
    public static final String PH_PLUGIN_NAME = "pluginName";
    public static final String PH_GROUP_ID = "groupId";
    public static final String PH_ARTIFACT_ID = "artifactId";
    public static final String PH_PLUGIN_CONTROLLER = "controllerName";
    public static final String PH_PLUGIN_ID = "pluginId";
    public static final String PH_PLUGIN_DESCRIPTION = "pluginDescription";
    public static final String PH_VERSION_INSTRUCTION_DATA = "versionInstructionData";
    private static final Pattern INVALID_CHARS_PATTERN = Pattern.compile("_-");
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([\\w.]+);");

    private static final String ACTION_COPY = "copy";
    private static final Pattern PATTERN_VERSION = Pattern.compile("\\$\\{(.*)}");

    public static UserFeedback createPlugin(final ServiceContext context) throws Exception {
        final File root = createDirectory(context.data.getTargetDirectory() + FS + context.data.getArtifactId());
        log.info("Creating plugin in: {}", root.getAbsolutePath());
        // add plugin skeleton:
        createSkeleton(context, root.getAbsolutePath());
        // write component resources:

        return new UserFeedback().addSuccess("Successfully created plugin: " + context.data.getPluginName());
    }

    private static void createSkeleton(final ServiceContext context, final String rootPath) throws Exception {

        final Set<InstructionData> instructions = new HashSet<>();
        // pom
        writePom(rootPath, context);
        // icon
        writeIcon(rootPath, context);
        // plugin descriptor:
        writePluginDescriptor(rootPath, context);
        // web-fragment
        writeWebFragment(rootPath, context);
        // html/js file
        writeInterface(rootPath, context);
        // java file
        writeRestResource(rootPath, context);
        // component
        writeCatalogComponents(instructions, context);
        // document types
        writeDocumentTypes(instructions, context);
        // write templates
        writeTemplates(instructions, context);
        // webfiles
        writeWebfiles(instructions, context);
        // write site files
        writeFiles(instructions, context);
        // write pages
        writeHstPages(instructions, context);
        // write sitemap items
        writeHstSitemap(instructions, context);
        writeHstSites(instructions, context);
        writeHstMounts(instructions, context);
        // content
        writeRepositoryXml(instructions, context);
        // menu items
        writeHstMenuItems(instructions, context);
        // menu
        writeHstMenus(instructions, context);
        // component
        writeHstComponents(instructions, context);
        writeHstPageContainers(instructions, context);
        // dependencies
        writeDependencies(instructions, DEPENDENCY, context.data.getSelectedDependencies(), context);
        // shared dependencies
        writeDependencies(instructions, SHARED_DEPENDENCY, context.data.getSelectedSharedDependencies(), context);
        // yaml files
        writeYamlFiles(instructions, context);
        // yaml deps
        writeYamBinaryFiles(instructions, context);

        // process collected instructions:
        processInstructions(instructions, rootPath, context);
    }

    private static void writeYamlFiles(final Set<InstructionData> instructions, final ServiceContext context) {
        final List<YamlWrapper> files = context.data.getSelectedYamlFiles();
        if (files != null) {
            for (YamlWrapper file : files) {
                final String path = file.getPath();
                final InstructionData instructionData = new InstructionData(YAML_FILE, path);
                final File ourFile = new File(path);
                instructionData.setName(ourFile.getName());
                final String text = GlobalUtils.readTextFile(ourFile.toPath()).toString();
                final String data = processProjectFile(context, text);
                instructionData.setData(data);
                instructions.add(instructionData);
            }
        }
    }

    private static void writeYamBinaryFiles(final Set<InstructionData> instructions, final ServiceContext context) {
        final List<YamlBinaryWrapper> files = context.data.getSelectedYamlBinaryFiles();
        if (files != null) {
            for (YamlBinaryWrapper file : files) {
                final String path = file.getPath();
                final InstructionData instructionData = new InstructionData(YAML_BINARY_FILE, path);
                instructionData.setName(new File(path).getName());
                instructions.add(instructionData);
            }
        }
    }

    private static void writeHstComponents(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {
        final List<ComponentWrapper> components = context.data.getSelectedComponents();
        if (components != null) {
            for (ComponentWrapper component : components) {
                final Node node = context.session.getNode(component.getPath());
                final String xml = processProjectFile(context, getNodeAsXml(node, true));
                final InstructionData instructionData = new InstructionData(HST_COMPONENT, component.getPath(), xml);
                final String name = node.getName();
                instructionData.setName(name);
                instructions.add(instructionData);
            }
        }
    }

    private static void writeHstPageContainers(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {
        final List<PageContainerWrapper> containers = context.data.getSelectedPageContainers();
        if (containers != null) {
            for (PageContainerWrapper container : containers) {
                final Node node = context.session.getNode(container.getPath());
                final String xml = processProjectFile(context, getNodeAsXml(node, true));
                final InstructionData instructionData = new InstructionData(HST_PAGE_CONTAINER, container.getPath(), xml);
                final String name = node.getName();
                instructionData.setName(name);
                instructions.add(instructionData);
            }
        }
    }

    private static void writeDependencies(final Set<InstructionData> instructions, final TYPE type, final List<DependencyWrapper> dependencies, final ServiceContext context) {

        if (dependencies != null) {
            for (DependencyWrapper dependency : dependencies) {
                final InstructionData instructionData = new InstructionData(type, dependency.getName());
                instructionData.setRawData(dependency);
                instructions.add(instructionData);
                // check version $
                final String version = dependency.getVersion();
                if (!Strings.isNullOrEmpty(version) && version.startsWith("${")) {
                    // strip ${essentials.version} to essentials.version
                    final String versionName = PATTERN_VERSION.matcher(version).replaceAll("$1");
                    final String value = extractProperty(versionName, context);
                    if (!Strings.isNullOrEmpty(value)) {
                        context.versionInstructionData.put(versionName, value);
                    }

                }
            }
        }
    }

    private static String extractProperty(final String version, final ServiceContext context) {
        final ProjectService projectService = context.projectService;
        final Module[] modules = new Module[]{Module.PROJECT, Module.CMS, Module.SITE};
        for (Module module : modules) {
            final File pom = projectService.getPomPathForModule(module).toFile();
            final Model pomModel = MavenModelUtils.readPom(pom);
            final Properties properties = pomModel.getProperties();
            if (properties != null) {
                final String property = properties.getProperty(version, null);
                if (property != null) {
                    return property;
                }
            }
        }
        return null;
    }

    private static void writeHstPages(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {
        final List<PageWrapper> pages = context.data.getSelectedPages();
        if (pages != null) {
            for (PageWrapper page : pages) {
                final Node node = context.session.getNode(page.getPath());
                final String xml = processProjectFile(context, getNodeAsXml(node, true));
                final InstructionData instructionData = new InstructionData(HST_PAGE, page.getPath(), xml);
                final String name = node.getName();
                instructionData.setName(name);
                instructions.add(instructionData);
            }
        }
    }

    private static void writeHstSitemap(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {

        final List<SitemapItemWrapper> items = context.data.getSelectedSitemapItems();
        if (items != null) {
            for (SitemapItemWrapper item : items) {
                final Node node = context.session.getNode(item.getPath());
                final InstructionData instructionData = new InstructionData(HST_SITEMAP_ITEM, item.getPath(), getNodeAsXml(node, true));
                final String name = node.getName();
                instructionData.setName(name);
                instructions.add(instructionData);
            }
        }
    }

    private static void writeHstSites(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {

        final List<SiteWrapper> items = context.data.getSelectedSites();
        if (items != null) {
            for (SiteWrapper item : items) {
                final Node node = context.session.getNode(item.getPath());
                final InstructionData instructionData = new InstructionData(HST_SITE, item.getPath(), getNodeAsXml(node, true));
                final String name = node.getName();
                instructionData.setName(name);
                instructions.add(instructionData);
            }
        }
    }

    private static void writeHstMounts(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {

        final List<MountWrapper> items = context.data.getSelectedMounts();
        if (items != null) {
            for (MountWrapper item : items) {
                final Node node = context.session.getNode(item.getPath());
                final InstructionData instructionData = new InstructionData(HST_MOUNT, item.getPath(), getNodeAsXml(node, true));
                final String name = node.getName();
                instructionData.setName(name);
                instructions.add(instructionData);
            }
        }
    }

    private static void writeRepositoryXml(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {

        final List<ContentWrapper> items = context.data.getSelectedContent();
        if (items != null) {
            for (ContentWrapper item : items) {
                final Node node = context.session.getNode(item.getPath());
                final InstructionData instructionData = new InstructionData(REPOSITORY_XML, item.getPath(), processProjectFile(context, getNodeAsXml(node, true)));
                final String name = node.getName();
                instructionData.setName(name);
                instructions.add(instructionData);
            }
        }
    }


    private static void writeHstMenuItems(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {

        final List<MenuItemWrapper> items = context.data.getSelectedMenuItems();
        if (items != null) {
            for (MenuItemWrapper item : items) {
                final Node node = context.session.getNode(item.getPath());
                final InstructionData instructionData = new InstructionData(HST_MENU_ITEM, item.getPath(), getNodeAsXml(node, true));
                final String name = node.getName();
                instructionData.setName(name);
                instructions.add(instructionData);
            }
        }
    }

    private static void writeHstMenus(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {

        final List<MenuWrapper> items = context.data.getSelectedMenus();
        if (items != null) {
            for (MenuWrapper item : items) {
                final Node node = context.session.getNode(item.getPath());
                final InstructionData instructionData = new InstructionData(HST_MENU, item.getPath(), getNodeAsXml(node, true));
                final String name = node.getName();
                instructionData.setName(name);
                instructions.add(instructionData);
            }
        }
    }

    private static void writeFiles(final Set<InstructionData> instructions, final ServiceContext context) {

        final List<FileWrapper> files = context.data.getSelectedFiles();
        if (files != null) {
            for (FileWrapper file : files) {
                final TYPE type = getFileType(file.getPath(), context);
                instructions.add(new InstructionData(type, file.getPath(), file.getName()));
            }
        }
    }

    private static TYPE getFileType(final String path, final ServiceContext context) {

        //TODO add more project files
        return PROJECT_FILE;
    }


    private static void processInstructions(final Set<InstructionData> instructions, final String rootPath, final ServiceContext context) throws Exception {
        final PluginInstructionSet set = new PluginInstructionSet();
        for (InstructionData instruction : instructions) {
            final TYPE type = instruction.getType();
            switch (type) {
                case SITE_BINARY:
                    copyBinaryFile(set, instruction, rootPath, Module.SITE, context);
                    break;
                case SITE_JAVA:
                    copyJavaFile(set, instruction, context, rootPath);
                    break;
                case FTL_WEBFILE:
                    copyFtlWebFile(set, instruction, rootPath);
                    break;
                case SITE_TEXT:
                    log.error("Processing: {}", instruction);
                    break;
                case PROJECT_FILE:
                    copyProjectFile(set, instruction, context, rootPath);
                    break;
                case DOCUMENT_TYPE_XML:
                    copyDocumentType(set, instruction, rootPath);
                    break;
                case REPOSITORY_XML:
                    copyRepositoryXml(context, set, instruction, rootPath);
                    break;
                case DEPENDENCY:
                    copyDependency(set, instruction, context);
                    break;
                case SHARED_DEPENDENCY:
                    copyDependency(set, instruction, context);
                    copySharedDependency(set, rootPath, instruction, context);
                    break;
                case BINARY_WEBFILE:
                    copyWebfile(set, instruction, context, rootPath, BINARY_WEBFILE);
                    break;
                case HST_SITEMAP_ITEM:
                    copyHstSitemapItem(set, instruction, context, rootPath);
                    break;
                case HST_SITE:
                    copyHstSite(set, instruction, context, rootPath);
                    break;
                case HST_MOUNT:
                    copyHstMount(set, instruction, context, rootPath);
                    break;
                case HST_MENU_ITEM:
                    copyHstMenuItem(set, instruction, context, rootPath);
                    break;
                case HST_MENU:
                    copyHstMenu(set, instruction, context, rootPath);
                    break;
                case WEBFILE:
                    copyWebfile(set, instruction, context, rootPath, WEBFILE);
                    break;
                case TEMPLATE_XML:
                    copyTemplateXml(set, instruction, context, rootPath);
                    break;
                case CATALOG_COMPONENT_XML:
                    copyCatalogComponentXmlFile(set, instruction, rootPath, context);
                    break;
                case HST_PAGE:
                    copyHstPage(set, instruction, rootPath, context);
                    break;
                case HST_COMPONENT:
                    copyHstComponent(set, instruction, rootPath, context);
                    break;
                case HST_PAGE_CONTAINER:
                    copyHstPageContainer(set, instruction, rootPath, context);
                    break;
                case YAML_FILE:
                    copyYamlFile(set, instruction, rootPath, context);
                    break;
                case YAML_BINARY_FILE:
                    copyYamlBinaryFile(set, instruction, rootPath, context);
                    break;
                case UNKNOWN:
                default:
                    log.error("Unknown instruction {}", instruction);

            }
        }

        // check if we have version  processing:
        copyVersions(set, rootPath, context);

        final PluginInstructions pluginInstructions = new PluginInstructions();
        // pre-create groups so we have default order:
        final Set<String> groups = ImmutableSet.of("CND", "documents", "hstMenu", "hstMenuItems", "freemarker", "hstConfiguration", "default", "maven");
        for (String group : groups) {
            createNewInstructionSet(pluginInstructions, group);
        }

        for (Instruction instruction : set.getInstructions()) {
            if (instruction instanceof CndInstruction) {
                final PluginInstructionSet mySet = getSetForGroup(pluginInstructions, "CND");
                mySet.addInstruction(instruction);
            } else if (instruction instanceof MavenDependencyInstruction || instruction instanceof ExecuteInstruction) {
                final PluginInstructionSet mySet = getSetForGroup(pluginInstructions, "maven");
                mySet.addInstruction(instruction);
            } else if (instruction instanceof FreemarkerInstruction) {
                final PluginInstructionSet mySet = getSetForGroup(pluginInstructions, "freemarker");
                mySet.addInstruction(instruction);
            } else if (instruction instanceof XmlInstruction) {
                final String target = ((XmlInstruction) instruction).getTarget();
                String groupName = "xml";
                if (target.contains("/hippo:configuration/hippo:queries/")
                        || target.contains("/hippo:namespaces/")
                        || target.contains("{{beansFolder}}")) {
                    groupName = "documents";
                } else if (target.contains("/content/documents/")) {
                    groupName = "sampleData";
                } else if (target.contains("hst:sitemenus/")) {
                    groupName = "hstMenuItems";
                } else if (target.contains("hst:sitemenus")) {
                    groupName = "hstMenu";
                } else if (target.contains("/hst:hst")) {
                    groupName = "hstConfiguration";
                } else if (target.contains("{{freemarkerRoot}}")) {
                    groupName = "freemarker";
                }


                final PluginInstructionSet mySet = getSetForGroup(pluginInstructions, groupName);
                mySet.addInstruction(instruction);
            } else {
                final PluginInstructionSet mySet = getSetForGroup(pluginInstructions, "default");
                mySet.addInstruction(instruction);
            }

        }
        // remove names:
        final Set<PluginInstructionSet> instructionSets = pluginInstructions.getInstructionSets();
        final Set<PluginInstructionSet> nonEmptySet = instructionSets
                .stream()
                .filter(pluginInstructionSet -> pluginInstructionSet.getInstructions() != null && !pluginInstructionSet.getInstructions().isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (PluginInstructionSet instructionSet : nonEmptySet) {
            final String group = instructionSet.getGroup();
            if (!Strings.isNullOrEmpty(group) && !group.equals("freemarker")) {
                instructionSet.setGroup(null);
            }
        }
        pluginInstructions.setInstructionSets(nonEmptySet);
        writeInstructions(pluginInstructions, rootPath, context);

    }


    private static PluginInstructionSet getSetForGroup(final PluginInstructions pluginInstructions, final String name) {
        final Set<PluginInstructionSet> instructionSets = pluginInstructions.getInstructionSets();
        if (instructionSets == null) {
            return createNewInstructionSet(pluginInstructions, name);
        }
        for (PluginInstructionSet instructionSet : instructionSets) {
            final Set<String> groups = instructionSet.getGroups();
            if (groups != null && groups.contains(name)) {
                return instructionSet;
            }
        }
        return createNewInstructionSet(pluginInstructions, name);
    }

    private static PluginInstructionSet createNewInstructionSet(final PluginInstructions pluginInstructions, final String name) {
        final Set<PluginInstructionSet> newSet = pluginInstructions.getInstructionSets() == null ? new LinkedHashSet<>() : pluginInstructions.getInstructionSets();
        final PluginInstructionSet mySet = new PluginInstructionSet();
        mySet.setGroup(name);
        newSet.add(mySet);
        pluginInstructions.setInstructionSets(newSet);
        return mySet;

    }


    private static void copyYamlFile(final PluginInstructionSet set, final InstructionData instruction, final String rootPath, final ServiceContext context) throws IOException {
        final FileInstruction fileInstruction = new FileInstruction();
        fileInstruction.setAction(ACTION_COPY);
        fileInstruction.setBinary(false);
        final String resourceFolder = "yamlFiles";
        final String subDir = DIR_RESOURCES + FS + resourceFolder;
        final String directory = rootPath + FS + subDir;
        createDirectory(directory);
        final String fileName = instruction.getName();
        final String data = instruction.getData();
        final String target = directory + FS + fileName;
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data.getBytes()), new File(target));

        fileInstruction.setSource(resourceFolder + '/' + fileName);
        fileInstruction.setTarget(processProjectFile(context, instruction.getPath()));
        set.addInstruction(fileInstruction);
    }

    private static void copyYamlBinaryFile(final PluginInstructionSet set, final InstructionData instruction, final String rootPath, final ServiceContext context) throws IOException {
        final FileInstruction fileInstruction = new FileInstruction();
        fileInstruction.setAction(ACTION_COPY);
        fileInstruction.setBinary(true);
        final String resourceFolder = "yamlBinaryFiles";
        final String subDir = DIR_RESOURCES + FS + resourceFolder;
        final String directory = rootPath + FS + subDir;
        createDirectory(directory);
        final String fileName = instruction.getName();
        FileUtils.copyInputStreamToFile(new FileInputStream(instruction.getPath()), new File(directory + FS + fileName));
        fileInstruction.setSource(resourceFolder + '/' + fileName);
        fileInstruction.setTarget(processProjectFile(context, instruction.getPath()));
        set.addInstruction(fileInstruction);
    }


    private static void copyHstPage(final PluginInstructionSet set, final InstructionData instruction, final String rootPath, final ServiceContext context) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        final String resourceFolder = "hstPages";
        final String subDir = DIR_RESOURCES + FS + resourceFolder;
        final String directory = rootPath + FS + subDir;
        createDirectory(directory);
        final String fileName = instruction.getName() + ".xml";
        final String data = instruction.getData();
        final String target = directory + FS + fileName;
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data.getBytes()), new File(target));
        xmlInstruction.setAction(ACTION_COPY);
        xmlInstruction.setSource(resourceFolder + '/' + fileName);
        xmlInstruction.setTarget(TargetUtils.getPageTarget(instruction.getPath()));
        set.addInstruction(xmlInstruction);
    }

    private static void copyHstComponent(final PluginInstructionSet set, final InstructionData instruction, final String rootPath, final ServiceContext context) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        final String resourceFolder = "hstComponents";
        final String subDir = DIR_RESOURCES + FS + resourceFolder;
        final String directory = rootPath + FS + subDir;
        createDirectory(directory);
        final String fileName = instruction.getName() + ".xml";
        final String data = instruction.getData();
        final String target = directory + FS + fileName;
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data.getBytes()), new File(target));
        xmlInstruction.setAction(ACTION_COPY);
        xmlInstruction.setSource(resourceFolder + '/' + fileName);
        xmlInstruction.setTarget(TargetUtils.getComponentTarget(instruction.getPath()));
        set.addInstruction(xmlInstruction);

    }

    private static void copyHstPageContainer(final PluginInstructionSet set, final InstructionData instruction, final String rootPath, final ServiceContext context) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        final String resourceFolder = "hstPageContainers";
        final String subDir = DIR_RESOURCES + FS + resourceFolder;
        final String directory = rootPath + FS + subDir;
        createDirectory(directory);
        final String fileName = instruction.getName() + ".xml";
        final String data = instruction.getData();
        final String target = directory + FS + fileName;
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data.getBytes()), new File(target));
        xmlInstruction.setAction(ACTION_COPY);
        xmlInstruction.setSource(resourceFolder + '/' + fileName);
        xmlInstruction.setTarget(TargetUtils.getContainerTarget(instruction.getPath()));
        set.addInstruction(xmlInstruction);

    }



    private static void copyHstSitemapItem(final PluginInstructionSet set, final InstructionData instruction, final ServiceContext context, final String rootPath) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        final String resourceFolder = "hstSitemapItems";
        final String subDir = DIR_RESOURCES + FS + resourceFolder;
        final String directory = rootPath + FS + subDir;
        createDirectory(directory);
        final String fileName = instruction.getName() + ".xml";
        final String data = instruction.getData();
        final String target = directory + FS + fileName;
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data.getBytes()), new File(target));
        xmlInstruction.setAction(ACTION_COPY);
        xmlInstruction.setSource(resourceFolder + '/' + fileName);
        xmlInstruction.setTarget(TargetUtils.getSitemapTarget(instruction.getPath()));
        set.addInstruction(xmlInstruction);
    }

    private static void copyHstSite(final PluginInstructionSet set, final InstructionData instruction, final ServiceContext context, final String rootPath) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        final String resourceFolder = "hstSite";
        final String subDir = DIR_RESOURCES + FS + resourceFolder;
        final String directory = rootPath + FS + subDir;
        createDirectory(directory);
        final String fileName = instruction.getName() + ".xml";
        final String data = instruction.getData();
        final String target = directory + FS + fileName;
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(processProjectFile(context, data).getBytes()), new File(target));
        xmlInstruction.setAction(ACTION_COPY);
        xmlInstruction.setSource(resourceFolder + '/' + fileName);
        final String myTarget = processProjectFile(context, instruction.getPath());
        xmlInstruction.setTarget(myTarget.substring(0, myTarget.lastIndexOf('/')));
        set.addInstruction(xmlInstruction);
    }


    private static void copyHstMount(final PluginInstructionSet set, final InstructionData instruction, final ServiceContext context, final String rootPath) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        final String resourceFolder = "hstMount";
        final String subDir = DIR_RESOURCES + FS + resourceFolder;
        final String directory = rootPath + FS + subDir;
        createDirectory(directory);
        final String fileName = instruction.getName() + ".xml";
        final String data = instruction.getData();
        final String target = directory + FS + fileName;
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(processProjectFile(context, data).getBytes()), new File(target));
        xmlInstruction.setAction(ACTION_COPY);
        xmlInstruction.setSource(resourceFolder + '/' + fileName);
        final String myTarget = processProjectFile(context, instruction.getPath());
        xmlInstruction.setTarget(myTarget.substring(0, myTarget.lastIndexOf('/')));
        set.addInstruction(xmlInstruction);
    }

    private static void copyHstMenuItem(final PluginInstructionSet set, final InstructionData instruction, final ServiceContext context, final String rootPath) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        final String resourceFolder = "hstMenuItems";
        final String subDir = DIR_RESOURCES + FS + resourceFolder;
        final String directory = rootPath + FS + subDir;
        createDirectory(directory);
        final String fileName = instruction.getName() + ".xml";
        final String data = instruction.getData();
        final String target = directory + FS + fileName;
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data.getBytes()), new File(target));
        xmlInstruction.setAction(ACTION_COPY);
        xmlInstruction.setSource(resourceFolder + '/' + fileName);
        xmlInstruction.setTarget(TargetUtils.getMenuItemTarget(instruction.getPath()));
        set.addInstruction(xmlInstruction);
    }

    private static void copyHstMenu(final PluginInstructionSet set, final InstructionData instruction, final ServiceContext context, final String rootPath) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        final String resourceFolder = "hstMenus";
        final String subDir = DIR_RESOURCES + FS + resourceFolder;
        final String directory = rootPath + FS + subDir;
        createDirectory(directory);
        final String fileName = instruction.getName() + ".xml";
        final String data = instruction.getData();
        final String target = directory + FS + fileName;
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data.getBytes()), new File(target));
        xmlInstruction.setAction(ACTION_COPY);
        xmlInstruction.setSource(resourceFolder + '/' + fileName);
        xmlInstruction.setTarget(TargetUtils.getMenuItemTarget(instruction.getPath()));
        set.addInstruction(xmlInstruction);
    }



    private static void copyDependency(final PluginInstructionSet set, final InstructionData instruction, final ServiceContext context) {
        final DependencyWrapper data = (DependencyWrapper) instruction.getRawData();
        final MavenDependencyInstruction dependencyInstruction = new MavenDependencyInstruction();
        dependencyInstruction.setArtifactId(data.getArtifactId());
        dependencyInstruction.setGroupId(data.getGroupId());
        dependencyInstruction.setTargetPom(data.getModule().getName());
        dependencyInstruction.setScope(data.getScope());
        dependencyInstruction.setVersion(data.getVersion());
        set.addInstruction(dependencyInstruction);
    }

    private static void copyVersions(final PluginInstructionSet set, final String rootPath, final ServiceContext context) throws IOException {
        final Map<String, String> data = context.versionInstructionData;
        final boolean empty = data.isEmpty();
        if (!empty) {
            final ExecuteInstruction versionInstruction = new ExecuteInstruction();
            final String pluginId = context.data.getPluginId();
            final String instructionClazz = "org.onehippo.cms7.essentials." + pluginId + ".instructions.AddVersionsInstruction";
            // create placeholders
            final Map<String, Object> extraPlaceholders = new HashMap<>();
            final String versionData = Joiner.on("\",\"").withKeyValueSeparator("\",\"").join(data);
            extraPlaceholders.put(PH_VERSION_INSTRUCTION_DATA, '"' + versionData + '"');
            log.info("versionData {}", versionData);
            final String instructionPackage = "org.onehippo.cms7.essentials." + pluginId + ".instructions";
            final String packageDir = DOT_PATTERN.matcher(instructionPackage).replaceAll(FS);
            final String directory = rootPath + FS + DIR_JAVA + FS + packageDir;
            createDirectory(directory);
            final Map<String, Object> placeholderData = new HashMap<>(context.placeholderData);
            placeholderData.putAll(extraPlaceholders);
            final String instructionData = replaceResource(TPL_INSTRUCTION_VERSION, placeholderData);
            final String instructionPath = directory + FS + "AddVersionsInstruction.java";
            writeToFile(instructionData, instructionPath);
            versionInstruction.setClazz(instructionClazz);
            set.addInstruction(versionInstruction);

        }


    }

    private static void copySharedDependency(final PluginInstructionSet set, final String rootPath, final InstructionData instruction, final ServiceContext context) throws IOException {
        final DependencyWrapper dependency = (DependencyWrapper) instruction.getRawData();

        final String pluginId = context.data.getPluginId();
        final String instructionPackage = "org.onehippo.cms7.essentials." + pluginId + ".instructions";
        final String essentializerInstructionName = getInstructionSuffix(set);
        final String instructionClazz = "org.onehippo.cms7.essentials." + pluginId + ".instructions.SharedLibraryInstruction" + essentializerInstructionName;
        final String packageDir = DOT_PATTERN.matcher(instructionPackage).replaceAll(FS);
        final String directory = rootPath + FS + DIR_JAVA + FS + packageDir;
        createDirectory(directory);
        // instruction class:
        final ImmutableMap<String, Object> extraPlaceholders = ImmutableMap
                .of(
                        "essentializerInstructionName", essentializerInstructionName,
                        "essentializerArtifactId", dependency.getArtifactId(),
                        "essentializerGroupId", dependency.getGroupId());
        final Map<String, Object> placeholderData = new HashMap<>(context.placeholderData);
        placeholderData.putAll(extraPlaceholders);
        final String instructionData = replaceResource(TPL_INSTRUCTION, placeholderData);
        final String instructionPath = directory + FS + "SharedLibraryInstruction" + essentializerInstructionName + ".java";
        writeToFile(instructionData, instructionPath);
        final ExecuteInstruction executeInstruction = new ExecuteInstruction();
        set.addInstruction(executeInstruction);
        executeInstruction.setClazz(instructionClazz);

    }


    private static String getInstructionSuffix(final PluginInstructionSet set) {
        final Set<Instruction> instructions = set.getInstructions();
        final long count = instructions.stream().filter(instruction -> instruction instanceof ExecuteInstruction).count();
        final String[] suffixNames = {"", "One", "Two", "Three",
                "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
                "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
                "Sixteen", "Seventeen", "Eighteen", "Nineteen", "Twenty"};
        return count >= suffixNames.length ? String.valueOf(count) : suffixNames[(int) count];
    }


    private static void copyProjectFile(final PluginInstructionSet set, final InstructionData instruction, final ServiceContext context, final String rootPath) throws IOException {

        final String fileName = instruction.getData();
        final String filePath = instruction.getPath();
        final FileInstruction fileInstruction = new FileInstruction();

        //final String subPath = filePath.substring(modulePath.length());
        final String target = findBestFileReplacement(context, filePath);
        final String cmsModule = context.projectService.getBasePathForModule(Module.CMS).toString();
        final boolean isCms = filePath.startsWith(cmsModule);
        final String projectFiles = isCms ? "cmsFiles" : "siteFiles";
        final String subDir = DIR_RESOURCES + FS + projectFiles;
        final String directory = rootPath + FS + subDir;
        fileInstruction.setSource(projectFiles + '/' + fileName);
        fileInstruction.setTarget(target);
        final boolean binary = isBinary(filePath);
        final String targetFile = directory + FS + fileName;
        if (!binary) {
            final StringBuilder builder = GlobalUtils.readTextFile(new File(filePath).toPath());
            final String rawText = builder.toString();
            // TODO improve this e.g. some files might need namespace replacement
            final String text = isCms ? rawText : processProjectFile(context, rawText);
            FileUtils.copyInputStreamToFile(new ByteArrayInputStream(text.getBytes()), new File(targetFile));

        } else {
            FileUtils.copyFile(new File(filePath), new File(targetFile));
        }
        set.addInstruction(fileInstruction);

    }

/*    private static String fixJavaPath(final String target, final ServiceContext context) {
        final int idx = context.projectService.getBeansRootPath().toString().length()  + 1;
        final String beansPath = context.projectService.getBeansPackagePath().toString().substring(idx);
        final String componentsPath = context.projectService.getComponentsPackagePath().toString().substring(idx);
        final String restPath = context.projectService.getRestPackagePath().toString().substring(idx);
        final Comparator<Pair<String, String>> lengthComparator = comparingInt(o -> o.getRight().length());

        final List<Pair<String,String>> paths = new ArrayList<>();
        paths.add(new ImmutablePair<>("{{componentsPackage}}",componentsPath));
        paths.add(new ImmutablePair<>("{{beansPackage}}",beansPath));
        paths.add(new ImmutablePair<>("{{restPackage}}",restPath));

        paths.sort(lengthComparator);
        String ourTarget = target;
        for (Pair<String, String> path : paths) {
            ourTarget = target.replaceAll(path.getRight(), path.getLeft());
        }

        return ourTarget;
    }*/


    /**
     * NOTE: 2-pass replacement is needed because we can have prefix and suffix replacement
     */
    private static String processProjectFile(final ServiceContext context, String text) {
        final String firstProcess = processProjectFileOnce(context, text);
        return processProjectFileOnce(context, firstProcess);
    }

    private static String processProjectFileOnce(final ServiceContext context, String text) {
        final Map<String, Object> placeholderData = context.placeholderData;
        final Comparator<Map.Entry<String, String>> comparingByValue =
                (o1, o2) -> {
                    final int first = o1.getValue().length();
                    final int second = o2.getValue().length();
                    return Integer.compare(second, first);
                };

        final Map<String, String> strings = placeholderData
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));

        final Map<String, String> sorted = strings
                .entrySet()
                .stream()
                .sorted(comparingByValue)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));


        // do replacements by matching longest possible value:
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            final String key = entry.getKey();
            if (EXCLUDED_KEYS.contains(key)) {
                continue;
            }
            final String value = entry.getValue();
            text = text.replaceAll(value, "{{" + key + "}}");
        }
        return text;
    }


    private static String findBestFileReplacement(final ServiceContext context, final String path) {
        final ProjectService projectService = context.projectService;
        final Map<String, Object> placeholderData = context.placeholderData;
        final Comparator<String> lengthComparator = comparingInt(String::length).reversed();
        final List<String> paths = new ArrayList<>();
        for (Map.Entry<String, Object> entry : placeholderData.entrySet()) {
            paths.add(entry.getValue().toString());
        }
        paths.sort(lengthComparator);
        String bestMatch = null;
        for (String p : paths) {
            if (path.startsWith(p)) {
                bestMatch = p;
                break;
            }
        }
        if (Strings.isNullOrEmpty(bestMatch)) {
            return bestPathForModule(context, path, projectService);
        }
        // we matched a placeholder:
        for (Map.Entry<String, Object> entry : placeholderData.entrySet()) {
            final String entryValue = entry.getValue().toString();
            if (bestMatch.equals(entryValue)) {
                final String key = entry.getKey();
                final String suffix = path.substring(bestMatch.length() + 1);
                log.debug("suffix {}", suffix);
                log.debug("key {}", key);
                return "{{" + key + "}}/" + suffix;
            }
        }
        return bestPathForModule(context, path, projectService);
    }

    private static String bestPathForModule(final ServiceContext context, final String path, final ProjectService projectService) {
        final Module module = TargetUtils.getFileTarget(path, context);
        final String modulePath = projectService.getBasePathForModule(module).toString();
        final String subPath = path.substring(modulePath.length());
        // siteRoot, cmsRoot etc.
        return "{{" + module.getName() + "Root}}" + subPath;
    }



    private static void writeWebfiles(final Set<InstructionData> instructions, final ServiceContext context) {

        final List<WebFileWrapper> webFiles = context.data.getSelectedWebFiles();
        if (webFiles != null) {
            for (WebFileWrapper webFile : webFiles) {
                instructions.add(new InstructionData(getFtlWebfileType(webFile.getPath()), webFile.getPath()));
            }
        }

    }


    public static boolean isBinary(final String path) {
        final String extension = '.' + FilenameUtils.getExtension(path);
        return BINARY_EXTENSIONS.contains(extension);

    }


    private static TYPE getFtlWebfileType(final String path) {
        // we might introduce more types...
        if (path.indexOf('.') > -1) {
            final String ext = path.substring(path.lastIndexOf('.'));
            if (BINARY_EXTENSIONS.contains(ext)) {
                return BINARY_WEBFILE;
            }
        }
        return WEBFILE;
    }


    private static void copyWebfile(final PluginInstructionSet set, final InstructionData instruction, final ServiceContext context, final String rootPath, final TYPE type) throws Exception {
        final String resourceDirectory = "webfiles";
        final Map<String, Object> placeholderData = context.placeholderData;
        final FileInstruction fileInstruction = new FileInstruction();
        final boolean binary = type == BINARY_WEBFILE;
        fileInstruction.setAction(ACTION_COPY);

        final String path = instruction.getPath();
        fileInstruction.setTarget(TargetUtils.getWebfileTarget(context, path));
        fileInstruction.setBinary(binary);

        final String fileName = path.substring(path.lastIndexOf('/') + 1);

        final String subDir = DIR_RESOURCES + FS + resourceDirectory;
        final String directory = rootPath + FS + subDir;
        fileInstruction.setSource(resourceDirectory + '/' + fileName);
        final String targetFile = directory + FS + fileName;
        final Node node = context.session.getNode(path);
        if (!binary) {
            final String text = TemplateUtils.replaceTemplateData(extractJcrContent(node), placeholderData);
            FileUtils.copyInputStreamToFile(new ByteArrayInputStream(text.getBytes()), new File(targetFile));
        } else {
            FileUtils.copyInputStreamToFile(extractJcrStream(node), new File(targetFile));
        }
        set.addInstruction(fileInstruction);
    }




    private static void copyFtlWebFile(final PluginInstructionSet set, final InstructionData instruction, final String rootPath) throws IOException {
        final String resourceDirectory = "freemarker";
        final String path = instruction.getPath();
        final FreemarkerInstruction freemarker = new FreemarkerInstruction();
        freemarker.setAction(ACTION_COPY);
        final String fileName = path.substring(path.lastIndexOf('/'));
        final String source = resourceDirectory + fileName;
        freemarker.setSource(source);
        freemarker.setTarget(TargetUtils.getFreemarkerTarget(path));
        log.info("instruction {}", instruction);
        set.addInstruction(freemarker);
        // copy file:
        final String directory = rootPath + FS + DIR_RESOURCES + FS + resourceDirectory;
        createDirectory(directory);
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(instruction.getData().getBytes()), new File(directory + FS + fileName));
    }



    private static void copyTemplateXml(final PluginInstructionSet set, final InstructionData instruction, final ServiceContext context, final String rootPath) throws IOException {
        final String data = instruction.getData();
        final XmlInstruction xmlInstruction = new XmlInstruction();
        xmlInstruction.setAction(ACTION_COPY);
        final String path = instruction.getPath();
        final String webfileTarget = TargetUtils.getTemplateTarget(path);
        xmlInstruction.setTarget(webfileTarget);
        // source:
        String xml = data;
        final String xpath = "/sv:node/sv:property[@sv:name = 'hst:renderpath']/sv:value";
        final String renderPath = getXmlNodeValue(data, xpath);
        if (!Strings.isNullOrEmpty(renderPath)) {
            final String newPath = TargetUtils.getNamespacedRenderPath(renderPath);
            xml = replaceValue(xml, xpath, newPath);
        }
        // create xml file:
        final String fileName = path.substring(path.lastIndexOf('/') + 1) + ".xml";
        final String sourceDirectory = "templates";
        xmlInstruction.setSource(sourceDirectory + '/' + fileName);
        final String directory = rootPath + FS + DIR_RESOURCES + FS + sourceDirectory;
        final String targetPath = directory + FS + fileName;
        createDirectory(directory);
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(xml.getBytes()), new File(targetPath));
        set.addInstruction(xmlInstruction);


        set.addInstruction(xmlInstruction);
    }



    private static void writeTemplates(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {
        final List<TemplateWrapper> templates = context.data.getSelectedTemplates();
        if (templates != null) {
            for (TemplateWrapper template : templates) {
                final String path = template.getPath();
                final InstructionData xmlInstructionData = new InstructionData(TEMPLATE_XML, path);
                final Node node = context.session.getNode(path);
                log.info("path {}", path);
                log.info("template {}", template);
                final String xml = getNodeAsXml(node, true);
                xmlInstructionData.setData(xml);
                instructions.add(xmlInstructionData);
                // add file instruction:
                final String templatePath = node.getProperty("hst:renderpath").getString();
                // TODO JSP stuff
                final String prefix = "webfile:/";
                if (templatePath.startsWith(prefix)) {
                    final String ftlTemplatePath = "/webfiles/site/" + templatePath.substring(prefix.length());
                    final InstructionData webfileData = new InstructionData(FTL_WEBFILE, ftlTemplatePath);
                    final String ftl = extractJcrContent(context.session.getNode(ftlTemplatePath));
                    webfileData.setData(ftl);
                    instructions.add(webfileData);
                }
            }
        }
    }

    private static void copyRepositoryXml(final ServiceContext context, final PluginInstructionSet set, final InstructionData instruction, final String rootPath) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        xmlInstruction.setAction(ACTION_COPY);
        final String sourceDirectory = getDirForPath(instruction.getPath());
        final String path = instruction.getPath();
        final String nodeName = path.substring(path.lastIndexOf('/') + 1);
        final String fileName = nodeName + ".xml";

        xmlInstruction.setTarget(processProjectFile(context, path.substring(0, path.lastIndexOf('/'))));
        xmlInstruction.setSource(sourceDirectory + '/' + fileName);
        final String directory = rootPath + FS + DIR_RESOURCES + FS + sourceDirectory;
        final String targetPath = directory + FS + fileName;
        createDirectory(directory);
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(instruction.getData().getBytes()), new File(targetPath));
        set.addInstruction(xmlInstruction);

    }

    private static String getDirForPath(final String path) {

        for (Map.Entry<String, String> entry : EssentializerUtils.CONTENT_PATHS.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "contentDocuments";
    }

    private static void copyDocumentType(final PluginInstructionSet set, final InstructionData instruction, final String rootPath) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        xmlInstruction.setAction(ACTION_COPY);
        final String sourceDirectory = "documentTypes";
        final String path = instruction.getPath();
        final String nodeName = path.substring(path.lastIndexOf('/') + 1);
        final String fileName = nodeName + ".xml";
        xmlInstruction.setTarget("/hippo:namespaces/{{namespace}}");
        xmlInstruction.setSource(sourceDirectory + '/' + fileName);
        final String directory = rootPath + FS + DIR_RESOURCES + FS + sourceDirectory;
        final String targetPath = directory + FS + fileName;
        createDirectory(directory);
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(instruction.getData().getBytes()), new File(targetPath));
        set.addInstruction(xmlInstruction);
        // add CND instructions:
        final String rawData = (String) instruction.getRawData();
        final CndInstruction cndInstruction = new CndInstruction();
        cndInstruction.setSuperType(rawData);
        cndInstruction.setDocumentType(instruction.getName());
        set.addInstruction(cndInstruction);
    }

    private static void writeDocumentTypes(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {
        final List<DocumentTypeWrapper> documentTypes = context.data.getSelectedDocumentTypes();
        if (documentTypes != null) {
            for (DocumentTypeWrapper documentType : documentTypes) {
                final Set<String> replacedSupertypes = new HashSet<>();
                final String[] superTypes = documentType.getSuperTypes();
                for (String superType : superTypes) {
                    replacedSupertypes.add(processProjectFile(context, superType));
                }
                final String path = documentType.getPath();
                final InstructionData instructionData = new InstructionData(DOCUMENT_TYPE_XML, path);
                final String xml = replaceNamespace(getNodeAsXml(context.session.getNode(path), true), context);
                instructionData.setData(xml);
                instructionData.setName(documentType.getName());
                final String superTypeString = Joiner.on(',').join(replacedSupertypes);
                instructionData.setRawData(superTypeString);
                instructions.add(instructionData);
            }
        }
    }


    private static void copyCatalogComponentXmlFile(final PluginInstructionSet set, final InstructionData instruction, final String rootPath, final ServiceContext context) throws IOException {
        final XmlInstruction xmlInstruction = new XmlInstruction();
        xmlInstruction.setAction(ACTION_COPY);
        final String sourceDirectory = "xml";
        final String path = instruction.getPath();
        final String nodeName = path.substring(path.lastIndexOf('/') + 1);
        final String fileName = nodeName + ".xml";
        String target = TargetUtils.getHstConfigurationTarget(path, nodeName);
        xmlInstruction.setTarget(target);
        xmlInstruction.setSource(sourceDirectory + '/' + fileName);
        final String directory = rootPath + FS + DIR_RESOURCES + FS + sourceDirectory;
        final String targetPath = directory + FS + fileName;
        createDirectory(directory);
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(instruction.getData().getBytes()), new File(targetPath));
        set.addInstruction(xmlInstruction);
    }



    private static void copyJavaFile(final PluginInstructionSet set, final InstructionData instruction, final ServiceContext context, final String rootPath) throws IOException {
        final FileInstruction fileInstruction = new FileInstruction();
        fileInstruction.setBinary(false);
        final String path = instruction.getPath();
        final String ourPath = instruction.getPath();
        final String fileName = ourPath.substring(ourPath.lastIndexOf(FS) + 1);
        final String text = GlobalUtils.readTextFile(new File(path).toPath()).toString();
        final String template = processProjectFile(context, PACKAGE_PATTERN.matcher(text).replaceAll(Matcher.quoteReplacement("package {{" + PlaceholderService.COMPONENTS_PACKAGE + "}};")));

        final String sourceDirectory = getFolderForPlaceholder(PlaceholderService.COMPONENTS_PACKAGE);
        final String directory = rootPath + FS + DIR_RESOURCES + FS + sourceDirectory;
        createDirectory(directory);
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(template.getBytes()), new File(directory + FS + fileName));
        final String target = "{{" + PlaceholderService.COMPONENTS_PACKAGE + "}}" + '/' + fileName;

        final String source = sourceDirectory + '/' + fileName;
        fileInstruction.setTarget(target);
        fileInstruction.setSource(source);
        set.addInstruction(fileInstruction);
    }


    public static void writeInstructions(final PluginInstructions instructions, final String rootPath, final ServiceContext context) throws IOException {
        final String instructionFileName = context.data.getPluginId() + "_instructions.xml";
        final String directory = rootPath + FS + DIR_META_INF;
        createDirectory(directory);
        try {
            // serialize to file:
            final JAXBContext ctx = JAXBContext.newInstance(PluginInstructionSet.class, PluginInstructions.class);
            final Marshaller m = ctx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            final StringWriter writer = new StringWriter();

            m.marshal(instructions, writer);
            // write to meta inf dir:
            final String target = directory + FS + instructionFileName;
            writeToFile(writer.toString(), target);

        } catch (JAXBException e) {
            log.error("", e);
        }


    }


    private static void copyBinaryFile(final PluginInstructionSet set, final InstructionData instruction, String rootPath, final Module module, final ServiceContext context) throws IOException {
        final String moduleRoot = context.projectService.getBasePathForModule(module).toString();
        // find placeholder:
        final String ourPath = instruction.getPath();
        final String fileName = ourPath.substring(ourPath.lastIndexOf(FS) + 1);
        final String placeHolder = findMatchingPlaceholder(ourPath, context.placeholderData);
        if (!Strings.isNullOrEmpty(placeHolder)) {
            final String target = "{{" + placeHolder + "}}" + ourPath.substring(context.placeholderData.get(placeHolder).toString().length());
            final String sourceDirectory = getFolderForPlaceholder(placeHolder);
            final String source = sourceDirectory + '/' + fileName;
            // copy file:
            final String directory = rootPath + FS + DIR_RESOURCES + FS + sourceDirectory;
            createDirectory(directory);
            FileUtils.copyInputStreamToFile(new FileInputStream(ourPath), new File(directory + FS + fileName));
            final FileInstruction fileInstruction = new FileInstruction();
            fileInstruction.setAction(ACTION_COPY);
            fileInstruction.setBinary(true);
            fileInstruction.setSource(source);
            fileInstruction.setTarget(target);
            set.addInstruction(fileInstruction);
        } else {
            log.error("No placeholder for path: {}", ourPath);
        }

    }

    private static String getFolderForPlaceholder(final String placeHolder) {
        switch (placeHolder) {
            case PlaceholderService.IMAGES_ROOT:
                return "images";
            case PlaceholderService.COMPONENTS_PACKAGE:
                return "components";
        }
        return placeHolder;
    }

    private static String findMatchingPlaceholder(final String path, final Map<String, Object> placeholderData) {
        String placeHolderValue = "";
        String placeholderName = "";
        for (Map.Entry<String, Object> entry : placeholderData.entrySet()) {
            final String value = entry.getValue().toString();
            if (path.startsWith(value) && value.length() > placeHolderValue.length()) {
                placeHolderValue = value;
                placeholderName = entry.getKey();
            }
        }
        return placeholderName;
    }

    private static void writeCatalogComponents(final Set<InstructionData> instructions, final ServiceContext context) throws Exception {
        final List<CatalogComponentWrapper> components = context.data.getSelectedCatalogComponents();
        // might be made optional later, hence null check
        if (components != null) {
            final List<Path> projectFiles = getProjectFiles(INCLUDED_FILE_EXTENSIONS, context);
            final Path siteRoot = context.projectService.getBasePathForModule(Module.SITE);
            for (CatalogComponentWrapper component : components) {
                final String iconPath = component.getIconPath();
                final String iconFullPath = pathForSuffix(iconPath, projectFiles, siteRoot);
                if (!Strings.isNullOrEmpty(iconFullPath)) {
                    instructions.add(new InstructionData(SITE_BINARY, iconFullPath));
                }
                // java class:
                final String className = component.getComponentClassName() == null ? "" : component.getComponentClassName();
                String componentPath = "";
                if (!Strings.isNullOrEmpty(className)) {
                    final String classPath = DOT_PATTERN.matcher(className).replaceAll(FS) + ".java";
                    final String classFullPath = pathForSuffix(classPath, projectFiles, siteRoot);
                    if (!Strings.isNullOrEmpty(classFullPath)) {
                        instructions.add(new InstructionData(SITE_JAVA, classFullPath));
                    }
                    componentPath = "{{" + PlaceholderService.COMPONENTS_PACKAGE + "}}";
                    final int idx = className.lastIndexOf('.');
                    componentPath += (idx > -1) ? className.substring(idx) : className;

                }
                // add component xml
                final String path = component.getPath();

                final String xml = replaceValue(getNodeAsXml(context.session.getNode(path), true), "/sv:node/sv:property[@sv:name = 'hst:componentclassname']/sv:value", componentPath);
                log.info("xml {}", xml);
                instructions.add(new InstructionData(CATALOG_COMPONENT_XML, path, xml));
            }
        }
    }


    private static String pathForSuffix(final String suffixPath, final List<Path> projectFiles, final Path siteRoot) {
        final String prefix = siteRoot.toString();
        for (Path projectFile : projectFiles) {
            final String fullPath = projectFile.toString();
            if (fullPath.startsWith(prefix) && fullPath.endsWith(suffixPath)) {
                return fullPath;
            }
        }
        return null;
    }

    private static void writeRestResource(final String rootPath, final ServiceContext context) throws IOException {
        final DataWrapper data = context.data;
        if (data.isCreateInterface()) {
            final String pluginId = data.getPluginId();
            final String packageDir = DOT_PATTERN.matcher(("org.onehippo.cms7.essentials." + pluginId + ".rest")).replaceAll(FS);
            final String directory = rootPath + FS + DIR_JAVA + FS + packageDir;
            createDirectory(directory);
            // rest class:
            final String restData = replaceResource(TPL_PLUGIN_REST, context.placeholderData);
            final String restPath = directory + FS + "PluginResource.java";
            writeToFile(restData, restPath);
            // data class:
            final String dataData = replaceResource(TPL_PLUGIN_DATA, context.placeholderData);
            final String dataPath = directory + FS + "PluginData.java";
            writeToFile(dataData, dataPath);
            // context class:
            final String contextData = replaceResource(TPL_PLUGIN_CONTEXT, context.placeholderData);
            final String contextPath = directory + FS + "PluginContext.java";
            writeToFile(contextData, contextPath);
        }
    }

    private static void writeInterface(final String rootPath, final ServiceContext context) throws IOException {
        final DataWrapper data = context.data;
        if (data.isCreateInterface()) {
            final String pluginId = data.getPluginId();
            final String directory = rootPath + FS + DIR_RESOURCES_META + FS + data.getPluginType() + FS + pluginId;
            createDirectory(directory);
            // add html:
            final String htmlData = replaceResource(TPL_PLUGIN_HTML, context.placeholderData);
            final String htmlPath = directory + FS + pluginId + ".html";
            writeToFile(htmlData, htmlPath);
            // add js:
            final String jsData = replaceResource(TPL_PLUGIN_JAVASCRIPT, context.placeholderData);
            final String jsPath = directory + FS + pluginId + ".js";
            writeToFile(jsData, jsPath);
        }
    }

    private static void writePom(final String rootPath, final ServiceContext context) throws IOException {
        final String pomTemplate = EssentializerUtils.LICENSE_COMMUNITY.equals(context.data.getLicense()) ? TPL_POM : TPL_POM_ENTERPRISE;
        final String pomData = replaceResource(pomTemplate, context.placeholderData);
        final String pomPath = rootPath + FS + "pom.xml";
        writeToFile(pomData, pomPath);
    }


    private static void writeWebFragment(final String rootPath, final ServiceContext context) throws IOException {
        final String directory = rootPath + FS + DIR_META_INF;
        createDirectory(directory);
        final String fragmentData = replaceResource(TPL_WEB_FRAGMENT, context.placeholderData);
        final String fragmentPath = directory + FS + "web-fragment.xml";
        writeToFile(fragmentData, fragmentPath);

    }

    private static void writeIcon(final String rootPath, final ServiceContext context) throws IOException {
        final String directory = rootPath + FS + DIR_RESOURCES_META;
        final String targetFile = directory + FS + context.data.getPluginType() + FS + "images" + FS + context.data.getPluginId() + ".png";
        if (new File(targetFile).exists()) {
            log.info("Icon file already exists skipping copy: {}", targetFile);
            return;
        }
        log.info("Writing image to directory {}", directory);
        createDirectory(directory);
        final InputStream stream = WriteUtils.class.getResourceAsStream('/' + TPL_ICON);
        FileUtils.copyInputStreamToFile(stream, new File(targetFile));

    }

    private static void writePluginDescriptor(final String root, final ServiceContext context) throws IOException {
        final PluginDescriptor plugin = new PluginDescriptor();
        final DataWrapper data = context.data;
        plugin.setName(data.getPluginName());
        final Map<String, Set<String>> categories = new HashMap<>();
        categories.put("type", new ImmutableSet.Builder<String>().add(data.getPluginType()).build());
        categories.put("license", new ImmutableSet.Builder<String>().add(data.getLicense()).build());
        categories.put("application", new ImmutableSet.Builder<String>().add("content").build());
        plugin.setCategories(categories);
        plugin.setType(data.getPluginType());
        plugin.setId(data.getPluginId());
        plugin.setIntroduction(data.getPluginDescription());
        plugin.setDescription(data.getPluginDescription());
        plugin.setIcon("/essentials/" + data.getPluginType() + "/images/" + data.getPluginId() + ".png");
        final PluginDescriptor.Vendor vendor = new PluginDescriptor.Vendor();
        vendor.setName("Hippo");
        vendor.setUrl("https://www.onehippo.com");
        plugin.setVendor(vendor);
        plugin.setPackageFile("/META-INF/" + data.getPluginId() + "_instructions.xml");
        // rest
        if (data.isCreateInterface()) {
            plugin.setHasConfiguration(true);
            plugin.setRestClasses(new ImmutableList.Builder<String>().add("org.onehippo.cms7.essentials." + data.getPluginId() + ".rest.PluginResource").build());
        }
        final List<String> selectedPluginDependencies = data.getSelectedPluginDependencies();
        if (selectedPluginDependencies != null) {
            final List<PluginDescriptor.Dependency> pluginDependencies = new ArrayList<>();
            for (String selectedPluginDependency : selectedPluginDependencies) {
                final PluginDescriptor.Dependency dependency = new PluginDescriptor.Dependency();
                dependency.setPluginId(selectedPluginDependency);
                dependency.setMinInstallStateForInstalling(null);
                dependency.setMinInstallStateForBoarding(null);
                pluginDependencies.add(dependency);
            }
            plugin.setPluginDependencies(pluginDependencies);
        }

        final String descriptorData = JsonUtils.toJson(plugin);
        //final String targetDirectory = root + FS + data.getArtifactId() + FS;
        final String dir = root + FS + DIR_RESOURCES;
        createDirectory(dir);
        final String descriptorPath = dir + FS + "plugin-descriptor.json";
        writeToFile(descriptorData, descriptorPath);
    }

    public static Map<String, Object> createPlaceHolders(final PlaceholderService placeholderService, final DataWrapper data) {
        final Map<String, Object> placeholders = placeholderService.makePlaceholders();
        placeholders.put(PH_PLUGIN_CONTROLLER, createJsSafeName(data));
        placeholders.put(PH_PLUGIN_ID, data.getPluginId());
        placeholders.put(PH_ARTIFACT_ID, data.getArtifactId());
        placeholders.put(PH_GROUP_ID, data.getGroupId());
        placeholders.put(PH_ESSENTIALS_VERSION, data.getEssentialsVersion());
        placeholders.put(PH_PLUGIN_NAME, data.getPluginName());
        placeholders.put(PH_PLUGIN_DESCRIPTION, data.getPluginDescription());
        placeholders.put(PH_PLUGIN_VERSION, data.getPluginVersion());
        return placeholders;
    }

    private static String createJsSafeName(final DataWrapper data) {
        final String cleaned = CharMatcher.ascii().retainFrom(data.getPluginId());
        return INVALID_CHARS_PATTERN.matcher(cleaned).replaceAll("") + "Ctrl";
    }

    public static File createDirectory(final String directory) throws IOException {
        final File target = new File(directory);
        if (target.exists()) {
            if (target.isDirectory()) {
                return target;
            }
            throw new IllegalArgumentException("Target directory exists, but it is a file: " + directory);
        }
        FileUtils.forceMkdir(target);
        return target;
    }


    private static String replaceResource(final String template, final Map<String, Object> placeholderData) {
        final String plugin = readResourceTemplate(template);
        return TemplateUtils.replaceTemplateData(plugin, placeholderData);
    }

    private static void writeToFile(final String data, final String path) throws IOException {
        try {
            final File file = new File(path);
            file.createNewFile();
            GlobalUtils.writeToFile(data, file.toPath());
        } catch (IOException e) {
            log.error("Error writing to: {}", path);
            throw e;
        }
    }

    private static String extractJcrContent(final Node node) throws RepositoryException {
        final InputStream stream = node.getNode("jcr:content").getProperty("jcr:data").getBinary().getStream();
        return readStreamAsText(stream);

    }

    private static byte[] extractJcrBytes(final Node node) throws Exception {
        final InputStream stream = extractJcrStream(node);
        return IOUtils.toByteArray(stream);

    }

    private static InputStream extractJcrStream(final Node node) throws Exception {
        return node.getNode("jcr:content").getProperty("jcr:data").getBinary().getStream();


    }


    private static String readResourceTemplate(final String resourceName) {
        final InputStream stream = WriteUtils.class.getResourceAsStream('/' + resourceName);
        if (stream == null) {

            throw new IllegalStateException("Resource not found: " + resourceName);
        }

        final String template = readStreamAsText(stream);
        if (Strings.isNullOrEmpty(template)) {
            throw new IllegalStateException("Resource was empty ");
        }
        return template;
    }

    private static String replaceNamespace(final String xml, final ServiceContext context) {
        final String namespace = context.settingsService.getSettings().getProjectNamespace();
        final String properties = xml.replaceAll(namespace + ':', "{{namespace}}:");
        return properties.replaceAll('/' + namespace + '/', "/{{namespace}}/");
    }

    public static String getNodeAsXml(final Node node, final boolean removeUUID) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ((HippoSession) node.getSession()).exportDereferencedView(node.getPath(), out, true, false);
        final byte[] bytes = out.toByteArray();
        if (removeUUID) {
            return prettyPrint(removeUUID(bytes));
        }
        return prettyPrint(bytes);
    }


    private static byte[] removeUUID(final byte[] bytes) {
        try {
            final org.dom4j.Document document = DocumentHelper.parseText(prettyPrint(bytes));
            final org.dom4j.Node node = document.selectSingleNode("/sv:node/sv:property[@sv:name = 'jcr:uuid']");
            if (node != null) {
                final Element parent = node.getParent();
                if (parent != null) {
                    parent.remove(node);
                }
            }
            return document.asXML().getBytes();
        } catch (Exception e) {
            log.error("", e);
        }
        return bytes;
    }

    private static String getXmlNodeValue(final String xml, final String xpath) {
        try {
            final org.dom4j.Document document = DocumentHelper.parseText(xml);
            final org.dom4j.Node node = document.selectSingleNode(xpath);
            if (node != null) {
                return node.getText();
            }
            return prettyPrint(document.asXML().getBytes());
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    private static String replaceValue(final String xml, final String xpath, final String value) {

        try {
            final org.dom4j.Document document = DocumentHelper.parseText(xml);
            final org.dom4j.Node node = document.selectSingleNode(xpath);
            if (node != null) {
                node.setText(value);
            }
            return prettyPrint(document.asXML().getBytes());
        } catch (Exception e) {
            log.error("", e);
        }
        return xml;
    }

    private static String prettyPrint(byte[] bytes) throws Exception {
        final Source source = new StreamSource(new ByteArrayInputStream(bytes));
        final DOMResult result = new DOMResult();
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer identityTransformer = transformerFactory.newTransformer();
        identityTransformer.transform(source, result);
        final Document doc = (Document) result.getNode();
        doc.setXmlStandalone(true);
        final Transformer transformer = transformerFactory.newTransformer();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
        return out.toString("UTF-8");
    }

    private WriteUtils() {
    }



}
