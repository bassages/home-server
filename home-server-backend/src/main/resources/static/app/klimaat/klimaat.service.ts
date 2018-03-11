(function() {
    'use strict';

    angular
        .module('app')
        .service('KlimaatService', KlimaatService);

    KlimaatService.$inject = [];

    function KlimaatService() {

        this.getUnitlabel = function(sensortype) {
            var result = '';

            if (sensortype === 'temperatuur') {
                result = '\u2103';
            } else if (sensortype === 'luchtvochtigheid') {
                result = '%';
            }
            return result;
        };
    }
})();
