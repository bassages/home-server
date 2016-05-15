(function() {
    'use strict';

    angular
        .module('app')
        .service('LoadingIndicatorService', LoadingIndicatorService);

    LoadingIndicatorService.$inject = ['$uibModal', '$log'];

    function LoadingIndicatorService($uibModal, $log) {
        var loadingModalInstance = null;

        this.startLoading = function() {
            loadingModalInstance = $uibModal.open({
                animation: false,
                templateUrl: 'app/loading-indicator/loading-indicator-dialog.html',
                size: 'sm',
                backdrop: 'static'
            });
        };

        this.stopLoading = function() {
            loadingModalInstance.close();
            loadingModalInstance = null;
        };
    }
})();
