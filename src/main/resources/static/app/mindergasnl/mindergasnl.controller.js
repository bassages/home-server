(function() {
    'use strict';

    angular
        .module('app')
        .controller('MindergasnlController', MindergasnlController);

    MindergasnlController.$inject = ['$log', 'MindergasnlService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function MindergasnlController($log, MindergasnlService, LoadingIndicatorService, ErrorMessageService) {
        var vm = this;

        vm.save = save;

        activate();

        function activate() {
            LoadingIndicatorService.startLoading();

            MindergasnlService.query(
                function(data) {
                    if (data.length === 0) {
                        vm.settings = new MindergasnlService({automatischUploaden: false, authenticatietoken: ''});
                    } else {
                        vm.settings = data;
                    }
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResponse) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Ophalen van gegevens is niet gelukt.', errorResponse);
                }
            );
        }

        function save() {
            LoadingIndicatorService.startLoading();

            $log.info('Save Mindergas.nl settings: ' + angular.toJson(vm.settings));

            vm.settings.$save(
                function(successResult) {
                    vm.settings.id = successResult.id;
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Opslaan is niet gelukt.', errorResult);
                }
            );
        }

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
