(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatAverageController', KlimaatAverageController);

    KlimaatAverageController.$inject = ['$http', '$log', '$routeParams', 'KlimaatService', 'LoadingIndicatorService', 'ErrorMessageService', 'DATETIME_CONSTANTS', '$uibModal'];

    function KlimaatAverageController($http, $log, $routeParams, KlimaatService, LoadingIndicatorService, ErrorMessageService, DATETIME_CONSTANTS, $uibModal) {
        var vm = this;

        activate();

        function activate() {
            vm.sensortype = $routeParams.sensortype;
            vm.unitlabel = KlimaatService.getUnitlabel(vm.sensortype);
            vm.selection = [Date.january().first()];

            vm.openMultipleYearsSelectionDialog = function() {
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
                        }
                    }
                });
                modalInstance.result.then(function(selectedYears) {
                    vm.selection = selectedYears;
                    getDataFromServer();
                }, function() {
                    $log.info('Multiple Date Selection dialog was closed');
                });

            };

            getDataFromServer();
        }

        function loadData(response) {
            vm.averagePerMonthInYear = response.data;
        }

        vm.getMonthName = function(monthNumber) {
            return DATETIME_CONSTANTS.fullMonths[monthNumber];
        };

        vm.getFormattedSelectedYears = function() {
            return _.map(vm.selection, function(date) { return new Date(date).getFullYear(); }).join(', ');
        };

        function getDataFromServer() {

            vm.averagePerMonthInYear = [];

            if (vm.selection) {
                LoadingIndicatorService.startLoading();

                var urlParamJaar = 'jaar=' + _.map(vm.selection, function(o) { return new Date(o).getFullYear(); }).join('&jaar=');

                var dataUrl = 'api/klimaat/gemiddeld-per-maand-in-jaar?' + urlParamJaar + '&sensortype=' + vm.sensortype;

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

