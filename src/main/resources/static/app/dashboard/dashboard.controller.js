(function() {
    'use strict';

    angular
        .module('app')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$http', '$log', 'RealtimeMeterstandenService', 'RealtimeKlimaatService'];

    function DashboardController($http, $log, RealtimeMeterstandenService, RealtimeKlimaatService) {
        var vm = this;

        getMeestRecenteMeterstand();
        getGasVerbruikVandaag();
        getGemiddeldeGasVerbruikPerDagInAfgelopenWeek();
        getOudsteMeterstandVanVandaag();
        getMeestRecenteKlimaat();

        RealtimeMeterstandenService.receive().then(null, null, function(jsonData) {
            updateMeterstand(jsonData);
        });

        RealtimeKlimaatService.receive().then(null, null, function(jsonData) {
            updateKlimaat(jsonData);
        });

        function getGemiddeldeGasVerbruikPerDagInAfgelopenWeek() {
            var gisteren = Date.parse('yesterday').getTime();
            var weekVoorGisteren = Date.parse('yesterday').add(-6).days().getTime();

            var url = 'api/gas/gemiddelde-per-dag-in-periode/' + weekVoorGisteren + '/' + gisteren;
            $log.info('Getting data from URL: ' + url);

            $http({
                method: 'GET', url: url
            }).then(function successCallback(response) {
                vm.gemiddeldeGasVerbruikPerDagInAfgelopenWeek = response.data.verbruik;
                setGasVandaagLeds(vm);
            }, function errorCallback(response) {
                $log.error(angular.toJson(response));
            });
        }

        function getGasVerbruikVandaag() {
            var url = 'api/gas/verbruik-per-dag/' + Date.today().getTime() + '/' + (Date.today().set({hour: 23, minute: 59, second: 59, millisecond: 999})).getTime();
            $log.info('Getting data from URL: ' + url);

            $http({
                method: 'GET', url: url
            }).then(function successCallback(response) {
                if (response.data.length == 1) {
                    vm.gasVerbruikVandaag = response.data[0].verbruik;
                    setGasVandaagLeds();
                }
            }, function errorCallback(response) {
                $log.error(angular.toJson(response));
            });
        }

        function getMeestRecenteMeterstand() {
            $http({
                method: 'GET', url: 'api/meterstanden/meest-recente'
            }).then(function successCallback(response) {
                updateMeterstand(response.data);
            }, function errorCallback(response) {
                $log.error(angular.toJson(response));
            });
        }

        function getMeestRecenteKlimaat() {
            $http({
                method: 'GET', url: 'api/klimaat/meest-recente'
            }).then(function successCallback(response) {
                updateKlimaat(response.data);
            }, function errorCallback(response) {
                $log.error(angular.toJson(response));
            });
        }

        function getOudsteMeterstandVanVandaag() {
            $http({
                method: 'GET', url: 'api/meterstanden/oudste-vandaag'
            }).then(function successCallback(response) {
                vm.oudsteVanVandaag = response.data;
            }, function errorCallback(response) {
                $log.error(angular.toJson(response));
            });
        }

        function updateKlimaat(klimaat) {
            if (klimaat) {
                vm.huidigKlimaat = klimaat;

                if (klimaat.temperatuur) {
                    setTemperatuurLeds(klimaat.temperatuur);
                }
                if (klimaat.luchtvochtigheid) {
                    setLuchtVochtigheidLeds(klimaat.luchtvochtigheid);
                }
            }
        }

        function setOpgenomenVermogenLeds(stroomOpgenomenVermogenInWatt) {
            vm.opgenomenVermogenLed0 = stroomOpgenomenVermogenInWatt > 0;

            for (var i = 1; i < 9; i++) {
                vm['opgenomenVermogenLed' + i] = stroomOpgenomenVermogenInWatt >= (i * 150);
            }
        }

        function setTemperatuurLeds(temperatuur) {
            vm.temperatuurLed0 = true;

            for (var i = 1; i <= 9; i++) {
                var temperatuurForLed = i + 16;
                vm['temperatuurLed' + i] = temperatuur >= temperatuurForLed;
            }
        }

        function setLuchtVochtigheidLeds(luchtvochtigheid) {
            for (var i = 0; i <= 9; i++) {
                vm['luchtvochtigheidLed' + i] = luchtvochtigheid >= (i * 10);
            }
        }

        function setGasVandaagLeds() {
            if (vm.gasVerbruikVandaag && vm.gemiddeldeGasVerbruikPerDagInAfgelopenWeek) {
                var procentueleVeranderingTovAfgelopenWeek = ((vm.gasVerbruikVandaag - vm.gemiddeldeGasVerbruikPerDagInAfgelopenWeek) / vm.gemiddeldeGasVerbruikPerDagInAfgelopenWeek) * 100;
                $log.debug('Procentuele verandering: ' + procentueleVeranderingTovAfgelopenWeek);

                if (procentueleVeranderingTovAfgelopenWeek <= 0) {
                    vm.gasVandaagMood = 'positive';
                } else {
                    vm.gasVandaagMood = 'negative';
                }

                var procentueleVerandering = Math.abs(procentueleVeranderingTovAfgelopenWeek);
                for (var i = 0; i <= 9; i++) {
                    vm['gasVandaagLed' + i] = procentueleVerandering >= (i * 3);
                }
            }
        }

        function updateMeterstand(meterstanden) {
            if (meterstanden) {
                if (vm.gasVerbruikVandaag === null) {
                    getGasVerbruikVandaag();
                }

                vm.huidigeMeterstanden = meterstanden;

                setOpgenomenVermogenLeds(meterstanden.stroomOpgenomenVermogenInWatt);

                if (vm.oudsteVanVandaag) {
                    vm.gasVerbruikVandaag = meterstanden.gas - vm.oudsteVanVandaag.gas;
                }
            }
        }
    }
})();

