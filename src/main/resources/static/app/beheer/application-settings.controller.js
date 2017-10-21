(function() {
    'use strict';

    angular
        .module('app')
        .controller('ApplicationSettingsController', ApplicationSettingsController);

    ApplicationSettingsController.$inject = ['ApplicationSettingsService'];

    function ApplicationSettingsController(ApplicationSettingsService) {
        var vm = this;

        vm.settings = ApplicationSettingsService;
    }
})();
