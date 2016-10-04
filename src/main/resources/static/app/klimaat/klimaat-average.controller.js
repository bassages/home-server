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
            $scope.selection = Date.january().first();
            $scope.dateformat = 'yyyy';

            getDataFromServer();
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            $scope.averagePerMonthInYear = [];

            var month = $scope.selection.clone();

            var requests = [];

            for (var i = 1; i <= 12 ;i++) {
                var dataUrl = 'rest/klimaat/gemiddelde?from=' + month.getTime() + '&to=' + month.clone().add(1).month().getTime() + '&sensortype=' + $scope.sensortype;
                $log.info('Getting data from URL: ' + dataUrl);
                requests.push( $http( { method: 'GET', url: dataUrl } ) );
                month = month.clone().add(1).month();
            }

            $q.all(requests).then(
                function successCallback(response) {
                    for (var i = 0; i < requests.length; i++) {
                        var dateString = $scope.selection.getFullYear() + '-' + (i + 1) + '-1';
                        $log.info(response[i].data);
                        $scope.averagePerMonthInYear.push({date: new Date(dateString), average: response[i].data ? response[i].data : null});
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

        $scope.datepickerPopupOptions = {
            maxDate: Date.today(),
            datepickerMode: 'year',
            minMode: 'year'
        };

        $scope.datepickerPopup = {
            opened: false
        };

        $scope.toggleDatepickerPopup = function() {
            $scope.datepickerPopup.opened = !$scope.datepickerPopup.opened;
        };

        $scope.datepickerChange = function() {
            getDataFromServer();
        };
    }

})();

