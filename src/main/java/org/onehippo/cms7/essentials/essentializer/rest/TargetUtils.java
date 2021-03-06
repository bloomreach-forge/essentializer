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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.onehippo.cms7.essentials.sdk.api.model.Module;
import org.onehippo.cms7.essentials.sdk.api.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static org.onehippo.cms7.essentials.essentializer.rest.WriteUtils.FS;

public final class TargetUtils {

    private static final Logger log = LoggerFactory.getLogger(TargetUtils.class);
    private static final String HST_DEFAULT = "/hst:hst/hst:configurations/hst:default/";
    private static final String HST_COMMON = "/hst:hst/hst:configurations/common/";

    private TargetUtils() {
    }


    public static String getContainerTarget(final String path) {
        if (path.startsWith("/hst:hst/hst:configurations/common/hst:workspace/hst:containers")
                || path.startsWith("/hst:hst/hst:configurations/hst:default/hst:workspace/hst:containers"))

        {
            return path.substring(0, path.lastIndexOf('/'));
        }
        return "/hst:hst/hst:configurations/{{namespace}}/hst:workspace/hst:containers";
    }

    public static String getComponentTarget(final String path) {
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
        if (!target.contains("/hst:hst/hst:configurations/hst:default/") && !target.contains("/hst:hst/hst:configurations/common/")) {
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
        if (path.startsWith(HST_DEFAULT) || path.startsWith(HST_COMMON)) {
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


    public static String createSubdirectory(final ServiceContext context, final String defaultDir, final String target) {
        log.info("target {}", target);
        if (target.contains("hst:default")) {
            return defaultDir + FS + "hstdefault";
        } else if (target.contains("/common/")) {
            return defaultDir + FS + "common";
        }
        return defaultDir + FS + "namespace";
    }

    public static String createSubdirectoryForMount(final ServiceContext context, final String path) {
        final Iterable<String> pathParts = Splitter.on("/").split(path);
        final List<String> parts = Lists.newArrayList(pathParts);
        if (parts.size() > 1) {
            return "hstMounts" + FS + parts.get(parts.size() - 2);
        }
        return "hstMounts";
    }

    public static String replaceSubPaths(final ServiceContext context, final String path) {
       return path.replaceAll('/' + context.settingsService.getSettings().getProjectNamespace() + '/', "/{{namespace}}/");
    }
}
