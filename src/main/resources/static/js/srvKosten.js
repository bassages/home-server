(function() {
    'use strict';

    angular
        .module('appHomecontrol')
        .factory('KostenService', ['$resource', KostenService]);

    function KostenService($resource) {
        return $resource('/homecontrol/rest/kosten/:id');
    }
})();