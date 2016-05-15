(function() {
    'use strict';

    angular
        .module('app')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$scope', '$http', '$log', 'RealtimeMeterstandenService', 'RealtimeKlimaatService'];

    function turnOffAllStroomLeds($scope) {
        for (var i = 0; i < 10; i++) {
            $scope['stroomLed' + i] = false;
        }
    }

    function DashboardController($scope, $http, $log, RealtimeMeterstandenService, RealtimeKlimaatService) {

        clearMeterstandData();
        clearKlimaatData();

        function clearMeterstandData() {
            turnOffAllStroomLeds($scope);
            $scope.lastupdate = null;
            $scope.huidigOpgenomenVermogen = null;
            $scope.gasVerbruikVandaag = null;
            $scope.oudsteVanVandaag = null;
            $scope.t1 = null;
            $scope.t2 = null;
            $scope.meterstandGas = null;
        }

        function clearKlimaatData() {
            $scope.huidigKlimaat = null;
        }

        getMeestRecenteMeterstand();
        getGasVerbruikVandaag();
        getOudsteMeterstandVanVandaag();
        getMeestRecenteKlimaat();

        RealtimeMeterstandenService.receive().then(null, null, function(jsonData) {
            updateMeterstand(jsonData);
        });

        RealtimeKlimaatService.receive().then(null, null, function(jsonData) {
            updateKlimaat(jsonData);
        });

        function getGasVerbruikVandaag() {
            var url = 'rest/gas/verbruik-per-dag/' + Date.today().getTime() + '/' + (Date.today().set({hour: 23, minute: 59, second: 59, millisecond: 999})).getTime();
            $log.info('Getting data from URL: ' + url);

            $http({
                method: 'GET', url: url
            }).then(function successCallback(response) {
                if (response.data.length == 1) {
                    $scope.gasVerbruikVandaag = response.data[0].verbruik
                }
            }, function errorCallback(response) {
                $log.error(JSON.stringify(response));
            });
        }

        function getMeestRecenteMeterstand() {
            $http({
                method: 'GET', url: 'rest/meterstanden/meest-recente'
            }).then(function successCallback(response) {
                updateMeterstand(response.data);
            }, function errorCallback(response) {
                $log.error(JSON.stringify(response));
            });
        }

        function getMeestRecenteKlimaat() {
            $http({
                method: 'GET', url: 'rest/klimaat/meest-recente'
            }).then(function successCallback(response) {
                updateKlimaat(response.data);
            }, function errorCallback(response) {
                $log.error(JSON.stringify(response));
            });
        }

        function getOudsteMeterstandVanVandaag() {
            $http({
                method: 'GET', url: 'rest/meterstanden/oudste-vandaag'
            }).then(function successCallback(response) {
                $scope.oudsteVanVandaag = response.data;
            }, function errorCallback(response) {
                $log.error(JSON.stringify(response));
            });
        }

        function updateKlimaat(data) {
            if (data != null) {
                $scope.huidigKlimaat = data;
            }
        }

        function updateMeterstand(data) {
            if (data != null) {
                if ($scope.gasVerbruikVandaag == null) {
                    getGasVerbruikVandaag();
                }

                $scope.lastupdate = new Date(data.datumtijd);
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
    }
})();

