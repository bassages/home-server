(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatTopChartsController', KlimaatTopChartsController);

    KlimaatTopChartsController.$inject = ['$scope', '$http', '$q', '$log', '$routeParams', 'LoadingIndicatorService', 'ErrorMessageService'];

    function KlimaatTopChartsController($scope, $http, $q, $log, $routeParams, LoadingIndicatorService, ErrorMessageService) {
        $scope.soort = $routeParams.soort;

        activate();

        function activate() {
            LoadingIndicatorService.startLoading();

            var requests = [];

            requests.push($http({
                method: 'GET', url: 'rest/klimaat/hoogste?sensortype=' + $scope.soort + '&limit=15'
            }));
            requests.push($http({
                method: 'GET', url: 'rest/klimaat/laagste?sensortype=' + $scope.soort + '&limit=15'
            }));

            $q.all(requests).then(
                function successCallback(responses) {
                    // TODO...

                    for (var i = 0, len = responses.length; i < len; i++) {
                        if (responses[i].config.url.indexOf('laagste') !== -1 ) {
                            $scope.laagste = responses[i].data;
                        } else if (responses[i].config.url.indexOf('hoogste') !== -1 ) {
                            $scope.hoogste = responses[i].data;
                        }
                    }

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
    }

})();

