(function() {
    'use strict';

    angular
        .module('app')
        .factory('KostenService', KostenService);

    KostenService.$inject = ['$resource'];

    function KostenService($resource) {
        return $resource('/rest/kosten/:id');
    }
})();