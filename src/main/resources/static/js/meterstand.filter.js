(function() {
    'use strict';

    angular
        .module('app')
        .filter('meterstand', meterstand);

    meterstand.$inject = ['$filter'];

    function meterstand($filter) {

        numbro.culture('nl-NL');

        return function(input) {
            if(input == null){ return ""; }
            return numbro(input).format('0.000');
        };
    }

})();