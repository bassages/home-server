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
                function fromUser(text) {
                    return Date.parse(text);
                }
                function toUser(date) {
                    if(date) {
                        return date.toString(scope.getDateFormat());
                    } else {
                        return '';
                    }
                }
                ngModel.$parsers.push(fromUser);
                ngModel.$formatters.push(toUser);
            }
        };
    }

})();