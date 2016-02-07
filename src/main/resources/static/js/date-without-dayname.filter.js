(function() {
    'use strict';

    angular
        .module('app')
        .filter('dateWithoutDayname', datewithoutdayname);

    datewithoutdayname.$inject = ['$filter'];

    function datewithoutdayname($filter) {
        return function(input) {
            if(input == null) {
                return "";
            }
            var formatter = d3.time.format('%d-%m-%Y');
            return formatter(new Date(input));
        };
    }

})();