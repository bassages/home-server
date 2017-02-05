(function() {
    'use strict';

    angular
        .module('app')
        .directive('energieprijs', energieprijs);

    function energieprijs() {
        return {
            restrict: 'A', // only matches attribute
            require: 'ngModel',
            link: function(scope, element, attr, ngModel) {
                function fromUser(text) {
                    var validate = text.replace(',','.');
                    if (!isNaN(parseFloat(validate)) && isFinite(validate)) {
                        ngModel.$setValidity("energieprijs", true);
                        return parseFloat(validate);
                    } else {
                        ngModel.$setValidity("energieprijs", false);
                        return null;
                    }
                }
                function toUser(prijs) {
                    if(prijs !== null) {
                        ngModel.$setValidity("energieprijs", true);
                        return prijs.toFixed(4).replace('.',',');
                    } else {
                        return null;
                    }
                }
                ngModel.$parsers.push(fromUser);
                ngModel.$formatters.push(toUser);
            }
        };
    }

})();