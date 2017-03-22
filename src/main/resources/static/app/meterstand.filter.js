(function () {
    'use strict';

    angular
        .module('app')
        .filter('meterstand', meterstand);

    meterstand.$inject = ['$filter'];

    function meterstand($filter) {
        numbro.culture('nl-NL');

        return function (input, numberOfDecimals) {
            if (!input) {
                return "";
            } else {
                if (typeof numberOfDecimals !== 'undefined') {
                    var decimalFormat = '';

                    if (numberOfDecimals > 0) {
                        decimalFormat += '.';
                    }
                    for (var i = 0; i < numberOfDecimals; i++) {
                        decimalFormat += '0';
                    }
                    return numbro(input).format('0' + decimalFormat);
                } else {
                    return numbro(input).format('0.000');
                }
            }
        };
    }

})();