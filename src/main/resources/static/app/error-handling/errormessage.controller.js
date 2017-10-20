(function() {
    'use strict';

    angular
        .module('app')
        .controller('ErrorMessageController', ErrorMessageController);

    ErrorMessageController.$inject = ['$uibModalInstance', 'message'];

    function ErrorMessageController($uibModalInstance, message) {
        var vm = this;

        vm.message = message;
        vm.confirm = confirm;

        function confirm() {
            $uibModalInstance.close();
        }
    }

})();
