(function() {
    'use strict';

    angular
        .module('app')
        .service('ErrorMessageService', ErrorMessageService);

    ErrorMessageService.$inject = ['$uibModal'];

    function ErrorMessageService($uibModal) {
        var modalDialogInstance = null;

        this.showMessage = function(message) {

            $uibModal.open({
                animation: false,
                templateUrl: 'error-dialog.html',
                backdrop: 'static',
                controller: function($scope, message) {
                    $scope.message = message;

                    $scope.confirm = function () {
                        $scope.$close();
                    };
                },
                resolve: {
                    message: function () {
                        return message;
                    }
                }
            });
        };
    }
})();
