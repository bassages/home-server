'use strict';

angular.module('appHomecontrol.kostenService', [])

    .factory('KostenService', function($resource) {
        return $resource('/homecontrol/rest/kosten/:id');
    });
