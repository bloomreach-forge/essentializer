/*
 * Copyright {{dateCurrentYear}}  Hippo B.V. (http://www.onehippo.com)
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

(function () {
  "use strict";
  angular.module('hippo.essentials')
      .controller('{{controllerName}}', function ($scope, $sce, $log, $rootScope, $http, essentialsRestService, essentialsPluginService, essentialsProjectService) {
        $scope.pluginId = "{{pluginId}}";
        $scope.endpoint = essentialsRestService.baseUrl + '/' + $scope.pluginId;
        $scope.data = {};

        $scope.init = function () {
          console.log("Initializing {{pluginId}}");
          $http.get($scope.endpoint).success(function (data) {
            $scope.data = data;
            console.log("Initialized {{pluginId}}", data);
          });
        };
        $scope.run = function () {
          console.log("POST {{pluginId}}");
          $http.post($scope.endpoint, $scope.data).success(function (data) {
            console.log("After POST {{pluginId}}", data)
          });
        };
        $scope.init();
      })
})();