(function() {
    'use strict';

    angular
        .module('app')
        .filter('dateWithAbbreviatedDayname', dateWithAbbreviatedDayname);

    dateWithAbbreviatedDayname.$inject = ['$filter'];

    function dateWithAbbreviatedDayname($filter) {
        return function(input) {
            if(input == null) {
                return '';
            }
            var formatter = d3.time.format('%a %d-%m-%Y');
            return formatter(new Date(input));
        };
    }

})();
