(function() {
    'use strict';

    angular
        .module('app')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$scope', '$http', '$log', 'RealtimeMeterstandenService'];

    function turnOffAllStroomLeds($scope) {
        for (var i = 0; i < 10; i++) {
            $scope['led' + i] = false;
        }
    }

    function DashboardController($scope, $http, $log, RealtimeMeterstandenService) {
        turnOffAllStroomLeds($scope);

        $scope.huidigOpgenomenVermogen = 0;
        $scope.gasVerbruikVandaag = 0;
        $scope.oudsteVanVandaag = null;

        $http.get('rest/meterstanden/meest-recente')
            .success(function(data) {
                update(data);
            }
        );

        $http.get('rest/gas/verbruik-per-dag/' + Date.today().getTime() + '/' + (Date.today().setHours(23, 59, 59, 999)))
            .success(function(data) {
                if (data.length == 1) {
                    $scope.gasVerbruikVandaag = data[0].verbruik
                }
            }
        );

        $http.get('rest/meterstanden/oudste-vandaag')
            .success(function(data) {
                $scope.oudsteVanVandaag = data;
            }
        );

        RealtimeMeterstandenService.receive().then(null, null, function(jsonData) {
            update(jsonData);
        });

        function update(data) {
            $scope.t1 = data.stroomTarief1;
            $scope.t2 = data.stroomTarief2;
            $scope.meterstandGas = data.gas;
            $scope.huidigOpgenomenVermogen = data.stroomOpgenomenVermogenInWatt;

            var step = 150;
            $scope.stroomLed0 = data.stroomOpgenomenVermogenInWatt > 0;
            $scope.stroomLed1 = data.stroomOpgenomenVermogenInWatt >= (step);
            $scope.stroomLed2 = data.stroomOpgenomenVermogenInWatt >= (2 * step);
            $scope.stroomLed3 = data.stroomOpgenomenVermogenInWatt >= (3 * step);
            $scope.stroomLed4 = data.stroomOpgenomenVermogenInWatt >= (4 * step);
            $scope.stroomLed5 = data.stroomOpgenomenVermogenInWatt >= (5 * step);
            $scope.stroomLed6 = data.stroomOpgenomenVermogenInWatt >= (6 * step);
            $scope.stroomLed7 = data.stroomOpgenomenVermogenInWatt >= (7 * step);
            $scope.stroomLed8 = data.stroomOpgenomenVermogenInWatt >= (8 * step);
            $scope.stroomLed9 = data.stroomOpgenomenVermogenInWatt >= (9 * step);

            if ($scope.oudsteVanVandaag != null) {
                $scope.gasVerbruikVandaag = data.gas - $scope.oudsteVanVandaag.gas;
            }
        }
    }
})();

