'use strict';

angular.module('appHomecontrol.kostenService', [])

    .factory('Kosten', function($resource) {
        return $resource('/homecontrol/rest/kosten/:id');
    });
