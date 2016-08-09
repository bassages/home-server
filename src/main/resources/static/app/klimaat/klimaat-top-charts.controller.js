(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatTopChartsController', KlimaatTopChartsController);

    KlimaatTopChartsController.$inject = ['$scope', '$http', '$q', '$log', '$routeParams', 'KlimaatService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function KlimaatTopChartsController($scope, $http, $q, $log, $routeParams, KlimaatService, LoadingIndicatorService, ErrorMessageService) {
        activate();

        function activate() {
            $scope.sensortype = $routeParams.sensortype;
            $scope.unitlabel = KlimaatService.getUnitlabel($scope.sensortype);

            $scope.limits = [5, 10, 25, 50, 100];
            $scope.limit = 5;

            getDataFromServer();
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var requests = [];

            requests.push(getTopChartData('hoogste'));
            requests.push(getTopChartData('laagste'));

            $q.all(requests).then(
                function successCallback(responses) {
                    $scope.laagste = getResponseData(responses, 'laagste');
                    $scope.hoogste = getResponseData(responses, 'hoogste');
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
            var from = Date.january().first();
            var to = from.clone().addYears(1);
            return $http( { method: 'GET', url: 'rest/klimaat/' + charttype + '?from=' + from.getTime() + '&to=' + to.getTime() + '&sensortype=' + $scope.sensortype + '&limit=' + $scope.limit } );
        }

        function handleServiceError(message, errorResult) {
            $log.error(message + ' Cause=' + JSON.stringify(errorResult));
            ErrorMessageService.showMessage(message);
        }

        $scope.limitUpdate = function() {
            getDataFromServer();
        }
    }

})();

