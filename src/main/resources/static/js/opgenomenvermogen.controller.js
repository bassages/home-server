(function() {
    'use strict';

    angular
        .module('app')
        .controller('StroomMeterstandController', StroomMeterstandController);

    StroomMeterstandController.$inject = ['$scope', '$http', 'RealtimeMeterstandenService'];

    function StroomMeterstandController($scope, $http, RealtimeMeterstandenService) {
        // Turn off all leds
        for (var i = 0; i < 10; i++) {
            $scope['led'+i] = false;
        }

        $scope.huidigOpgenomenVermogen = '0';

        $http.get('rest/meterstanden/meestrecente')
            .success(function(data) {
                update(data);
            }
        );

        RealtimeMeterstandenService.receive().then(null, null, function(jsonData) {
            update(jsonData);
        });

        function update(data) {
            $scope.t1 = data.stroomTarief1;
            $scope.t2 = data.stroomTarief2;
            $scope.huidigOpgenomenVermogen = data.stroomOpgenomenVermogenInWatt;

            var step = 150;
            $scope.led0 = data.stroomOpgenomenVermogenInWatt > 0;
            $scope.led1 = data.stroomOpgenomenVermogenInWatt >= (1 * step);
            $scope.led2 = data.stroomOpgenomenVermogenInWatt >= (2 * step);
            $scope.led3 = data.stroomOpgenomenVermogenInWatt >= (3 * step);
            $scope.led4 = data.stroomOpgenomenVermogenInWatt >= (4 * step);
            $scope.led5 = data.stroomOpgenomenVermogenInWatt >= (5 * step);
            $scope.led6 = data.stroomOpgenomenVermogenInWatt >= (6 * step);
            $scope.led7 = data.stroomOpgenomenVermogenInWatt >= (7 * step);
            $scope.led8 = data.stroomOpgenomenVermogenInWatt >= (8 * step);
            $scope.led9 = data.stroomOpgenomenVermogenInWatt >= (9 * step);
        }
    }
})();

