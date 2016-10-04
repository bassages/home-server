(function() {
    'use strict';

    angular
        .module('app')
        .controller('MindergasnlController', MindergasnlController);

    MindergasnlController.$inject = ['$scope', '$log', 'MindergasnlService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function MindergasnlController($scope, $log, MindergasnlService, LoadingIndicatorService, ErrorMessageService) {

        function activate() {
            LoadingIndicatorService.startLoading();

            MindergasnlService.query(
                function(data) {
                    if (data.length == 0) {
                        $scope.settings = new MindergasnlService({automatischUploaden: false, authenticatietoken: ''});
                    } else {
                        $scope.settings = data[0];
                    }
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResponse) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Ophalen van gegevens is niet gelukt.', errorResponse);
                }
            );
        }

        activate();

        $scope.save = function() {
            LoadingIndicatorService.startLoading();

            $log.info('Save Mindergas.nl settings: ' + angular.toJson($scope.settings));

            $scope.settings.$save(
                function(successResult) {
                    $scope.settings.id = successResult.id;
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Opslaan is niet gelukt.', errorResult);
                }
            );

        };

        function handleServiceError(message, errorResult) {
            $log.error(message + ' Cause=' + angular.toJson(errorResult));
            ErrorMessageService.showMessage(message);
        }

        function handleTechnicalError(details) {
            var message = 'Er is een onverwachte fout opgetreden.';
            $log.error(message + ' ' + details);
            ErrorMessageService.showMessage(message);
        }
    }
})();
