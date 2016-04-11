(function() {
    'use strict';

    angular
        .module('app')
        .directive('odometer', odometer);

    function odometer () {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {

                var options = {
                    el: element[0],
                    value: scope[attrs.odometer],
                    duration: 1000
                };

                if (attrs.format) {
                    options.format = attrs.format;
                }

                new Odometer(options);

                //Watch for changes and update the element value (causing odometer to redraw)
                scope.$watch(attrs.odometer, function(val) {
                    element.text(val);
                });

            }
        };
    }
})();