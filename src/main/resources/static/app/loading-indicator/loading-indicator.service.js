(function() {
    'use strict';

    angular
        .module('app')
        .service('LoadingIndicatorService', LoadingIndicatorService);

    LoadingIndicatorService.$inject = ['$uibModal', '$log', '$q'];

    function LoadingIndicatorService($uibModal, $log, $q) {
        var loadingModalInstance = null;

        this.startLoading = function(functionToCallWhenDialogIsOpened) {
            loadingModalInstance = $uibModal.open({
                animation: false,
                templateUrl: 'app/loading-indicator/loading-indicator-dialog.html',
                size: 'sm',
                backdrop: 'static'
            });

            $q.all(loadingModalInstance.opened, loadingModalInstance.rendered).then(
                function successCallback(response) {
                    $log.info("Opened and rendered");

                    if (functionToCallWhenDialogIsOpened && typeof functionToCallWhenDialogIsOpened === 'function') {
                        functionToCallWhenDialogIsOpened();
                    }
                },
                function errorCallback(response) {
                    $log.info("Failed open and/or render");
                });
        };

        this.stopLoading = function() {
            loadingModalInstance.close();
            loadingModalInstance = null;

            // Workaround for issue where somtimes the dialog is nog closed
            angular.element(document).find('.modal-backdrop').remove();
            angular.element(document).find('.modal').remove();
        };
    }
})();
