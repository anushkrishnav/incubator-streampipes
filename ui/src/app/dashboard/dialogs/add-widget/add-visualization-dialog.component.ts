/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {AfterViewInit, ChangeDetectorRef, Component, Inject, OnInit} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ElementIconText } from '../../../services/get-element-icon-text.service';
import { Dashboard } from '../../models/dashboard.model';
import { WidgetConfigBuilder } from '../../registry/widget-config-builder';
import { WidgetRegistry } from '../../registry/widget-registry';
import { MappingPropertyGenerator } from '../../sdk/matching/mapping-property-generator';
import { DashboardService } from '../../services/dashboard.service';
import {
    DashboardWidgetModel,
    DashboardWidgetSettings,
    EventPropertyUnion, EventSchema,
    FreeTextStaticProperty,
    MappingPropertyNary,
    MappingPropertyUnary,
    VisualizablePipeline
} from "../../../core-model/gen/streampipes-model";
import {PipelineService} from "../../../platform-services/apis/pipeline.service";
import {FormBuilder, FormGroup} from "@angular/forms";

@Component({
    selector: 'add-visualization-dialog-component',
    templateUrl: './add-visualization-dialog.component.html',
    styleUrls: ['./add-visualization-dialog.component.scss']
})
export class AddVisualizationDialogComponent implements OnInit, AfterViewInit {

    pages = [{
        type: 'select-pipeline',
        title: 'Select Pipeline',
        description: 'Select a pipeline you\'d like to visualize'
    }, {
        type: 'select-widget',
        title: 'Select Widget',
        description: 'Select widget'
    }, {
        type: 'configure-widget',
        title: 'Configure Widget',
        description: 'Configure widget'
    }];

    visualizablePipelines: VisualizablePipeline[] = [];
    availableWidgets: DashboardWidgetSettings[];

    selectedPipeline: VisualizablePipeline;
    selectedWidget: DashboardWidgetSettings;

    dashboard: Dashboard;

    selectedType: any;
    page: any = 'select-pipeline';
    dialogTitle: string;

    parentForm: FormGroup;

    formValid: boolean = false;
    viewInitialized: boolean = false;


    constructor(
        public dialogRef: MatDialogRef<AddVisualizationDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private dashboardService: DashboardService,
        private pipelineService: PipelineService,
        public elementIconText: ElementIconText,
        private fb: FormBuilder,
        private changeDetectorRef: ChangeDetectorRef) {
    }

    ngOnInit() {
        this.parentForm = this.fb.group({});
        this.parentForm.statusChanges.subscribe(status => {
           this.formValid = this.viewInitialized && this.parentForm.valid;
        });
        if (!this.data) {
            this.dialogTitle = 'Add widget';
            this.dashboardService.getVisualizablePipelines().subscribe(visualizations => {
                this.visualizablePipelines = [];
                visualizations.forEach(vis => {
                    this.pipelineService.getPipelineById(vis.pipelineId).subscribe(pipeline => {
                        vis.pipelineName = pipeline.name;
                        this.visualizablePipelines.push(vis);
                        this.sortPipeline();
                    });
                });
            });
        } else {
            this.dialogTitle = 'Edit widget';
            this.selectedPipeline = this.data.pipeline;
            this.selectedWidget = this.data.widget.dashboardWidgetSettings;
            this.page = 'configure-widget';
        }
    }

    ngAfterViewInit() {
        this.viewInitialized = true;
        this.formValid = this.viewInitialized && this.parentForm.valid;
        this.changeDetectorRef.detectChanges();
    }

    sortPipeline() {
        this.visualizablePipelines.sort((a, b) => {
            return a.pipelineName < b.pipelineName ? -1 : 1;
        });
    }

    onCancel(): void {
        this.dialogRef.close();
    }

    getSelectedPipelineCss(vis) {
        return this.getSelectedCss(this.selectedPipeline, vis);
    }

    getSelectedVisTypeCss(type) {
        return this.getSelectedCss(this.selectedType, type);
    }

    getSelectedCss(selected, current) {
        if (selected == current) {
            return 'wizard-preview wizard-preview-selected';
        } else {
            return 'wizard-preview';
        }
    }

    getTabCss(page) {
        if (page == this.page) { return 'md-fab md-accent'; } else { return 'md-fab md-accent wizard-inactive'; }
    }

    selectPipeline(vis) {
        this.selectedPipeline = vis;
        this.next();
    }

    selectWidget(widget) {
        this.selectedWidget = widget;
        this.selectedWidget.config.forEach(sp => {
            if (sp instanceof MappingPropertyUnary || sp instanceof MappingPropertyNary) {
                const requirement: EventPropertyUnion = this.findRequirement(this.selectedWidget.requiredSchema, sp.internalName);
                sp.mapsFromOptions = new MappingPropertyGenerator(requirement, this.selectedPipeline.schema.eventProperties).computeMatchingProperties();
            }
            if (sp instanceof FreeTextStaticProperty && sp.internalName === WidgetConfigBuilder.TITLE_KEY) {
                sp.value = this.selectedPipeline.visualizationName;
            }
        });
        this.next();
    }

    findRequirement(requiredSchema: EventSchema, internalName: string) {
        return requiredSchema.eventProperties.find(ep => ep.runtimeName === internalName);
    }

    next() {
        if (this.page == 'select-pipeline') {
            this.availableWidgets = WidgetRegistry.getCompatibleWidgetTemplates(this.selectedPipeline);
            this.availableWidgets.sort((a, b) => {
                return a.widgetLabel < b.widgetLabel ? -1 : 1;
            });
            this.page = 'select-widget';
        } else if (this.page == 'select-widget') {
            this.page = 'configure-widget';
        } else {
            const configuredWidget: DashboardWidgetModel = new DashboardWidgetModel();
            configuredWidget["@class"] = "org.apache.streampipes.model.dashboard.DashboardWidgetModel";
            configuredWidget.dashboardWidgetSettings = this.selectedWidget;
            configuredWidget.dashboardWidgetSettings["@class"] = "org.apache.streampipes.model.dashboard.DashboardWidgetSettings";
            configuredWidget.visualizablePipelineId = this.selectedPipeline._id;
            configuredWidget.visualizablePipelineTopic = this.selectedPipeline.topic;
            if (!this.data) {
                this.dashboardService.saveWidget(configuredWidget).subscribe(response => {
                    this.dialogRef.close(response);
                });
            } else {
                configuredWidget._id = this.data.widget._id;
                configuredWidget._rev = this.data.widget._rev;
                configuredWidget.widgetId = this.data.widget.widgetId;
                this.dialogRef.close(configuredWidget);
            }
        }
    }

    back() {
        if (this.page == 'select-widget') {
            this.page = 'select-pipeline';
        } else if (this.page == 'configure-widget') {
            this.page = 'select-widget';
        }
    }

    iconText(s) {
        return this.elementIconText.getElementIconText(s);
    }
}
