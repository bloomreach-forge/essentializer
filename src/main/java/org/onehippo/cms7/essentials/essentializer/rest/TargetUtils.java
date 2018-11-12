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

public final class TargetUtils {

    private static final Logger log = LoggerFactory.getLogger(TargetUtils.class);
    private static final String HST_DEFAULT = "/hst:hst/hst:configurations/hst:default/";

    private TargetUtils() {
    }


    public static String getComponentTarget(final String path) {
        if (path.startsWith("/hst:hst/hst:configurations/common/hst:workspace/hst:containers")
                || path.startsWith("/hst:hst/hst:configurations/hst:default/hst:workspace/hst:containers")) {
            return path.substring(0, path.lastIndexOf('/'));
        }
        return "/hst:hst/hst:configurations/{{namespace}}/hst:workspace/hst:containers";
    }

    public static String getContainerTarget(final String path) {
        if (path.startsWith("/hst:hst/hst:configurations/common") || path.startsWith("/hst:hst/hst:configurations/hst:default")) {
            return path.substring(0, path.lastIndexOf('/'));
        }
        return "/hst:hst/hst:configurations/{{namespace}}/hst:components";
    }

    public static String getPageTarget(final String path) {
        if (path.startsWith("/hst:hst/hst:configurations/common") || path.startsWith("/hst:hst/hst:configurations/hst:default")) {
            return path.substring(0, path.lastIndexOf('/'));
        }
        return "/hst:hst/hst:configurations/{{namespace}}/hst:pages";
    }

    public static String getWebfileTarget(final ServiceContext context, final String path) {
        final String projectNamespace = context.settingsService.getSettings().getProjectNamespace();
        return path.replace("/webfiles/site", "{{webfilesRoot}}").replace(projectNamespace, "{{namespace}}");
    }

    public static String getSitemapTarget(final String path) {
        if (path.startsWith("/hst:hst/hst:configurations/common") || path.startsWith("/hst:hst/hst:configurations/hst:default")) {
            return path.substring(0, path.lastIndexOf('/'));
        }
        return "/hst:hst/hst:configurations/{{namespace}}/hst:sitemap";
    }


    public static String getMenuItemTarget(final String path) {
        if (path.startsWith("/hst:hst/hst:configurations/common") || path.startsWith("/hst:hst/hst:configurations/hst:default")) {
            return path.substring(0, path.lastIndexOf('/'));
        }
        final String start = "/hst:hst/hst:configurations/";
        final String cleaned = path.substring(start.length());
        final String secondPart = cleaned.substring(cleaned.indexOf('/'));
        return start + "{{namespace}}" + secondPart.substring(0, secondPart.lastIndexOf('/'));
    }

    public static Module getFileTarget(final String filePath, final ServiceContext context) {
        final ProjectService projectService = context.projectService;
        final List<Module> modules = ImmutableList.of(Module.SITE, Module.CMS);
        for (Module module : modules) {
            final Path path = projectService.getBasePathForModule(module);
            if (filePath.startsWith(path.toString())) {
                return module;
            }
        }

        return Module.PROJECT;
    }

    public static String getHstConfigurationTarget(final String path, final String nodeName) {
        String target = path;
        if (!target.contains("/hst:hst/hst:configurations/hst:default/")) {
            target = path.replaceAll("/hst:hst/hst:configurations/", "");
            target = target.substring(target.indexOf('/'));
            target = target.replaceAll(nodeName, "");
            target = "/hst:hst/hst:configurations/{{namespace}}" + target;
        }
        return target.replaceAll('/' + nodeName, "");

    }
    
    public static String getNamespacedRenderPath(final String renderPath) {
        if (renderPath.contains("/hstdefault/")) {
            return renderPath;
        }

        final String start = "webfile:/freemarker/";
        final String path = renderPath.replaceAll(start, "");
        return start + "{{namespace}}" + path.substring(path.indexOf('/'));

    }

    public static String getTemplateTarget(final String path) {
        if (path.startsWith(HST_DEFAULT)) {
            return path.substring(0, path.lastIndexOf('/'));
        }
        final String part = path.replaceAll("/hst:hst/hst:configurations/", "");
        final String ourPath = "/hst:hst/hst:configurations/{{namespace}}" + part.substring(part.indexOf('/'));
        return ourPath.substring(0, ourPath.lastIndexOf('/'));
    }

    public static String getFreemarkerTarget(final String path) {
        final String prefix = "/webfiles/site/freemarker";
        if (path.contains("hstdefault")) {
            return path.replaceAll(prefix, "{{freemarkerRoot}}");
        }
        final String ourPath = path.substring(prefix.length() + 1);
        return "{{freemarkerRoot}}/{{namespace}}" + ourPath.substring(ourPath.indexOf('/'));
    }

}
