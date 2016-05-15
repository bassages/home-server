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

                function toDate(text) {
                    if (typeof scope.isMultidateAllowed == 'function' && scope.isMultidateAllowed()) {
                        return toMultipleDates(text);
                    } else {
                        return toSingleDate(text);
                    }
                }

                function toMultipleDates(text) {
                    var result = [];
                    var dates = text.split(scope.getMultidateSeparator());
                    for (var i = 0; i < dates.length; i++) {
                        if (dates[i] != '') {
                            result.push(d3.time.format(scope.getD3DateFormat()).parse(dates[i]));
                        }
                    }
                    return result;
                }

                function toSingleDate(text) {
                    return d3.time.format(scope.getD3DateFormat()).parse(text);
                }

                function toDisplay(date) {
                    if (date) {
                        if (Object.prototype.toString.call(date) === '[object Array]') {
                            return multipleToDisplay(date);
                        } else {
                            return singleToDisplay(date);
                        }
                    }
                    return '';
                }

                function multipleToDisplay(dates, formatter) {
                    var result = '';
                    var formatter = d3.time.format(scope.getD3DateFormat());

                    for (var i = 0; i < dates.length; i++) {
                        if (result != '') {
                            result = result + scope.getMultidateSeparator();
                        }
                        result = result + formatter(dates[i]);
                    }
                    return result;
                }

                function singleToDisplay(date) {
                    var formatter = d3.time.format(scope.getD3DateFormat());
                    return formatter(date);
                }

                ngModel.$parsers.push(toDate);
                ngModel.$formatters.push(toDisplay);
            }
        };
    }

})();