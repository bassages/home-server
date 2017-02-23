(function () {
    'use strict';

    angular
        .module('app')
        .filter('meterstand', meterstand);

    meterstand.$inject = ['$filter'];

    function meterstand($filter) {
        numbro.culture('nl-NL');

        return function (input) {
            if (!input) {
                return "";
            } else {
                return numbro(input).format('0.000');
            }
        };
    }

})();