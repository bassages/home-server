(function() {
    'use strict';

    angular
        .module('app')
        .factory('EnergieContractService', EnergieContractService);

    EnergieContractService.$inject = ['$resource'];

    function EnergieContractService($resource) {
        return $resource('/api/energiecontract/:id');
    }
})();