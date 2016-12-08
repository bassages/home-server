(function() {
    'use strict';

    angular
        .module('app')
        .service('LoadingIndicatorService', LoadingIndicatorService);

    LoadingIndicatorService.$inject = ['$uibModal'];

    function LoadingIndicatorService($uibModal) {
        var loadingModalInstance = null;

        this.startLoading = function() {
            loadingModalInstance = $uibModal.open({
                animation: false,
                templateUrl: 'app/loading-indicator/loading-indicator-dialog.html',
                size: 'sm',
                backdrop: 'static',
                keyboard: true
            });

            return loadingModalInstance;
        };

        this.stopLoading = function() {
            window.setTimeout(function () {
                if (loadingModalInstance != null) {
                    loadingModalInstance.close();
                    loadingModalInstance = undefined;
                }
            }, 1);
        };
    }
})();
