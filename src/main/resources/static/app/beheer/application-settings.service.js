(function() {
    'use strict';

    angular
        .module('app')
        .service('ApplicationSettingsService', ApplicationSettingsService);

    function ApplicationSettingsService() {
        this.displayChartOrTable = 'auto';
    }
})();
