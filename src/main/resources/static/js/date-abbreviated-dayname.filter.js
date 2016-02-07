(function() {
    'use strict';

    angular
        .module('app')
        .filter('abbreviatedDatename', abbreviatedDatename);

    abbreviatedDatename.$inject = ['$filter'];

    function abbreviatedDatename($filter) {
        return function(input) {
            if(input == null){ return ""; }
            var formatter = d3.time.format('%a');
            return formatter(new Date(input));
        };
    }

})();