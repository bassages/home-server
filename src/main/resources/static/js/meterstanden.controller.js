(function() {
    'use strict';

    angular
        .module('app')
        .controller('MeterstandenController', MeterstandenController);

    MeterstandenController.$inject = ['$scope', '$log', 'MeterstandenService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function MeterstandenController($scope, $log, MeterstandenService, LoadingIndicatorService, ErrorMessageService) {

        function activate() {
            LoadingIndicatorService.startLoading();

            var van = Date.today().clearTime().moveToFirstDayOfMonth().getTime();
            var totEnMet = Date.today().clearTime().moveToLastDayOfMonth().setHours(23, 59, 59, 999);

            MeterstandenService.getMeterstandenPerDagInPeriod(van, totEnMet)
                .then(
                    function successCallback(response) {
                        $scope.meterstandenPerDag = response.data;
                        LoadingIndicatorService.stopLoading();
                    },
                    function errorCallback(response) {
                        LoadingIndicatorService.stopLoading();
                        ErrorMessageService.showMessage("Kon meterstanden niet ophalen");
                    }
                );
        }

        activate();
    }
})();
