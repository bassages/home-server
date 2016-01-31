(function() {
    'use strict';

    angular
        .module('app')
        .service('MeterstandenService', MeterstandenService);

    MeterstandenService.$inject = ['$http'];

    function MeterstandenService($http) {

        this.getMeterstandenPerDagInPeriod = function(van, totEnMet) {

            return $http({
                method: 'GET', url: 'rest/meterstanden/per-dag/' + van + '/' + totEnMet
            });
        };

    }
})();