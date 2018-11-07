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

(function (root, factory) {

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = factory(root, exports);
    }
  } else if (typeof define === 'function' && define.amd) {
    define(['exports'], function (exports) {
      root.R = factory(root, exports);
    });
  } else {
    root.R = factory(root, {});
  }

}(this, function (root, R) {
  var L = localStorage;
  var J = JSON;
  if (!Array.prototype.indexOf) {
    Array.prototype.indexOf = function (elt) {
      var len = this.length >>> 0;
      var from = Number(arguments[1]) || 0;
      from = (from < 0) ? Math.ceil(from) : Math.floor(from);
      if (from < 0) {
        from += len;
      }
      for (; from < len; from++) {
        if (from in this && this[from] === elt) {
          return from;
        }
      }
      return -1;
    };
  }

  R.p = "";
  R.k = function (key, options) {
    options = options || {};
    if (options.noPrefix) {
      return key;
    } else {
      return this.p + key;
    }

  };

  R.set = function (key, value, options) {
    var query_key = this.k(key, options);

    try {
      L.setItem(query_key, J.stringify({"data": value}));
    } catch (e) {
    }
  };

  R.get = function (key, missing, options) {
    var query_key = this.k(key, options), value;

    try {
      value = J.parse(L.getItem(query_key));
    } catch (e) {
      try {
        if (L[query_key]) {
          value = J.parse('{"data":"' + L.getItem(query_key) + '"}');
        } else {
          value = null;
        }
      } catch (e) {

      }
    }
    if (value === null) {
      return missing;
    } else if (typeof value.data !== 'undefined') {
      return value.data;
    } else {
      return missing;
    }
  };

  R.sadd = function (key, value, options) {
    var query_key = this.k(key, options),
        json;

    var values = R.smembers(key);

    if (values.indexOf(value) > -1) {
      return null;
    }

    try {
      values.push(value);
      json = J.stringify({"data": values});
      L.setItem(query_key, json);
    } catch (e) {
    }
  };

  R.smembers = function (key, options) {
    var query_key = this.k(key, options),
        value;

    try {
      value = J.parse(L.getItem(query_key));
    } catch (e) {
      value = null;
    }

    if (value === null) {
      return [];
    } else {
      return (value.data || []);
    }
  };

  R.sismember = function (key, value) {
    return R.smembers(key).indexOf(value) > -1;
  };

  R.getAll = function () {
    var keys = Object.keys(L);
    return keys.map(function (key) {
      var idx = key.indexOf(R.p);
      if (idx === 0) {
        var ourKey = key.substring(key.length, R.p.length);
        return R.get(ourKey);
      }
      return null;
    });
  };

  R.srem = function (key, value, options) {
    var query_key = this.k(key, options),
        json,
        index;

    var values = R.smembers(key, value);

    index = values.indexOf(value);

    if (index > -1) {
      values.splice(index, 1);
    }

    json = J.stringify({"data": values});

    try {
      L.setItem(query_key, json);
    } catch (e) {

    }
  };

  R.rm = function (key) {
    L.removeItem(R.p + key);
  };

  R.flush = function () {
    L.clear();
  };
  return R;

}));

(function () {
  "use strict";
  angular.module('hippo.essentials')
      .controller('essentializerCtrl', function ($scope, $sce, $log, $rootScope, $http, essentialsRestService, essentialsPluginService, essentialsProjectService) {
        $scope.pluginId = "essentializer";
        $scope.endpoint = essentialsRestService.baseUrl + '/' + $scope.pluginId;
        $scope.data = {};

        function loadOldData() {
          var allData = R.getAll();
          if (allData) {
            $scope.oldDataArray = allData.reduce(function (result, data) {
              if (data) {
                result.push(data);
              }
              return result;
            }, []);

          }
          // reset selected data:
          $scope.restorePluginData = null;
          $scope.deletePluginData = null;
        }

        $scope.componentChange = function (selected) {
          if (!selected) {
            return;
          }

          function notSelected(template) {
            if (template) {
              if (!$scope.data.selectedTemplates) {
                $scope.data.selectedTemplates = [];
              }
              return $scope.data.selectedTemplates.indexOf(template) === -1;
            }
            return false;
          }

          if (selected) {
            for (var i = 0; i < selected.length; i++) {
              var component = selected[i];
              var template = component.templateWrapper;
              if (notSelected(template)) {
                $scope.data.selectedTemplates.push(template);
              }
            }
          }

        };
        $scope.deleteOldData = function (oldData) {
          if (oldData) {
            R.rm(oldData.pluginId);
          }
          loadOldData();
        };
        $scope.restoreOldData = function (oldData) {

          function filterArray(old, existing) {
            if (!old || !existing) {
              return null;
            }
            var collection = [];
            for (var i = 0; i < old.length; i++) {
              var oldElement = old[i];
              for (var j = 0; j < existing.length; j++) {
                var existingElement = existing[j];
                var exists = objectEquals(existingElement, oldElement);
                if (exists) {
                  collection.push(oldElement);
                }
              }

            }

            function objectEquals(first, second) {
              if (first === null || first === undefined || second === null || second === undefined) {
                return first === second;
              }
              if (first.constructor !== second.constructor) {
                return false;
              }
              if (first instanceof Function) {
                return first === second;
              }
              if (first instanceof RegExp) {
                return first === second;
              }
              if (first === second || first.valueOf() === second.valueOf()) {
                return true;
              }
              if (Array.isArray(first) && first.length !== second.length) {
                return false;
              }
              if (first instanceof Date) {
                return false;
              }
              if (!(first instanceof Object)) {
                return false;
              }
              if (!(second instanceof Object)) {
                return false;
              }
              var p = Object.keys(first);
              return Object.keys(second).every(function (i) {
                    return p.indexOf(i) !== -1;
                  }) &&
                  p.every(function (i) {
                    return objectEquals(first[i], second[i]);
                  });
            }

            return collection;
          }

          if (oldData) {
            var data = $scope.data;
            data.targetDirectory = oldData.targetDirectory || data.targetDirectory;
            data.essentialsVersion = oldData.essentialsVersion || data.essentialsVersion;
            data.license = oldData.license || data.license;
            data.pluginId = oldData.pluginId || data.pluginId;
            data.artifactId = oldData.artifactId || data.artifactId;
            data.pluginName = oldData.pluginName || data.pluginName;
            data.pluginDescription = oldData.pluginDescription || data.pluginDescription;
            data.pluginVersion = oldData.pluginVersion || data.pluginVersion;
            data.selectedPluginDependencies = filterArray(oldData.selectedPluginDependencies, data.pluginDependencies) || [];
            data.pluginType = oldData.pluginType || data.pluginType;
            data.selectedCatalogComponents = filterArray(oldData.selectedCatalogComponents, data.catalogComponents) || [];
            data.selectedComponents = filterArray(oldData.selectedComponents, data.components) || [];
            data.selectedPages = filterArray(oldData.selectedPages, data.pages) || [];
            data.selectedSitemapItems = filterArray(oldData.selectedSitemapItems, data.sitemapItems) || [];
            data.selectedMenuItems = filterArray(oldData.selectedMenuItems, data.menuItems) || [];
            data.selectedMenus = filterArray(oldData.selectedMenus, data.menus) || [];
            data.selectedMounts = filterArray(oldData.selectedMounts, data.mounts) || [];
            data.selectedSites = filterArray(oldData.selectedSites, data.sites) || [];
            data.selectedDocumentTypes = filterArray(oldData.selectedDocumentTypes, data.documentTypes) || [];
            data.selectedContent = filterArray(oldData.selectedContent, data.content) || [];
            data.selectedWebFiles = filterArray(oldData.selectedWebFiles, data.webFiles) || [];
            data.selectedFiles = filterArray(oldData.selectedFiles, data.files) || [];
            data.selectedYamlFiles = filterArray(oldData.selectedYamlFiles, data.yamlFiles) || [];
            data.selectedYamlBinaryFiles = filterArray(oldData.selectedYamlBinaryFiles, data.yamlBinaryFiles) || [];
            data.selectedTemplates = filterArray(oldData.selectedTemplates, data.templates) || [];
            data.selectedDependencies = filterArray(oldData.selectedDependencies, data.dependencies) || [];
            data.selectedSharedDependencies = filterArray(oldData.selectedSharedDependencies, data.sharedDependencies) || [];
            data.createInterface = oldData.createInterface || data.createInterface;

          }
        };

        $scope.init = function () {
          // set storage prefix
          R.p = "essentializer";
          $http.get($scope.endpoint).success(function (data) {
            $scope.data = data;
            loadOldData();


          });
        };
        $scope.save = function () {
          R.set($scope.data.pluginId, $scope.data);
          loadOldData();
        };
        $scope.run = function () {
          R.set($scope.data.pluginId, $scope.data);
          $http.post($scope.endpoint, $scope.data).success(function (data) {
            loadOldData();
          });
        };

        $scope.init();


      })
})();