(function() {
    'use strict';

    angular
        .module('app')
        .filter('datewithoutdayname', datewithoutdayname);

    datewithoutdayname.$inject = ['$filter'];

    function datewithoutdayname($filter) {
        return function(input) {
            if(input == null){ return ""; }
            var _date = $filter('date')(new Date(input), 'dd-MM-yyyy');
            return _date.toUpperCase();
        };
    }

})();