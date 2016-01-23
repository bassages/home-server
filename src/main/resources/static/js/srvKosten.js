(function() {
    'use strict';

    angular
        .module('app')
        .factory('KostenService', ['$resource', KostenService]);

    function KostenService($resource) {
        return $resource('/homecontrol/rest/kosten/:id');
    }
})();