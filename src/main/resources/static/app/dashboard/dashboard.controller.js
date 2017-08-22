(function() {
    'use strict';

    angular
        .module('app')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$http', '$log', '$location', 'RealtimeMeterstandenService', 'RealtimeKlimaatService', 'RealtimeOpgenomenVermogenService'];

    function DashboardController($http, $log, $location, RealtimeMeterstandenService, RealtimeKlimaatService, RealtimeOpgenomenVermogenService) {
        var vm = this;

        vm.stroomClick = stroomClick;
        vm.gasClick = gasClick;
        vm.temperatuurClick = temperatuurClick;
        vm.luchtvochtigheidClick = luchtvochtigheidClick;

        activate();

        function activate() {
            getMeestRecenteMeterstand();
            getMeestRecenteOpgenomenVermogen();
            getGasVerbruikVandaag();
            getGemiddeldeGasVerbruikPerDagInAfgelopenWeek();
            getOudsteMeterstandVanVandaag();
            getMeestRecenteKlimaat();
            getHuidigeEnergieContract();
        }

        function stroomClick() {
            $location.path('energie/verbruik/uur').search('energiesoort', ['stroom']);
        }
        function gasClick() {
            $location.path('energie/verbruik/uur').search('energiesoort', ['gas']);
        }
        function temperatuurClick() {
            $location.path('klimaat/historie/temperatuur');
        }
        function luchtvochtigheidClick() {
            $location.path('klimaat/historie/luchtvochtigheid');
        }

        RealtimeMeterstandenService.receive().then(null, null, function(jsonData) {
            updateMeterstand(jsonData);
        });

        RealtimeOpgenomenVermogenService.receive().then(null, null, function(jsonData) {
            updateOpgenomenVermogen(jsonData);
        });

        RealtimeKlimaatService.receive().then(null, null, function(jsonData) {
            updateKlimaat(jsonData);
        });

        function getGemiddeldeGasVerbruikPerDagInAfgelopenWeek() {
            var gisteren = Date.parse('yesterday').getTime();
            var weekVoorGisteren = Date.parse('yesterday').add(-6).days().getTime();

            var url = 'api/energie/gemiddelde-per-dag-in-periode/' + weekVoorGisteren + '/' + gisteren;

            $http({
                method: 'GET', url: url
            }).then(function successCallback(response) {
                vm.gemiddeldeGasVerbruikPerDagInAfgelopenWeek = response.data.gasVerbruik;
                $log.debug("Gemiddelde gasverbruik per dag over afgelopen week: " + vm.gemiddeldeGasVerbruikPerDagInAfgelopenWeek);
                setGasVandaagLeds(vm);
            }, function errorCallback(response) {
                $log.error(angular.toJson(response));
            });
        }

        function getGasVerbruikVandaag() {
            var url = 'api/energie/verbruik-per-dag/' + Date.today().getTime() + '/' + (Date.today().set({hour: 23, minute: 59, second: 59, millisecond: 999})).getTime();

            $http({
                method: 'GET', url: url
            }).then(function successCallback(response) {
                if (response.data.length == 1) {
                    vm.gasVerbruikVandaag = response.data[0].gasVerbruik;
                    setGasVandaagLeds();
                }
            }, function errorCallback(response) {
                $log.error(angular.toJson(response));
            });
        }

        function getHuidigeEnergieContract() {
            var url = 'api/energiecontract/current';

            $http({
                method: 'GET', url: url
            }).then(function successCallback(response) {
                vm.currentEnergieContract = response.data;
                updateYearlyUsageBasedOnCurrentUsage();
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

        function getMeestRecenteOpgenomenVermogen() {
            $http({
                method: 'GET', url: 'api/opgenomen-vermogen/meest-recente'
            }).then(function successCallback(response) {
                updateOpgenomenVermogen(response.data);
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
                $log.debug("Procentuele verandering gas dagverbruik t.o.v. gemiddelde in afgelopen week: " + procentueleVeranderingTovAfgelopenWeek);
                vm.gasVandaagLed9 = procentueleVeranderingTovAfgelopenWeek >= 50;
                vm.gasVandaagLed8 = procentueleVeranderingTovAfgelopenWeek >= 40;
                vm.gasVandaagLed7 = procentueleVeranderingTovAfgelopenWeek >= 30;
                vm.gasVandaagLed6 = procentueleVeranderingTovAfgelopenWeek >= 20;
                vm.gasVandaagLed5 = procentueleVeranderingTovAfgelopenWeek >= 10;
                vm.gasVandaagLed4 = procentueleVeranderingTovAfgelopenWeek >= 0;
                vm.gasVandaagLed3 = procentueleVeranderingTovAfgelopenWeek >= -10;
                vm.gasVandaagLed2 = procentueleVeranderingTovAfgelopenWeek >= -20;
                vm.gasVandaagLed1 = procentueleVeranderingTovAfgelopenWeek >= -30;
                vm.gasVandaagLed0 = true;
            }
        }

        function updateYearlyUsageBasedOnCurrentUsage() {
            if (vm.currentEnergieContract && vm.opgenomenVermogen) {
                vm.stroomVerbruikPerJaarInKwhObvHuidigeOpgenomenVermogen = Math.round((vm.opgenomenVermogen.watt * 24 * 365) / 1000);
                $log.debug('stroomVerbruikPerJaarInKwhObvHuidigeOpgenomenVermogen: ', vm.stroomVerbruikPerJaarInKwhObvHuidigeOpgenomenVermogen);

                if (vm.opgenomenVermogen.tariefIndicator == 'NORMAAL' || !vm.currentEnergieContract.stroomPerKwhDalTarief) {
                    vm.stroomKostenPerJaarObvHuidigeOpgenomenVermogen = vm.stroomVerbruikPerJaarInKwhObvHuidigeOpgenomenVermogen * vm.currentEnergieContract.stroomPerKwhNormaalTarief;
                } else if (vm.opgenomenVermogen.tariefIndicator == 'DAL' && vm.currentEnergieContract.stroomPerKwhDalTarief) {
                    vm.stroomKostenPerJaarObvHuidigeOpgenomenVermogen = vm.stroomVerbruikPerJaarInKwhObvHuidigeOpgenomenVermogen * vm.currentEnergieContract.stroomPerKwhDalTarief;
                }

                $log.debug('stroomKostenPerJaarObvHuidigeOpgenomenVermogen: ', vm.stroomKostenPerJaarObvHuidigeOpgenomenVermogen);
            }
        }

        function updateOpgenomenVermogen(opgenomenVermogen) {
            vm.opgenomenVermogen = opgenomenVermogen;
            setOpgenomenVermogenLeds(opgenomenVermogen.watt);
            updateYearlyUsageBasedOnCurrentUsage();
        }

        function updateMeterstand(meterstand) {
            if (meterstand) {
                if (vm.gasVerbruikVandaag === null) {
                    getGasVerbruikVandaag();
                }

                vm.meterstand = meterstand;

                if (vm.oudsteVanVandaag) {
                    vm.gasVerbruikVandaag = meterstand.gas - vm.oudsteVanVandaag.gas;
                }
            }
        }
    }
})();

