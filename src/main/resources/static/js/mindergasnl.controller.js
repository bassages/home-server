(function() {
    'use strict';

    angular
        .module('app')
        .controller('MindergasnlController', MindergasnlController);

    MindergasnlController.$inject = ['$scope', '$log', 'LoadingIndicatorService', 'ErrorMessageService'];

    function MindergasnlController($scope, $log, LoadingIndicatorService, ErrorMessageService) {

        function activate() {

        }

        activate();

        $scope.save = function() {

        };

        function handleServiceError(message, errorResult) {
            $log.error(message + ' Cause=' + JSON.stringify(errorResult));
            ErrorMessageService.showMessage(message);
        }

        function handleTechnicalError(details) {
            var message = 'Er is een onverwachte fout opgetreden.';
            $log.error(message + ' ' + details);
            ErrorMessageService.showMessage(message);
        }
    }
})();
