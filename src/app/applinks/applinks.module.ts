import * as angular from 'angular';

import spServices from '../services/services.module';

import {AppLinksCtrl} from './applinks.controller';

export default angular.module('sp.applinks', [spServices])
    .controller('AppLinksCtrl', AppLinksCtrl)
    .name;
