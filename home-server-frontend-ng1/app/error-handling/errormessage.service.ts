(function() {
    'use strict';

    angular
        .module('app')
        .service('ErrorMessageService', ErrorMessageService);

    ErrorMessageService.$inject = ['$uibModal'];

    function ErrorMessageService($uibModal) {

        this.showMessage = function(message) {

            $uibModal.open(
                {
                    animation: false,
                    templateUrl: 'app/error-handling/errormessage-dialog.html',
                    backdrop: 'static',
                    controllerAs: 'vm',
                    controller: 'ErrorMessageController',
                    resolve: { message: function () { return message; } }
                }
            );
        };
    }
})();
