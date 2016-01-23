(function() {
    'use strict';

    angular
        .module('app')
        .directive('formattedepochtimestamp', formattedepochtimestamp);

    function formattedepochtimestamp() {
        return {
            restrict: 'A', // only matches attribute
            require: 'ngModel',
            link: function(scope, element, attr, ngModel) {
                function fromUser(text) {
                    return Date.parse(text).getTime();
                }
                function toUser(epochDate) {
                    if(epochDate) {
                        return (new Date(epochDate)).toString('dd-MM-yyyy');
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