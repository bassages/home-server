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
                    var result = null;
                    if(epochDate) {
                        result = (new Date(epochDate)).toString('dd-MM-yyyy');
                    }
                    return result;
                }

                ngModel.$parsers.push(fromUser);
                ngModel.$formatters.push(toUser);
            }
        };
    }

})();