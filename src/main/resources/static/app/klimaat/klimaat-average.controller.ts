(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatAverageController', KlimaatAverageController);

    KlimaatAverageController.$inject = ['$http', '$log', '$routeParams', 'KlimaatService', 'LoadingIndicatorService', 'ErrorMessageService', 'DATETIME_CONSTANTS', '$uibModal'];

    function KlimaatAverageController($http, $log, $routeParams, KlimaatService, LoadingIndicatorService, ErrorMessageService, DATETIME_CONSTANTS, $uibModal) {
        var vm = this;

        vm.openMultipleYearsSelectionDialog = openMultipleYearsSelectionDialog;
        vm.getMonthName = getMonthName;
        vm.getFormattedSelectedYears = getFormattedSelectedYears;

        vm.sensortype = $routeParams.sensortype;
        vm.unitlabel = KlimaatService.getUnitlabel(vm.sensortype);
        vm.selection = [Date.january().first()];

        activate();

        function activate() {
            getDataFromServer();
        }

        function loadData(response) {
            vm.averagePerMonthInYear = response.data;
        }

        function getMonthName(monthNumber) {
            return DATETIME_CONSTANTS.fullMonths[monthNumber];
        }

        function getFormattedSelectedYears() {
            return _.map(vm.selection, function(date: Date) { return date.getFullYear(); }).join(', ');
        }

        function openMultipleYearsSelectionDialog () {
            var modalInstance = $uibModal.open({
                animation: false,
                templateUrl: 'app/multiple-dates-selection-dialog.html',
                backdrop: 'static',
                controller: 'MultipleDateSelectionController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    selectedDates: function() {
                        return vm.selection;
                    },
                    datepickerOptions: function() {
                        return {
                            maxDate: Date.today(), datepickerMode: 'year', minMode: 'year', yearRows: 4, yearColumns: 4
                        };
                    },
                    selectedDateFormat: function() { return 'yyyy'; }
                }
            });
            modalInstance.result.then(function(selectedYears) {
                vm.selection = selectedYears;
                getDataFromServer();
            }, function() {
                $log.info('Multiple Date Selection dialog was closed');
            });
        }

        function getDataFromServer() {

            vm.averagePerMonthInYear = [];

            if (vm.selection) {
                LoadingIndicatorService.startLoading();

                var urlParamJaar = 'jaar=' + _.map(vm.selection, function(o: any) { return new Date(o).getFullYear(); }).join('&jaar=');

                var defaultSensorCode = 'WOONKAMER';
                var dataUrl = 'api/klimaat/' + defaultSensorCode + '/gemiddeld-per-maand-in-jaar?' + urlParamJaar + '&sensorType=' + vm.sensortype;

                $http( { method: 'GET', url: dataUrl } ).then(
                    function successCallback(response) {
                        loadData(response);
                        LoadingIndicatorService.stopLoading();
                    },
                    function errorCallback(response) {
                        LoadingIndicatorService.stopLoading();
                        handleServiceError("Er is een fout opgetreden bij het ophalen van de gegevens", response);
                    }
                );
            }

        }

        function handleServiceError(message, errorResult) {
            $log.error(message + ' Cause=' + angular.toJson(errorResult));
            ErrorMessageService.showMessage(message);
        }
    }

})();

