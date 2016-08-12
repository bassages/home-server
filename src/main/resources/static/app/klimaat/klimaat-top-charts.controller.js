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
            $scope.limit = 10;

            $scope.from = Date.january().first();
            $scope.to = $scope.from.clone().addYears(1);

            $scope.dateformat = 'EEEE. dd-MM-yyyy';

            getDataFromServer();
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var requests = [
                getTopChartData('hoogste'),
                getTopChartData('laagste')
            ];

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
            var url = 'rest/klimaat/' + charttype + '?from=' + $scope.from.getTime() + '&to=' + $scope.to.add(1).day().getTime() + '&sensortype=' + $scope.sensortype + '&limit=' + $scope.limit;
            $log.info('Getting data from URL: ' + dataUrl);
            return $http( { method: 'GET', url: url } );
        }

        function handleServiceError(message, errorResult) {
            $log.error(message + ' Cause=' + JSON.stringify(errorResult));
            ErrorMessageService.showMessage(message);
        }

        $scope.$watch("from", function(newValue, oldValue) {
            if (oldValue.getTime() != newValue.getTime()) {
                getDataFromServer();
            }
        });
        $scope.$watch("to", function(newValue, oldValue) {
            if (oldValue.getTime() != newValue.getTime()) {
                getDataFromServer();
            }
        });

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

        $scope.limitUpdate = function() {
            getDataFromServer();
        };
    }

})();

