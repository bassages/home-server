(function() {
    'use strict';

    angular
        .module('app')
        .filter('meterstand', meterstand);

    meterstand.$inject = ['$filter'];

    function meterstand($filter) {

        return function(input) {
            if(input == null){ return ""; }
            numbro.culture('nl-NL');
            return numbro(input).format('0.000');
        };
    }

})();