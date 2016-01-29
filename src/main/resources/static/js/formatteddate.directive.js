(function() {
    'use strict';

    angular
        .module('app')
        .directive('formatteddate', formatteddate);

    function formatteddate() {
        return {
            restrict: 'A', // only matches attribute
            require: 'ngModel',
            link: function(scope, element, attr, ngModel) {

                function toValue(text) {
                    return d3.time.format(scope.getD3DateFormat()).parse(text);
                }

                function toDisplay(date) {
                    var result = '';
                    if(date) {
                        var formatter = d3.time.format(scope.getD3DateFormat());
                        result = formatter(date);
                    }
                    return result;
                }

                ngModel.$parsers.push(toValue);
                ngModel.$formatters.push(toDisplay);
            }
        };
    }

})();