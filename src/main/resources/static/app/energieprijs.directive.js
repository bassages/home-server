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

                function requiredFromUser(text) {
                    var validate = text.replace(',','.');
                    if (!isNaN(parseFloat(validate)) && isFinite(validate)) {
                        ngModel.$setValidity("energieprijs", true);
                        return parseFloat(validate);
                    } else {
                        ngModel.$setValidity("energieprijs", false);
                        return null;
                    }
                }
                function optionalFromUser(text) {
                    var validate = text.replace(',','.');

                    if (validate.length === 0) {
                        ngModel.$setValidity("energieprijs", true);
                        return null;
                    } else if (!isNaN(parseFloat(validate)) && isFinite(validate)) {
                        ngModel.$setValidity("energieprijs", true);
                        return parseFloat(validate);
                    } else {
                        ngModel.$setValidity("energieprijs", false);
                        return null;
                    }
                }
                function toUser(prijs) {
                    if(typeof prijs != 'undefined' && prijs !== null) {
                        ngModel.$setValidity("energieprijs", true);
                        return prijs.toFixed(4).replace('.',',');
                    } else {
                        return null;
                    }
                }

                if (element[0].attributes.getNamedItem("required")) {
                    ngModel.$parsers.push(requiredFromUser);
                } else {
                    ngModel.$parsers.push(optionalFromUser);
                }
                ngModel.$formatters.push(toUser);
            }
        };
    }

})();