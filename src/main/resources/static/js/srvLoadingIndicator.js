(function() {
    'use strict';

    angular
        .module('appHomecontrol')
        .service('LoadingIndicatorService', ['$uibModal', LoadingIndicatorService]);

    function LoadingIndicatorService($uibModal) {
        var loadingModalInstance = null;

        this.startLoading = function() {
            this.loadingModalInstance = $uibModal.open({
                animation: false,
                templateUrl: 'loading-dialog.html',
                size: 'sm',
                backdrop: 'static',
                controller: function ($scope) {}
            });
        };

        this.stopLoading = function() {
            this.loadingModalInstance.close();
            this.loadingModalInstance = null;
        };
    }
})();
