(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatTopChartsController', KlimaatTopChartsController);

    KlimaatTopChartsController.$inject = ['$http', '$q', '$log', '$routeParams', 'KlimaatService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function KlimaatTopChartsController($http, $q, $log, $routeParams, KlimaatService, LoadingIndicatorService, ErrorMessageService) {
        var vm = this;

        vm.sensortype = $routeParams.sensortype;
        vm.unitlabel = KlimaatService.getUnitlabel(vm.sensortype);

        vm.limits = [5, 10, 25, 50, 100];
        vm.limit = 10;

        vm.from = Date.january().first();
        vm.to = Date.today();

        vm.dateformat = 'EEEE. dd-MM-yyyy';

        activate();

        function activate() {
            getDataFromServer();
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var requests = [
                getTopChartData('hoogste'),
                getTopChartData('laagste')
            ];

            $q.all(requests).then(
                function successCallback(responses) {
                    vm.laagste = getResponseData(responses, 'laagste');
                    vm.hoogste = getResponseData(responses, 'hoogste');
                    LoadingIndicatorService.stopLoading();
                },
                function errorCallback(response) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Ophalen van gegevens is niet gelukt.', response);
                }
            );
        }

        function getResponseData(responses, charttype) {
            var result = null;
            for (var i = 0, len = responses.length; i < len; i++) {
                if (responses[i].config.url.indexOf(charttype) !== -1 ) {
                    return responses[i].data;
                }
            }
            return result;
        }

        function getTopChartData(charttype) {
            var defaultSensorCode = 'WOONKAMER';
            var dataUrl = 'api/klimaat/' + defaultSensorCode + '/' + charttype + '?from=' + vm.from.toString('yyyy-MM-dd') + '&to=' + vm.to.clone().add(1).day().toString('yyyy-MM-dd') + '&sensorType=' + vm.sensortype + '&limit=' + vm.limit;
            return $http( { method: 'GET', url: dataUrl } );
        }

        function handleServiceError(message, errorResult) {
            $log.error(message + ' Cause=' + angular.toJson(errorResult));
            ErrorMessageService.showMessage(message);
        }

        vm.datepickerPopupFromOptions = {
            maxDate: Date.today()
        };

        vm.datepickerPopupFrom = {
            opened: false
        };

        vm.toggleDatepickerPopupFrom = function() {
            vm.datepickerPopupFrom.opened = !vm.datepickerPopupFrom.opened;
        };

        vm.datepickerPopupToOptions = {
            maxDate: Date.today()
        };

        vm.datepickerPopupTo = {
            opened: false
        };

        vm.toggleDatepickerPopupTo = function() {
            vm.datepickerPopupTo.opened = !vm.datepickerPopupTo.opened;
        };

        vm.fromChange = function() {
            getDataFromServer();
        };

        vm.toChange = function() {
            getDataFromServer();
        };

        vm.limitChange = function() {
            getDataFromServer();
        };
    }

})();

