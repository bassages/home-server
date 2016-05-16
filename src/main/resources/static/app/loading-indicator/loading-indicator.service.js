(function() {
    'use strict';

    angular
        .module('app')
        .service('LoadingIndicatorService', LoadingIndicatorService);

    LoadingIndicatorService.$inject = ['$uibModal', '$log', '$q'];

    function LoadingIndicatorService($uibModal, $log, $q) {
        var loadingModalInstance = null;

        this.startLoading = function() {
            loadingModalInstance = $uibModal.open({
                animation: false,
                templateUrl: 'app/loading-indicator/loading-indicator-dialog.html',
                size: 'sm',
                backdrop: 'static'
            });

            $q.all(loadingModalInstance.opened).then(
                function successCallback(response) {
                    $log.info("Opened")
                },
                function errorCallback(response) {
                    $log.info("Failed open")
                });

            $q.all(loadingModalInstance.rendered).then(
                function successCallback(response) {
                    $log.info("Rendered")
                },
                function errorCallback(response) {
                    $log.info("Failed render")
                });

            $q.all(loadingModalInstance.closed).then(
                function successCallback(response) {
                    $log.info("Closed")
                },
                function errorCallback(response) {
                    $log.info("Failed close")
                });
        };

        this.stopLoading = function() {
            $log.info("Closing modal");

            loadingModalInstance.opened.then(loadingModalInstance.close());
            loadingModalInstance = null;

            $log.info("Closed modal");
        };
    }
})();
