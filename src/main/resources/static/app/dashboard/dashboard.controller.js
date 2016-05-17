(function() {
    'use strict';

    angular
        .module('app')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$scope', '$http', '$log', 'RealtimeMeterstandenService', 'RealtimeKlimaatService'];

    function turnOffAllStroomLeds($scope) {
        for (var i = 0; i < 10; i++) {
            $scope['opgenomenVermogenLed' + i] = false;
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
                setTemperatuurLeds(data.temperatuur);
            }
        }

        function setOpgenomenVermogenLeds(stroomOpgenomenVermogenInWatt) {
            var step = 150;

            $scope.opgenomenVermogenLed0 = stroomOpgenomenVermogenInWatt > 0;

            for (var i = 1; i < 9; i++) {
                $scope['opgenomenVermogenLed' + i] = stroomOpgenomenVermogenInWatt >= (i * step);

            }
        }

        function setTemperatuurLeds(temperatuur) {
            $scope.temperatuurLed9 = temperatuur >= 25; //&& temperatuur < 26;
            $scope.temperatuurLed8 = temperatuur >= 24; //&& temperatuur < 25;
            $scope.temperatuurLed7 = temperatuur >= 23; //&& temperatuur < 24;
            $scope.temperatuurLed6 = temperatuur >= 22; //&& temperatuur < 23;
            $scope.temperatuurLed5 = temperatuur >= 21; //&& temperatuur < 22;
            $scope.temperatuurLed4 = temperatuur >= 20; //&& temperatuur < 21;
            $scope.temperatuurLed3 = temperatuur >= 19; //&& temperatuur < 20;
            $scope.temperatuurLed2 = temperatuur >= 18; //&& temperatuur < 19;
            $scope.temperatuurLed1 = temperatuur >= 17; //&& temperatuur < 18;
            $scope.temperatuurLed0 = true;
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

                setOpgenomenVermogenLeds(data.stroomOpgenomenVermogenInWatt);

                if ($scope.oudsteVanVandaag != null) {
                    $scope.gasVerbruikVandaag = data.gas - $scope.oudsteVanVandaag.gas;
                }
            }
        }
    }
})();

