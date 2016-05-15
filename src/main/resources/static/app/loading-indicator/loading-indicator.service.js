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
                backdrop: 'static',
                controller: function ($scope) {}
            });
        };

        this.stopLoading = function() {
            $log.debug('Closing : ' + JSON.stringify(loadingModalInstance));
            loadingModalInstance.close();
            $log.debug('Closed  : ' + JSON.stringify(loadingModalInstance));

            loadingModalInstance = null;
        };
    }
})();
