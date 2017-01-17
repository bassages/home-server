(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatAverageController', KlimaatAverageController);

    KlimaatAverageController.$inject = ['$http', '$q', '$log', '$routeParams', 'KlimaatService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function KlimaatAverageController($http, $q, $log, $routeParams, KlimaatService, LoadingIndicatorService, ErrorMessageService) {
        var vm = this;

        activate();

        function activate() {
            vm.sensortype = $routeParams.sensortype;
            vm.unitlabel = KlimaatService.getUnitlabel(vm.sensortype);
            vm.selection = Date.january().first();
            vm.dateformat = 'yyyy';

            getDataFromServer();
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            vm.averagePerMonthInYear = [];

            var month = vm.selection.clone();

            var requests = [];

            for (var i = 1; i <= 12 ;i++) {
                var dataUrl = 'api/klimaat/gemiddelde?from=' + month.getTime() + '&to=' + month.clone().add(1).month().getTime() + '&sensortype=' + vm.sensortype;
                $log.info('Getting data from URL: ' + dataUrl);
                requests.push( $http( { method: 'GET', url: dataUrl } ) );
                month = month.clone().add(1).month();
            }

            $q.all(requests).then(
                function successCallback(response) {
                    for (var i = 0; i < requests.length; i++) {
                        var dateString = vm.selection.getFullYear() + '-' + (i + 1) + '-1';
                        vm.averagePerMonthInYear.push({date: new Date(dateString), average: response[i].data ? response[i].data : null});
                    }
                    LoadingIndicatorService.stopLoading();
                },
                function errorCallback(response) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError("Er is een fout opgetreden bij het ophalen van de gegevens", response);
                }
            );
        }

        function handleServiceError(message, errorResult) {
            $log.error(message + ' Cause=' + angular.toJson(errorResult));
            ErrorMessageService.showMessage(message);
        }

        vm.datepickerPopupOptions = {
            maxDate: Date.today(),
            datepickerMode: 'year',
            minMode: 'year'
        };

        vm.datepickerPopup = {
            opened: false
        };

        vm.toggleDatepickerPopup = function() {
            vm.datepickerPopup.opened = !vm.datepickerPopup.opened;
        };

        vm.datepickerChange = function() {
            getDataFromServer();
        };
    }

})();

