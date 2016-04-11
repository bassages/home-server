(function() {
    'use strict';

    angular
        .module('app')
        .directive('odometer', odometer);

    function odometer () {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {

                new Odometer({
                        el: element[0],
                        value: scope[attrs.odometer],
                        duration: 1000
                    }
                );

                //Watch for changes and update the element value (causing odometer to redraw)
                scope.$watch(attrs.odometer, function(val) {
                    element.text(val);
                });

            }
        };
    }
})();