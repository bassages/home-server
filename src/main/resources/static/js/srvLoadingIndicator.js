'use strict';

angular.module('appHomecontrol.loadingIndicatorService', [])

    .service('LoadingIndicatorService', ['$uibModal', function($uibModal) {
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
    }]);
