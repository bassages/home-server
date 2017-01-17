(function() {
    'use strict';

    angular
        .module('app')
        .factory('EnergieContractenService', EnergieContractenService);

    EnergieContractenService.$inject = ['$resource'];

    function EnergieContractenService($resource) {
        return $resource('/api/energiecontract/:id');
    }
})();