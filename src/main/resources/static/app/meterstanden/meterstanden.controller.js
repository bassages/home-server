(function() {
    'use strict';

    angular
        .module('app')
        .controller('MeterstandenController', MeterstandenController);

    MeterstandenController.$inject = ['$log', 'MeterstandenService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function MeterstandenController($log, MeterstandenService, LoadingIndicatorService, ErrorMessageService) {
        var vm = this;

        activate();

        function activate() {
            vm.selection = Date.today().clearTime().moveToFirstDayOfMonth();
            vm.dateformat = 'MMMM yyyy';
            getDataFromServer();
        }

        vm.isMaxSelected = function() {
            return Date.today().getMonth() == vm.selection.getMonth() && Date.today().getFullYear() == vm.selection.getFullYear();
        };

        vm.navigate = function(numberOfPeriods) {
            vm.selection = vm.selection.clone().add(numberOfPeriods).months();
            getDataFromServer();
        };

        vm.datepickerPopupOptions = {
            datepickerMode: 'month',
            minMode: 'month',
            maxDate: Date.today()
        };

        vm.datepickerPopup = {
            opened: false
        };

        vm.toggleDatepickerPopup = function() {
            vm.datepickerPopup.opened = !vm.datepickerPopup.opened;
        };

        vm.selectionChange = function() {
            getDataFromServer();
        };

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var van = vm.selection.getTime();
            var totEnMet = vm.selection.clone().moveToLastDayOfMonth().setHours(23, 59, 59, 999);

            MeterstandenService.getMeterstandenPerDagInPeriod(van, totEnMet)
                .then(
                function successCallback(response) {
                    vm.meterstandenPerDag = response.data;
                    LoadingIndicatorService.stopLoading();
                },
                function errorCallback(response) {
                    $log.error(angular.toJson(response));
                    LoadingIndicatorService.stopLoading();
                    ErrorMessageService.showMessage("Kon meterstanden niet ophalen");
                }
            );
        }
    }
})();
