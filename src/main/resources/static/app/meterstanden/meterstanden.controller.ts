(function() {
    'use strict';

    angular
        .module('app')
        .controller('MeterstandenController', MeterstandenController);

    MeterstandenController.$inject = ['$log', 'MeterstandenService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function MeterstandenController($log, MeterstandenService, LoadingIndicatorService, ErrorMessageService) {
        var vm = this;

        vm.isMaxSelected = isMaxSelected;
        vm.navigate = navigate;
        vm.toggleDatepickerPopup = toggleDatepickerPopup;

        vm.selection = Date.today().clearTime().moveToFirstDayOfMonth();
        vm.dateformat = 'MMMM yyyy';
        vm.datepickerPopupOptions = { datepickerMode: 'month', minMode: 'month', maxDate: Date.today() };
        vm.datepickerPopup = { opened: false };

        activate();

        function activate() {
            getDataFromServer();
        }

        function isMaxSelected() {
            return Date.today().getMonth() === vm.selection.getMonth() && Date.today().getFullYear() === vm.selection.getFullYear();
        }

        function navigate(numberOfPeriods) {
            vm.selection = vm.selection.clone().add(numberOfPeriods).months();
            getDataFromServer();
        }

        function toggleDatepickerPopup() {
            vm.datepickerPopup.opened = !vm.datepickerPopup.opened;
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var van = vm.selection;
            var tot = vm.selection.clone().addMonths(1);

            MeterstandenService.getMeterstandenPerDagInPeriod(van, tot)
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
