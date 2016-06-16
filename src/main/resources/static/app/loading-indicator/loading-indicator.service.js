(function() {
    'use strict';

    angular
        .module('app')
        .service('LoadingIndicatorService', LoadingIndicatorService);

    LoadingIndicatorService.$inject = ['$uibModal', '$log', '$q'];

    function LoadingIndicatorService($uibModal, $log, $q) {
        //removeModalElements();
        var loadingModalInstance = null;

        this.startLoading = function() {
            loadingModalInstance = $uibModal.open({
                animation: false,
                templateUrl: 'app/loading-indicator/loading-indicator-dialog.html',
                size: 'sm',
                backdrop: 'static',
                keyboard: false
            });

            //$q.all(loadingModalInstance.opened, loadingModalInstance.rendered).then(
            //    function successCallback(response) {
            //        $log.info("Opened and rendered");
            //    },
            //    function errorCallback(response) {
            //        $log.info("Failed open and/or render");
            //    });

            return loadingModalInstance;
        };

        //function removeModalElements() {
        //    if (!loadingModalInstance) {
        //        // Workaround for issue where somtimes the dialog is not closed
        //        var modalBackdrop = angular.element(document).find('.modal-backdrop');
        //        if (modalBackdrop.length > 0) {
        //            modalBackdrop.hide();
        //            $log.info("Hidden backdrop");
        //        }
        //        var modal = angular.element(document).find('.modal');
        //        if (modal.length > 0) {
        //            modal.hide();
        //            $log.info("Hidden modal");
        //        }
        //        setTimeout(removeModalElements, 1000);
        //    }
        //}

        this.stopLoading = function() {
            if (loadingModalInstance != null) {
                loadingModalInstance.close();
                loadingModalInstance = null;
            }
        };
    }
})();
