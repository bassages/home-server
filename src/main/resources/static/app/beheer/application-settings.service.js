(function() {
    'use strict';

    angular
        .module('app')
        .service('ApplicationSettingsService', ApplicationSettingsService);

    function ApplicationSettingsService() {
        // Default values...
        this.displayChartOrTable = 'auto';
        this.displayStroomDalNormaal = 'apart';
    }
})();
