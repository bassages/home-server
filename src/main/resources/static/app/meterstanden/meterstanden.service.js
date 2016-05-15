(function() {
    'use strict';

    angular
        .module('app')
        .service('MeterstandenService', MeterstandenService);

    MeterstandenService.$inject = ['$http', '$log'];

    function MeterstandenService($http, $log) {

        this.getMeterstandenPerDagInPeriod = function(van, totEnMet) {

            var url = 'rest/meterstanden/per-dag/' + van + '/' + totEnMet;

            $log.info('Getting data for meterstanden from URL: ' + url);

            return $http({
                method: 'GET', url: url
            });
        };
    }
})();