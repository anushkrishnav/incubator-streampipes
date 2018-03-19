import {AdvancedSettingsController} from "./advanced-settings.controller";

export let AdvancedSettingsComponent = {
    templateUrl: 'advanced-settings.tmpl.html',
    bindings: {
        disabled : "<"
    },
    transclude: true,
    controller: AdvancedSettingsController,
    controllerAs: 'ctrl'
};