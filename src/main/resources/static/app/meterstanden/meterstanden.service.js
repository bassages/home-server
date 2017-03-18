(function() {
    'use strict';

    angular
        .module('app')
        .service('MeterstandenService', MeterstandenService);

    MeterstandenService.$inject = ['$http', '$log'];

    function MeterstandenService($http, $log) {

        this.getMeterstandenPerDagInPeriod = function(van, totEnMet) {

            var url = 'api/meterstanden/per-dag/' + van + '/' + totEnMet;

            return $http({
                method: 'GET', url: url
            });
        };
    }
})();