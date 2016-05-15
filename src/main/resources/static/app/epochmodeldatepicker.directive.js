(function() {
    'use strict';

    angular
        .module('app')
        .directive('epochmodeldatepicker', epochmodeldatepicker);

    function epochmodeldatepicker() {
        return {
            restrict: 'A',
            require : 'ngModel',
            link: function(scope, element, attrs, ngModel) {
                function fromUser(text) {
                    var date = d3.time.format('%d-%m-%Y').parse(text);
                    if (date) {
                        return date.getTime();
                    } else {
                        return null;
                    }
                }
                function toUser(epochTimestamp) {
                    if(epochTimestamp) {
                        var date = new Date(epochTimestamp);
                        element.datepicker('setDate', date);
                        return date.toString('dd-MM-yyyy');
                    } else {
                        return null;
                    }
                }
                ngModel.$parsers.push(fromUser);
                ngModel.$formatters.push(toUser);

                element.datepicker({
                    autoclose: true,
                    todayBtn: 'linked',
                    calendarWeeks: true,
                    todayHighlight: true,
                    language: 'nl',
                    orientation: 'bottom left',
                    daysOfWeekHighlighted: '0,6'
                });
            }
        };
    }

})();