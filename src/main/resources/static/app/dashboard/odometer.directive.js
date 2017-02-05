(function() {
    'use strict';

    angular
        .module('app')
        .directive('odometer', odometer);

    // TODO: https://github.com/HubSpot/odometer/issues/106
    // An alternative could be: http://scrollerjs.pixelstech.net/

    function odometer () {
        return {

            restrict: 'A',
            link: function(scope, element, attrs) {

                var options = {
                    el: element[0],
                    value: scope[attrs.odometer],
                    duration: 1000
                };

                var nrOfDecimals = 0;
                if (attrs.nrofdecimals) {
                    nrOfDecimals = attrs.nrofdecimals;
                }

                var odoformat = "(.ddddddddd)";
                if (nrOfDecimals > 0) {
                    odoformat = odoformat + ',';
                    for (var i = 0; i < nrOfDecimals; i++) {
                        odoformat = odoformat + 'd';
                    }
                    odoformat = odoformat + 'd';
                }

                options.format = odoformat;

                var od = new Odometer(options);

                //Watch for changes and update the element value (causing odometer to redraw)
                scope.$watch(attrs.odometer, function(val) {
                    if (val !== null) {

                        if (nrOfDecimals == 1) {
                            val = val + 0.01;
                        } else if (nrOfDecimals == 2) {
                            val = val + 0.001;
                        } else if (nrOfDecimals == 3) {
                            val = val + 0.0001;
                        } else if (nrOfDecimals == 4) {
                            val = val + 0.00001;
                        }
                        od.update(val.toString().replace('.', ',') + '1');
                    }
                });

            }
        };
    }
})();