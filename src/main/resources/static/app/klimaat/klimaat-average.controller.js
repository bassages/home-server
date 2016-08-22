(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatAverageController', KlimaatAverageController);

    KlimaatAverageController.$inject = ['$scope', '$http', '$q', '$log', '$routeParams', 'KlimaatService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function KlimaatAverageController($scope, $http, $q, $log, $routeParams, KlimaatService, LoadingIndicatorService, ErrorMessageService) {
        activate();

        function activate() {
            $scope.sensortype = $routeParams.sensortype;
            $scope.unitlabel = KlimaatService.getUnitlabel($scope.sensortype);

            $scope.from = Date.january().first();
            $scope.to = Date.today();

            $scope.dateformat = 'EEEE. dd-MM-yyyy';

            getDataFromServer();
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var dataUrl = 'rest/klimaat/gemiddelde?from=' + $scope.from.getTime() + '&to=' + $scope.to.clone().add(1).day().getTime() + '&sensortype=' + $scope.sensortype;
            $log.info('Getting data from URL: ' + dataUrl);
            $http( { method: 'GET', url: dataUrl } )
                .then(
                    function successCallback(response) {
                        $scope.gemiddelde = response.data;
                        LoadingIndicatorService.stopLoading();
                    },
                    function errorCallback(response) {
                        LoadingIndicatorService.stopLoading();
                        handleServiceError('Ophalen van gegevens is niet gelukt.', response);
                    }
                );
        }

        function handleServiceError(message, errorResult) {
            $log.error(message + ' Cause=' + JSON.stringify(errorResult));
            ErrorMessageService.showMessage(message);
        }

        $scope.datepickerPopupFromOptions = {
            maxDate: Date.today()
        };

        $scope.datepickerPopupFrom = {
            opened: false
        };

        $scope.toggleDatepickerPopupFrom = function() {
            $scope.datepickerPopupFrom.opened = !$scope.datepickerPopupFrom.opened;
        };

        $scope.datepickerPopupToOptions = {
            maxDate: Date.today()
        };

        $scope.datepickerPopupTo = {
            opened: false
        };

        $scope.toggleDatepickerPopupTo = function() {
            $scope.datepickerPopupTo.opened = !$scope.datepickerPopupTo.opened;
        };

        $scope.fromChange = function() {
            getDataFromServer();
        };

        $scope.toChange = function() {
            getDataFromServer();
        };

        $scope.limitChange = function() {
            getDataFromServer();
        };
    }

})();

