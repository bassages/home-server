(function() {
    'use strict';

    angular
        .module('app')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$scope', '$http', '$log', 'RealtimeMeterstandenService', 'RealtimeKlimaatService'];

    function DashboardController($scope, $http, $log, RealtimeMeterstandenService, RealtimeKlimaatService) {
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
            if (data) {
                $scope.huidigKlimaat = data;

                if (data.temperatuur) {
                    setTemperatuurLeds(data.temperatuur);
                }
                if (data.luchtvochtigheid) {
                    setLuchtVochtigheidLeds(data.luchtvochtigheid);
                }
            }
        }

        function setOpgenomenVermogenLeds(stroomOpgenomenVermogenInWatt) {
            $scope.opgenomenVermogenLed0 = stroomOpgenomenVermogenInWatt > 0;

            for (var i = 1; i < 9; i++) {
                $scope['opgenomenVermogenLed' + i] = stroomOpgenomenVermogenInWatt >= (i * 150);
            }
        }

        function setTemperatuurLeds(temperatuur) {
            $scope.temperatuurLed0 = true;

            for (var i = 1; i <= 9; i++) {
                var temperatuurForLed = i + 16;
                $scope['temperatuurLed' + i] = temperatuur >= temperatuurForLed;
            }
        }

        function setLuchtVochtigheidLeds(luchtvochtigheid) {
            for (var i = 0; i <= 9; i++) {
                $scope['luchtvochtigheidLed' + i] = luchtvochtigheid >= (i * 10);
            }
        }

        function updateMeterstand(meterstanden) {
            if (meterstanden != null) {
                if ($scope.gasVerbruikVandaag == null) {
                    getGasVerbruikVandaag();
                }

                $scope.huidigeMeterstanden = meterstanden;

                setOpgenomenVermogenLeds(meterstanden.stroomOpgenomenVermogenInWatt);

                if ($scope.oudsteVanVandaag != null) {
                    $scope.gasVerbruikVandaag = meterstanden.gas - $scope.oudsteVanVandaag.gas;
                }
            }
        }
    }
})();

