(function() {
    'use strict';

    angular
        .module('app')
        .service('MeterstandenService', MeterstandenService);

    MeterstandenService.$inject = ['$http'];

    function MeterstandenService($http) {

        this.getMeterstandenPerDagInPeriod = function(van, tot) {
            const url = 'api/meterstanden/per-dag/' + van.toString('yyyy-MM-dd') + '/' + tot.toString('yyyy-MM-dd');

            return $http({
                method: 'GET', url: url
            });
        };
    }
})();