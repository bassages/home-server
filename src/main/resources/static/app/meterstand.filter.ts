(function () {
    'use strict';

    angular
        .module('app')
        .filter('meterstand', meterstand);

    meterstand.$inject = [];

    function meterstand() {
        return function (input, numberOfDecimals) {
            if (!input) {
                return "";
            } else {
                if (typeof numberOfDecimals !== 'undefined') {
                    let decimalFormat = '';

                    if (numberOfDecimals > 0) {
                        decimalFormat += '.';
                    }
                    for (let i = 0; i < numberOfDecimals; i++) {
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