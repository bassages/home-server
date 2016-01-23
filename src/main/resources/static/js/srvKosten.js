(function() {
    'use strict';

    angular
        .module('app')
        .factory('KostenService', KostenService);

    KostenService.$inject = ['$resource'];

    function KostenService($resource) {
        return $resource('/homecontrol/rest/kosten/:id');
    }
})();