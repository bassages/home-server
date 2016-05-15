'use strict';

/**
 * Bootstrap-toggle Directive
 * Forked from from: https://gist.github.com/dave-newson/f6c5e9c2f3bc315e292c
 * This version supports ngDisabled directive.
 *
 * @link https://gist.github.com/jjmontesl/54457bf1342edeb218b7
 */
angular
    .module('app')
    .directive('togglecheckbox', function() {

        return {
            restrict: 'A',
            transclude: true,
            replace: false,
            require: 'ngModel',
            link: function ($scope, $element, $attr, require) {

                var ngModel = require;

                // update model from Element
                var updateModelFromElement = function() {
                    // If modified
                    var checked = $element.prop('checked');
                    if (checked != ngModel.$viewValue) {
                        // Update ngModel
                        ngModel.$setViewValue(checked);
                        $scope.$apply();
                    }
                };

                // Update input from Model
                var updateElementFromModel = function() {
                    // Update button state to match model
                    var state = ! $($element).attr('disabled');
                    $($element).bootstrapToggle("enable");
                    $element.trigger('change');
                    $($element).bootstrapToggle(state ? "enable" : "disable");
                };

                // Observe: Element changes affect Model
                $element.on('change', function() {
                    updateModelFromElement();
                });

                // Observe: ngModel for changes
                $scope.$watch(function() {
                    return ngModel.$viewValue;
                }, function() {
                    updateElementFromModel();
                });

                // Observe: disabled attribute set by ngDisabled
                $scope.$watch(function() {
                    return $($element).attr('disabled');
                }, function(newVal) {
                    $($element).bootstrapToggle(! newVal ? "enable" : "disable");
                });

                // Initialise BootstrapToggle
                $element.bootstrapToggle();

            }
        };
    });