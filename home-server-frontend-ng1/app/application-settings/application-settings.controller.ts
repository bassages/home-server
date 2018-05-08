(function() {
    'use strict';

    angular
        .module('app')
        .controller('ApplicationSettingsController', ApplicationSettingsController);

    ApplicationSettingsController.$inject = ['ApplicationSettingsService'];

    function ApplicationSettingsController(ApplicationSettingsService) {
        let vm = this;
        vm.applicationsettings = ApplicationSettingsService;
    }
})();
