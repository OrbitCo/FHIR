import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { FormArray, FormBuilder, AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { DashboardCommonService } from '../../services/dashboard-common.service';
import { ProjectService } from '../../services/project/project.service';
import { StatusCodes } from 'http-status-codes'
import config from 'app/modules/constants/common-constants';
import { RequestModel } from '../../models/request.model';
import {AbstractConstructor} from "@angular/material/core/common-behaviors/constructor";
import { items } from 'app/mock-api/apps/file-manager/data';
import * as fileSaver from 'file-saver';
import { MatOption } from '@angular/material/core';
import {AuthModel} from "../../models/auth.model";
import {FHIRExportModel} from "../../models/FHIRExport.model";
import {project} from "../../../../mock-api/dashboards/project/data";
import { CSVModel } from '../../models/CSV.model';
import {EmailModel} from "../../models/email.model";
import { SFTPModel } from '../../models/SFTP.model';
import {ExportModel} from "../../models/export.model";
@Component({
    selector: 'create-project',
    templateUrl: './create-project.component.html',
    styleUrls: ['./create-project.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CreateProjectComponent implements OnInit, OnDestroy {
    authentication;
    partnerNames: any[] = [];
    queryTypeJsonData: any;
    singleProjectData: any;
    isEdit: boolean = false;
    jsonData: any;
    private intervalId: any = null;
    private loopCount = 0;
    private intervalLength = 2000;
    private windowHandle: Window;
    authorizationCode: string;
    oAuthToken: string;
    oAuthVerifier: string;
    tokenRetrievalStatus: string;
    dataRetrievalStatus: string;
    createProjectPartnerId: any;
    initialRequest: RequestModel = {
        order: 1,
        returnValue: null,
        connection: null,
        query: null,
        type: "full",
        authentication: null
    }
    requests: RequestModel[] = [this.initialRequest];
    initialAuth: AuthModel = {
        name: "No Authentication",
        authenticationType: "NoAuth",
        authorizationEndpoint: null,
        clientCode: null,
        clientId: null,
        clientScope: null,
        clientSecret: null,
        grantType: null,
        redirectUrls: null,
        tokenEndpoint: null
    }
    firstAuth: AuthModel = {
        name: null,
        authenticationType: "NoAuth",
        authorizationEndpoint: null,
        clientCode: null,
        clientId: null,
        clientScope: null,
        clientSecret: null,
        grantType: null,
        redirectUrls: null,
        tokenEndpoint: null
    }
    authentications: AuthModel[] = [this.initialAuth, this.firstAuth];

    private snackBarConf: MatSnackBarConfig = {
        duration: 0,
        horizontalPosition: 'center',
        verticalPosition: 'top',
    };

    authFormControls: string[] = [
        "authName", "authType", "grantType", "authorizationEndpoint", "tokenEndpoint", "clientId", "clientSecret", "redirectUrls"
    ]

    outputList: any[] = [
        { name: 'FHIR Export', value: 'FHIRExport'},
        { name: 'CSV', value: 'csv'},
        { name: 'JSON', value: 'json'},
        { name: 'Script', value: 'script'}
    ];

    //FHIR Export Members
    FHIRExportAuthentication;
    FHIRExportRetrievalStatus: string;

    sendMethods: any[] = [
        { name: 'Email', value: 'email' },
        { name: 'SFTP', value: 'sftp' }
    ];

    FHIRExportMethods: any[] = [
        { name: 'Transaction', value: 'transaction' },
        { name: 'Batch', value: 'batch' }
    ];

    columnsList: any[] = [
        { name: 'Patient Id', value: 'patientid' },
        { name: 'First Name', value: 'firstname' },
        { name: 'Last Name', value: 'lastname' },
        { name: 'Gender', value: 'gender' },
        { name: 'Birth Date', value: 'birthdate' },
        { name: 'Phone Number', value: 'phonenumber' },
        { name: 'Email', value: 'email' },
        { name: 'Address', value: 'address' },
        { name: 'Is Active', value: 'isactive' },
        { name: 'Is Deceased', value: 'isdeceased' },
    ];

    basicColumnsList: any =[]

    fhtrBlock: boolean;
    csvBlock : boolean;
    jsonBlock: boolean;
    csvEmailBlock: boolean;
    csvSftpBlock:boolean;
    checksCSVUploadBtn: boolean = true;

    csvPrivateKeyFile: any;
    csvKeyBase64textString: string;

    @ViewChild('allSelected') private allSelected: MatOption;
    createProjectForm = new FormGroup(
        {
            projectName: new FormControl('', Validators.required),
            partnerId: new FormControl('', Validators.required),
            contactNumber: new FormControl('', Validators.required),
            email: new FormControl('', [Validators.required, Validators.email, Validators.pattern(config.EMAIL_PATTERN)]),
            description: new FormControl('', [Validators.maxLength(250)]),

            //QUERY MEMBERS
            connection1: new FormControl('', [Validators.required,
                this.forbiddenInputValidator(/.*(?<!\/)$/, "Connection must end with a slash (/)")]),
            FHIRQueryAuthentication1: new FormControl('', []),
            query1: new FormControl('', [Validators.required,
                this.forbiddenInputValidator(/^\/.*/, "Query must not start with a slash (/)")]),
            status: new FormControl('', Validators.required),
            queryTypeConnection: new FormControl(''),
            queryTypeQuery: new FormControl(''),
            returnValue: new FormControl(''),
            order: new FormArray([]),

            //EXPORT MEMBERS
            projectOutputList: this.fb.array([]) ,
            csvName: new FormControl(''),
            csvEmailAddress: new FormControl(''),
            fhirPaths: this.fb.array([]) ,
            csvEmailBody: new FormControl(''),
            csvEmailSubject: new FormControl(''),
            csvSftpServer: new FormControl(''),
            csvSftpPortNumber: new FormControl(''),
            csvSftpDirectory: new FormControl(''),
            csvSftpUser: new FormControl(''),
            csvSftpPassword: new FormControl(''),
            coulumspath: new FormControl(''),
            basicCsvName: new FormControl(''),
            basicCoulumspath: new FormControl(''),
            basicResponseUri: new FormControl(''),
            basicResponsePort: new FormControl(''),
            exportJson: new FormControl('')
        })

    private _unsubscribeAll: Subject<any> = new Subject<any>();
    outputCheckBoxValues: any = [];
    sendMethodValues: any = [];
    FHIRExportValues: any = [];
    exportCheckboxValue: any = [];
    FHIRExportMethodscheckbox: any = [];
    CSVSendMethodsCheckbox: any = [];
    buttonFlag: boolean = true;
    projectId: any;
    projectOutputIds: any = [];
    isDOwnloadCSVBtn: boolean = true;
    patientData: any;
    basicQueryjsonData: any;
    basicQueryTypeJsonData: any;
    basicSubmitResponsebtn: boolean = true;
    basicQueryTypeStringyfyData: any;
    baiscPatientData: any;
    isBasicDOwnloadCSVBtn: boolean = true;
    patientIds: any = [];

    checkColumnBlock: boolean = false;
    checkFhirPathBlock: boolean = false;


    constructor(
        private _dashboardCommonService: DashboardCommonService,
        public _projectService: ProjectService,
        private router: Router,
        private snackBar: MatSnackBar,
        private cd: ChangeDetectorRef,
        private fb: FormBuilder
    ) {
    }

    ngOnInit(): void {
        this.getPartnerNames();
        this.createAuthorizationFormControls("1");
        if(!this.isEdit) {
            this.fhirPaths.push(
                this.fb.group({
                    basicCoulumspath: new FormControl(''),
                })
            );

        }
    }

    get fhirPaths(): FormArray {
        return this.createProjectForm.get("fhirPaths") as FormArray
    }


    newQuantity(): FormGroup {
        return this.fb.group({
            path: ''
        })
    }

    addColumn() {
        this.fhirPaths.push(this.newQuantity());
    }

    removeColumn(i: number) {
        this.fhirPaths.removeAt(i);
    }

    outputCheckboxChange(data, event) {
        if ( data.value == 'FHIRExport' ) {
            if(event.checked) { //Enable/Disable the FHIR Import html block
                this.enableFHIRBlock();
            } else {
                this.disableFHIRBlock();
            }
        } else if (data.value == 'csv' ) {
            if(event.checked) {
                this.enableCSVBlock();
            } else {
                this.disableCSVBlock();
            }
        } else if (data.value == 'json') {
            this.jsonBlock = event.checked;
        }

        if (data.value == 'FHIRExport' || data.value == 'csv' || data.value == 'json' || data.value == 'script') {
            //If it was checked, add it to the output array
            if (event.checked == true) {
                let outputTask ={
                    name: data.value
                }
                this.outputCheckBoxValues.push(outputTask)
            } else {
                //If it was unchecked, find and remove it from the output array
                const index = this.outputCheckBoxValues.findIndex(obj => obj.name==data.value);
                if (index > -1) {
                    this.outputCheckBoxValues.splice(index, 1);
                }
            }
        }
    }

    /**
     * Creates the form controls for the FHIR Import block, and then enables it.
     */
    enableFHIRBlock() {
        this.exportCheckboxValue.push('FHIRExport');

        this.createProjectForm.addControl('FHIRExportAuthentication',
            new FormControl('',[Validators.required]));
        this.createProjectForm.addControl('FHIRExportAddress',
            new FormControl('',[Validators.required]));
        this.createProjectForm.addControl('FHIRExportOpType',
            new FormControl('',[Validators.required]));
        this.createAuthorizationFormControls("FHIRExport");

        this.createProjectForm.get("FHIRExportOpType").setValue("TRNSC");

        this.FHIRExportAuthentication = "NoAuth"; //Set the default
        this.createProjectForm.get('FHIRExportAuthentication').setValue('NoAuth');

        this.fhtrBlock = true;
    }

    /**
     * Disables the FHIR Import block, then destroys the form controls.
     */
    disableFHIRBlock() {
        this.fhtrBlock = false;
        this.createProjectForm.removeControl('FHIRExportAuthentication');
        this.createProjectForm.removeControl('FHIRExportAddress');
        this.createProjectForm.removeControl('FHIRExportOpType');
        this.removeAuthorizationFormControls("FHIRExport");

        const index = this.exportCheckboxValue.indexOf("FHIRExport");
        if (index > -1) {
            this.exportCheckboxValue.splice(index, 1);
        }
    }

    enableCSVBlock() {
        this.exportCheckboxValue.push('csv');

        //TODO: Form Validation

        this.csvBlock = true;
    }

    onCSVMethodSelection(data, event) {
        if ( data.value == 'email' ) {
            if (event.checked) {
                this.CSVSendMethodsCheckbox.push(data.value);
                this.csvEmailBlock = true;
            } else {
                this.csvEmailBlock = false;
                const index = this.CSVSendMethodsCheckbox.indexOf(data.value);
                if (index > -1) {
                    this.CSVSendMethodsCheckbox.splice(index, 1);
                }
            }
        } else if (data.value == 'sftp' ) {
            if (event.checked) {
                this.CSVSendMethodsCheckbox.push(data.value);
                this.csvSftpBlock = true;
            } else {
                this.csvSftpBlock = false;
                const index = this.CSVSendMethodsCheckbox.indexOf(data.value);
                if (index > -1) {
                    this.CSVSendMethodsCheckbox.splice(index, 1);
                }
            }
        }
    }


    assigningColumns() {
        let basicColumns = [];
        if(!this.createProjectForm.value.basicCoulumspath) {
            this.createProjectForm.value.fhirPaths.forEach(element => {
                return basicColumns.push(element.basicCoulumspath);
            });
        } else {
            return  basicColumns = this.createProjectForm.value.basicCoulumspath;
        }
    }

    downloadCSV() {
        let data = this.buildCSVModel();
        console.log(data)
        this._dashboardCommonService.downloadCSVFile(data).subscribe(
            (res) => {
                const blob = new Blob([res], {type: 'text/csv'});
                fileSaver.saveAs(blob, `${this.createProjectForm.value.basicCsvName}.csv`);
            },
            (error) => {
                console.log(error)
            }
        );
    }

    checkCSVUploadField() {
        if(this.createProjectForm.value.csvSftpServer && this.createProjectForm.value.csvSftpPortNumber && this.createProjectForm.value.csvSftpDirectory && this.createProjectForm.value.csvSftpUser && this.createProjectForm.value.basicCsvName && this.createProjectForm.value.basicCoulumspath && this.csvPrivateKeyFile) {
            this.checksCSVUploadBtn = false;
        } else {
            this.checksCSVUploadBtn = true;
        }
    }

    checkCSVButton() {
        let csvName = this.createProjectForm.value.basicCsvName;
        let columns = this.createProjectForm.value.basicCoulumspath;
        if(csvName && columns) {
            this.isBasicDOwnloadCSVBtn = false;
        }
    }

    toggleAllSelection() {
        let list;
        let columns;
        list = this.basicColumnsList;
        columns = this.createProjectForm.controls.basicCoulumspath;
        if (this.allSelected.selected) {
            columns.patchValue([...list.map(item => item), 0]);
        } else {
            columns.patchValue([]);
        }
    }

    sendCSVToEmail() {
        let basicColumns = [];
        if(!this.createProjectForm.value.basicCoulumspath) {
            this.createProjectForm.value.fhirPaths.forEach(element => {
                basicColumns.push(element.basicCoulumspath);
            });
        } else {
            basicColumns = this.createProjectForm.value.basicCoulumspath;
        }
        let body = {
            email: this.createProjectForm.value.csvEmailAddress,
            csvEmailSubject: this.createProjectForm.value.csvEmailSubject,
            csvEmailBody: this.createProjectForm.value.csvEmailBody,
            patientJSON: JSON.stringify(this.jsonData),
            fileName: this.createProjectForm.value.basicCsvName,
            columns: basicColumns,
        }
        this._dashboardCommonService.sendCSVToEmail(body).subscribe(
            (res) => {
                this.snackBar.open(res.message, '', {
                    ...this.snackBarConf,
                    duration: 5000,
                    panelClass: ['green-color-snackbar'],
                }).afterDismissed().subscribe(() => {

                });
            },
            (error) => {
                this.snackBar.open(error.error.message, '', {
                    ...this.snackBarConf,
                    duration: 5000,
                    panelClass: ['green-color-snackbar'],
                }).afterDismissed().subscribe(() => {

                });
            }
        );
    }

    uploadCSV() {
        //TODO: Refactor
        let columnName = this.assigningColumns();
        let body = {
            sftpHost: this.createProjectForm.value.csvSftpServer,
            sftpPort: this.createProjectForm.value.csvSftpPortNumber,
            sftpDirectory: this.createProjectForm.value.csvSftpDirectory,
            csvSftpUser: this.createProjectForm.value.csvSftpUser,
            csvSftpPassword: this.createProjectForm.value.csvSftpPassword,
            patientJSON: JSON.stringify(this.jsonData),
            fileName: this.createProjectForm.value.basicCsvName,
            columns: columnName,
            sftpKeyFile: this.csvKeyBase64textString,
        }
        this._dashboardCommonService.UploadCSV(body).subscribe(
            (res) => {
                this.snackBar.open(res.message, '', {
                    ...this.snackBarConf,
                    duration: 5000,
                    panelClass: ['green-color-snackbar'],
                }).afterDismissed().subscribe(() => {

                });
            },
            (error) => {
                this.snackBar.open(error.message, '', {
                    ...this.snackBarConf,
                    duration: 5000,
                    panelClass: ['red-color-snackbar'],
                }).afterDismissed().subscribe(() => {

                });
            }
        );
    }

    buildCSVModel() {
        if(!this.csvBlock) {
            return null;
        }
        let csvModel: CSVModel = {
            fileName: this.createProjectForm.get("basicCsvName").value,
            //TODO: I'm not confident this is correct.  I think this is the set list.  maybe we should have both
            // the list and the checked ones?
            columns: this.basicColumnsList,
            selectedColumns: this.assigningColumns(),
            body: JSON.stringify(this.jsonData),
            emailExport: this.buildEmailModel("csv"),
            SFTPExport: this.buildSFTPModel("csv")
        }
        return csvModel;
    }

    buildEmailModel(prefix) {
        if(this.createProjectForm.get(this.formatCamelCase(prefix, "emailAddress")).value == null) {
            return null;
        }
        let emailModel: EmailModel = {
            to: this.createProjectForm.get(this.formatCamelCase(prefix, "emailAddress")).value,
            subject: this.createProjectForm.get(this.formatCamelCase(prefix, "emailSubject")).value,
            body: this.createProjectForm.get(this.formatCamelCase(prefix, "emailBody")).value
        }
        return emailModel;
    }

    buildSFTPModel(prefix) {
        if(this.createProjectForm.get(this.formatCamelCase(prefix, "sftpServer")).value == null) {
            return null;
        }
        let sftpModel: SFTPModel = {
            serverAddress: this.createProjectForm.get(this.formatCamelCase(prefix, "sftpServer")).value,
            serverPort: this.createProjectForm.get(this.formatCamelCase(prefix, "sftpPortNumber")).value,
            targetDirectory: this.createProjectForm.get(this.formatCamelCase(prefix, "sftpDirectory")).value,
            username: this.createProjectForm.get(this.formatCamelCase(prefix, "sftpUser")).value,
            password: this.createProjectForm.get(this.formatCamelCase(prefix, "sftpPassword")).value,
            privateKeyBase64: this.csvKeyBase64textString,
        }
        return sftpModel;
    }

    onSelectFile(evt, source) {
        var files = evt.target.files;
        this.csvPrivateKeyFile = files[0];
        this.checkCSVUploadField();
        if (files && this.csvPrivateKeyFile ) {
            var reader = new FileReader();

            reader.onload = this._handleReaderLoaded.bind(this);
            reader.readAsBinaryString(this.csvPrivateKeyFile );
        }

    }

    disableCSVBlock() {
        this.csvBlock = false;

        //TODO: Remove Form Validation

        const index = this.exportCheckboxValue.indexOf("csv");
        if (index > -1) {
            this.exportCheckboxValue.splice(index, 1);
        }
    }

    buildExportModel() {
        let exportModel: ExportModel = {
            CSVExport: this.buildCSVModel(),
            FHIRExport: this.buildFHIRExportModel()
        }
        return exportModel;
    }

    createAuthorizationFormControls(prefix) {
        for(let fc of this.authFormControls) {
            this.createProjectForm.addControl(this.formatCamelCase(prefix, fc),
                new FormControl('',[]));
        }
    }

    removeAuthorizationFormControls(prefix) {
        for(let fc of this.authFormControls) {
            this.createProjectForm.removeControl(this.formatCamelCase(prefix,fc));
        }
    }

    validateAuthorizationFormControls(prefix) {
        this.updateAuthorizationFormControls(prefix);
        let valid = true;
        for(let fc of this.authFormControls) {
            if (!this.createProjectForm.get(this.formatCamelCase(prefix, fc)).valid) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    updateAuthorizationFormControls(prefix) {
        for(let fc of this.authFormControls) {
           this.createProjectForm.get(this.formatCamelCase(prefix, fc)).updateValueAndValidity();
        }
    }

    changeFHIRQueryAuth(event, index) {
        console.log(this.authentications);
        this.requests[index].authentication = this.getAuthModelByName(event.value);
        console.log(this.authentications);
    }

    changeFHIRExportGrantType(event) {
        this.changeGrantType(event, "FHIRExport");
    }

    getAuthModelByName(name) {
        for(let auth of this.authentications) {
            if(auth.name == name) {
                return auth;
            }
        }
        return null;
    }

    FHIRExportSubmit() {
        //TODO: Validate Form

        //Validate that data query has output to export
        if(!(this.jsonData) || this.jsonData.length == 0 ||
            ((this.dataRetrievalStatus.slice(0, 3) != '200') && (this.dataRetrievalStatus.slice(0, 3) != '412'))){
            //If there isn't data to submit (or the status wasn't 200 or 412)
            if(this.jsonData && this.jsonData.length > 0) { //Status wasn't acceptable
                this.FHIRExportRetrievalStatus = "Query result gave non-OKAY response - this probably won't work."
            } else {
                this.FHIRExportRetrievalStatus = "There's no data to submit!  Did you run a query?";
            }
            this.cd.detectChanges();
            return;
        }
        //Construct Object
        let exportObj = this.buildFHIRExportModel();
        //Submit to backend
        this.sendDataForFHIRExport(exportObj);
    }

    buildFHIRExportModel() {
        if(!this.fhtrBlock) {
            return null;
        }
        let importRequest: RequestModel = {
            order: 1,
            returnValue: null,
            connection: this.createProjectForm.get("FHIRExportAddress").value,
            query: null,
            type: "full",
            authentication: this.getAuthModelByName(this.createProjectForm.get("FHIRExportAuthentication").value)
        };
        this.createProjectForm.get("FHIRExportOpType").updateValueAndValidity();
        let importModel: FHIRExportModel = {
            request: importRequest,
            type: this.createProjectForm.get("FHIRExportOpType").value,
            body: JSON.stringify(this.jsonData)
        }
        return importModel;
    }

    sendDataForFHIRExport(importModel) {
        this._dashboardCommonService.sendDataForFHIRExport(importModel).subscribe((res: any) => {
            this.FHIRExportRetrievalStatus = `${res.statusCode} ${StatusCodes[res.statusCode]}, Retrieved ${new Date().toLocaleTimeString()}`
            this.FHIRExportRetrievalStatus += "\n " + res.message;
            this.cd.detectChanges();
            });
        this.cd.detectChanges();
    }

    checkIsEdit() {
        this.singleProjectData = this._dashboardCommonService.singleProjectData;
        this.createProjectPartnerId = this._projectService.selectedParterForProject;
        this._dashboardCommonService.removePartenerProject();
        if (this.singleProjectData) {
            this.updateFormDetails();
        } else if (this.createProjectPartnerId) {
            this.createProjectForm.patchValue({
                partnerId: this.createProjectPartnerId,
            });
            this._projectService.removeSelectedProject();
        }
    }

    updateFormDetails() {
        this.isEdit = true;
        this.patchFormValues(this.singleProjectData);
    }

    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }

    patchFormValues(projectData) {
        let FHIRExport: any = [];
        let csvValues: any = [];
        this.projectId = projectData.id;
        projectData.projectOutputList.forEach(element => {
            this.exportCheckboxValue.push(element.outputName);
           let outputArray ={
                id : element.id,
                name: element.outputName
            }
            this.outputCheckBoxValues.push(outputArray)
            if(element.outputName == 'FHIRExport') {
                this.fhtrBlock = true;
                FHIRExport.push(JSON.parse(element.outputSettings));
                FHIRExport.forEach(element => {
                    this.FHIRExportMethodscheckbox.push(element['FHIRExport'][0]);
                    this.FHIRExportMethodscheckbox.push(element['FHIRExport'][1]);
                    this.FHIRExportValues.push(element['FHIRExport'][0])
                    this.FHIRExportValues.push(element['FHIRExport'][1])
                });

            } else if (element.outputName == 'csv') {
                this.csvBlock = true;
                csvValues.push(JSON.parse(element.outputSettings));
                csvValues.forEach(element => {
                    this.CSVSendMethodsCheckbox.push(element['sendMethod'][0]);
                    this.CSVSendMethodsCheckbox.push(element['sendMethod'][1])
                    this.sendMethodValues.push(element['sendMethod'][0])
                    this.sendMethodValues.push(element['sendMethod'][1])
                });
                this.CSVSendMethodsCheckbox.forEach(element => {
                    if(element == 'email') {
                        this.csvEmailBlock = true;
                    } else if (element == 'sftp') {
                        this.csvSftpBlock = true;
                    }
                });

            }
        });
        let csvName;
        let csvEmailAddress;
        let csvEmailSubject;
        let csvEmailBody;
        let csvSftpServer;
        let csvSftpPortNumber;
        let csvSftpDirectory;
        let csvSftpUser;
        let csvSftpPassword;
        csvValues.forEach(element => {
            csvName = element.csvname;
            csvEmailAddress = element.emailSettings.email;
            csvEmailSubject = element.emailSettings.csvEmailSubject;
            csvEmailBody = element.emailSettings.csvEmailBody;
            csvSftpServer = element.sftpSettings.csvSftpServer;
            csvSftpPortNumber = element.sftpSettings.csvSftpPortNumber;
            csvSftpDirectory = element.sftpSettings.csvSftpDirectory;
            csvSftpUser = element.sftpSettings.user;
            csvSftpPassword = element.sftpSettings.password;
        });
        this.createProjectForm.patchValue({
            projectName: projectData.projectName,
            partnerId: projectData.isPartnerDeleted ? '' : projectData.partnerId,
            contactNumber: projectData.contactNumber,
            email: projectData.email,
            description: projectData.description,
            uri: projectData.uri, //TODO: Will have to update for array of connections
            port: projectData.port,
            authentication: projectData.authentication,
            grantType: projectData.grantType,
            authorizationEndpoint: projectData.authorizationEndpoint,
            tokenEndpoint: projectData.tokenEndpoint,
            clientId: projectData.clientId,
            clientSecret: projectData.clientSecret,
            redirectUrls: projectData.redirectUrls,
            status: projectData.status,
            queryTypeQuery: projectData.queryTypeQuery,
            returnValue: projectData.returnValue,
            order: projectData.order,
            queryTypeConnection: projectData.queryTypeConnection,
            csvName: csvName,
            csvEmailAddress: csvEmailAddress,
            csvEmailSubject: csvEmailSubject,
            csvEmailBody: csvEmailBody,
            csvSftpServer: csvSftpServer,
            csvSftpPortNumber: csvSftpPortNumber,
            csvSftpDirectory: csvSftpDirectory,
            csvSftpUser: csvSftpUser,
            csvSftpPassword: csvSftpPassword,
            basicResponsePort: projectData.basicResponsePort,
            basicResponseUri: projectData.basicResponseUri,

        });

        //Process query JSON object
        if(JSON.parse(projectData.query) != null) {
            this.processPatchQueries(JSON.parse(projectData.query).queries);
        }

        //Process authentications JSON object
        if(JSON.parse(projectData.authentications) != null) {
            this.processPatchAuthentications(JSON.parse(projectData.authentications));
        }

        //Process output JSON object
        if(JSON.parse(projectData.outputJson) != null) {
            this.processPatchOutput(JSON.parse(projectData.outputJson));
        }

        //this.changeAuthentication(projectData.authentication);
    }

    processPatchQueries(incomingQueries) {
        //Set the first query (addQuery expects there to be at least one already)
        this.requests[0] = incomingQueries[0];
        this.createProjectForm.get("FHIRQueryAuthentication1")
            .setValue(this.requests[0].authentication && this.requests[0].authentication.name);
        let index = 0;
        for(let request of incomingQueries.slice(1)) {
            this.addQuery(request,index);
            index++;
        }
        this.cd.detectChanges();
    }

    processPatchAuthentications(incomingAuths) {
        this.authentications = []; //Clear the current array
        if(incomingAuths.length == 0) {
            this.addAuth(null, 0); //This should be impossible, but let's catch it anyway
        } else {
            this.authentications[0] = incomingAuths[0];
        }
        let index = 1;
        for(let auth of incomingAuths.slice(1)) {
            this.addAuth(auth, index+1);
        }
        if(incomingAuths.length < 2) {
            this.addAuth(null, 1); //This should be impossible, but let's catch it anyway
        }
        this.cd.detectChanges();
    }

    processPatchOutput(incomingOutput: ExportModel) {
        console.log(incomingOutput);
        if(incomingOutput.FHIRExport) {
            this.enableFHIRBlock();
            //Process data to form
            this.createProjectForm.get("FHIRExportAddress").setValue(incomingOutput.FHIRExport.request.connection);
            this.createProjectForm.get("FHIRExportAuthentication")
                .setValue(incomingOutput.FHIRExport.request.authentication.name);
            this.createProjectForm.get("FHIRExportOpType").setValue(incomingOutput.FHIRExport.type);
        }
        if(incomingOutput.CSVExport) {
            this.enableCSVBlock();
            //Process data to form
            this.createProjectForm.get("basicCsvName").setValue(incomingOutput.CSVExport.fileName);
            if(incomingOutput.CSVExport.columns.length > 0) {
                console.log("hello.  I'm patching stuff in.  Probably.");
                console.log(incomingOutput.CSVExport.columns);
                this.basicColumnsList = incomingOutput.CSVExport.columns;

                //TODO: This doesn't seem to be working.
                let list = incomingOutput.CSVExport.selectedColumns
                let columns = this.createProjectForm.controls.basicCoulumspath;
                columns.patchValue([...list.map(item => item), 0]);
                this.checkColumnBlock = true;
            }
            if(incomingOutput.CSVExport.emailExport) {
                this.onCSVMethodSelection(this.sendMethods[0], {checked: true});
                this.createProjectForm.get("csvEmailAddress").setValue(incomingOutput.CSVExport.emailExport.to);
                this.createProjectForm.get("csvEmailSubject").setValue(incomingOutput.CSVExport.emailExport.subject);
                this.createProjectForm.get("csvEmailBody").setValue(incomingOutput.CSVExport.emailExport.body);
            }
            if(incomingOutput.CSVExport.SFTPExport) {
                this.onCSVMethodSelection(this.sendMethods[1], {checked: true});
                this.createProjectForm.get("csvSftpServer").setValue(incomingOutput.CSVExport.SFTPExport.serverAddress);
                this.createProjectForm.get("csvSftpPortNumber").setValue(incomingOutput.CSVExport.SFTPExport.serverPort);
                this.createProjectForm.get("csvSftpDirectory").setValue(incomingOutput.CSVExport.SFTPExport.targetDirectory);
                this.createProjectForm.get("csvSftpUser").setValue(incomingOutput.CSVExport.SFTPExport.username);
                this.createProjectForm.get("csvSftpPassword").setValue(incomingOutput.CSVExport.SFTPExport.password);
                //TODO: Private Key File?
            }
        }
    }

    getPartnerNames() {
        // Get the data
        this._dashboardCommonService.getAllPartnerNamesData().subscribe((res: any[]) => {
            this.partnerNames = res;
            this.checkIsEdit();
        });
    }

    /**
     * Activates (or deactivates) the validators for Auth type.  Specific auth-type validators are enabled in
     * this.changeGrantType()
     * @param event The triggering event (a selection change, with .value of the new selection)
     * @param prefix Because this logic repeats, prefix might prefix the form control for a different auth locations.
     *          prefix expects just the prefix, for example: "FHIRImportGrantType" has prefix "FHIRImport"
     */
    changeAuthentication(event, prefix = "") {
        if (event.value == 'OAuth' || event == 'OAuth') {
            this.createProjectForm.get(this.formatCamelCase(prefix, 'grantType')).setValidators([Validators.required]);
            this.createProjectForm.get(this.formatCamelCase(prefix, 'tokenEndpoint')).setValidators([Validators.required]);
            this.createProjectForm.get(this.formatCamelCase(prefix, 'clientId')).setValidators([Validators.required]);
            this.createProjectForm.get(this.formatCamelCase(prefix, 'clientSecret')).setValidators([Validators.required]);
        } else {
            this.createProjectForm.get(this.formatCamelCase(prefix, 'grantType')).clearValidators();
            this.createProjectForm.get(this.formatCamelCase(prefix, 'authorizationEndpoint')).clearValidators();
            this.createProjectForm.get(this.formatCamelCase(prefix, 'tokenEndpoint')).clearValidators();
            this.createProjectForm.get(this.formatCamelCase(prefix, 'clientId')).clearValidators();
            this.createProjectForm.get(this.formatCamelCase(prefix, 'clientSecret')).clearValidators();
            this.createProjectForm.get(this.formatCamelCase(prefix, 'redirectUrls')).clearValidators();
        }
        this.createProjectForm.controls[this.formatCamelCase(prefix, 'grantType')].updateValueAndValidity();
        this.createProjectForm.controls[this.formatCamelCase(prefix, 'authorizationEndpoint')].updateValueAndValidity();
        this.createProjectForm.controls[this.formatCamelCase(prefix, 'tokenEndpoint')].updateValueAndValidity();
        this.createProjectForm.controls[this.formatCamelCase(prefix, 'clientId')].updateValueAndValidity();
        this.createProjectForm.controls[this.formatCamelCase(prefix, 'clientSecret')].updateValueAndValidity();
        this.createProjectForm.controls[this.formatCamelCase(prefix, 'redirectUrls')].updateValueAndValidity();
    }

    //This toggles the validators for the fields based on the grant type
    changeGrantType(event, prefix="") {
        if(event.value == "authorization_code") {
            this.createProjectForm.get(this.formatCamelCase(prefix, 'authorizationEndpoint')).setValidators([Validators.required]);
            this.createProjectForm.get(this.formatCamelCase(prefix, 'redirectUrls')).setValidators([Validators.required]);
        } else if(event.value == "client_credentials") {
            this.createProjectForm.get(this.formatCamelCase(prefix, 'authorizationEndpoint')).clearValidators();
            this.createProjectForm.get(this.formatCamelCase(prefix, 'redirectUrls')).clearValidators();
        } else if(event.value == "client_jwt") {
            this.createProjectForm.get(this.formatCamelCase(prefix, 'authorizationEndpoint')).clearValidators();
            this.createProjectForm.get(this.formatCamelCase(prefix, 'redirectUrls')).clearValidators();
        }
        this.createProjectForm.controls[this.formatCamelCase(prefix, 'redirectUrls')].updateValueAndValidity();
        this.createProjectForm.controls[this.formatCamelCase(prefix, 'authorizationEndpoint')].updateValueAndValidity();
    }

    //We use these properties for display, but they aren't actually part of the object, and we dont want to store them
    stripAuthResults() {
        for(let i = 0; i < this.authentications.length; i++) {
            // @ts-ignore
            this.authentications[i].oAuthToken = null;
            // @ts-ignore
            this.authentications[i].tokenRetrievalStatus = null;
        }
    }

    //We don't want to upload the body with the rest of the data for saving
    strippedExportModel() {
        let exportModel = this.buildExportModel();
        // @ts-ignore
        exportModel.CSVExport?.body = null;
        // @ts-ignore
        exportModel.FHIRExport?.body = null;
        return exportModel;
    }

    submitData() {
        if (this.createProjectForm.valid) {
            this.createProjectForm.value.query = JSON.stringify( { 'queries' : this.requests });
            this.stripAuthResults();
            this.createProjectForm.value.authentications = JSON.stringify(this.authentications);
            this.createProjectForm.value.outputJson = JSON.stringify(this.strippedExportModel());
            console.log(this.createProjectForm);
            if (this.isEdit) {
                this.projectOutputList();
                this._dashboardCommonService.updateProject(this.createProjectForm.value, this.singleProjectData.id).subscribe(
                    () => {
                        this.snackBar.open('Project Updated Successfully!', '', {
                            ...this.snackBarConf,
                            duration: 3000,
                            panelClass: ['green-color-snackbar'],
                        }).afterDismissed().subscribe(() => {
                            this.router.navigate(['dashboards/view-profiles']);
                        });
                    },
                    (response) => {
                        this.snackBar.open('Project Updating Failure!', '', {
                            ...this.snackBarConf,
                            duration: 3000,
                            panelClass: ['red-color-snackbar'],
                        }).afterDismissed().subscribe(() => {
                            this.router.navigate(['dashboards/view-profiles']);
                        });
                    }
                );
            } else {
                this.projectOutputList();
                this._dashboardCommonService.createProject(this.createProjectForm.value).subscribe(
                    () => {
                        this.snackBar.open('Project Created Successfully!', '', {
                            ...this.snackBarConf,
                            duration: 3000,
                            panelClass: ['green-color-snackbar'],
                        }).afterDismissed().subscribe(() => {
                            this.clearForm(this.createProjectForm);
                            this.router.navigate(['dashboards/view-projects']);
                        });
                    },
                    (response) => {
                        this.snackBar.open('Project Creating Failure!', '', {
                            ...this.snackBarConf,
                            duration: 3000,
                            panelClass: ['red-color-snackbar'],
                        });
                    }
                );
            }
        }
    }

    projectOutputList () {
        let projectOutputListData: any = [];
        this.outputCheckBoxValues.forEach((element) => {
            let data: any;
            if (
                element.name == 'FHIRImport' ||
                element.name == 'csv' ||
                element.name == 'json' ||
                element.name == 'script'
            ) {
                data = {
                    projectId: this.projectId,
                    outputName: element.name,
                    isDeleted: false,
                    id: element.id,
                };
            }
            let outputSettings: any;
            if (element.name == 'csv') {
                outputSettings = {
                    csvname: this.createProjectForm.value.csvName,
                    queryTypeConnection:
                        this.createProjectForm.value.queryTypeConnection,
                    columns: this.createProjectForm.value.coulumspath,
                    sendMethod: this.sendMethodValues,
                    emailSettings: {
                        email: this.createProjectForm.value.csvEmailAddress,
                        csvEmailBody: this.createProjectForm.value.csvEmailBody,
                        csvEmailSubject: this.createProjectForm.value.csvEmailSubject,
                    },
                    sftpSettings: {
                        csvSftpServer: this.createProjectForm.value.csvSftpServer,
                        csvSftpPortNumber: this.createProjectForm.value.csvSftpPortNumber,
                        csvSftpDirectory: this.createProjectForm.value.csvSftpDirectory,
                        user: this.createProjectForm.value.csvSftpUser,
                        password: this.createProjectForm.value.csvSftpPassword,
                    },
                };
            } else if (element.name == 'FHIRImport') {
                outputSettings = {
                    queryTypeConnection:
                        this.createProjectForm.value.queryTypeConnection,
                    fhirImport: this.FHIRExportValues,
                };
            } else {
                outputSettings = {};
            }

            data.outputSettings = JSON.stringify(outputSettings);
            projectOutputListData.push(data);
        });
        this.createProjectForm.value.projectOutputList = projectOutputListData;
        delete this.createProjectForm.value['csvName'];
        delete this.createProjectForm.value['csvEmailAddress'];
        delete this.createProjectForm.value['csvEmailBody'];
        delete this.createProjectForm.value['csvEmailSubject'];
        delete this.createProjectForm.value['csvSftpServer'];
        delete this.createProjectForm.value['csvSftpPortNumber'];
        delete this.createProjectForm.value['csvSftpDirectory'];
        delete this.createProjectForm.value['csvSftpUser'];
        delete this.createProjectForm.value['csvSftpPassword'];
        let requestBody: any = this.createProjectForm.value;
        requestBody.outputList = projectOutputListData;
    }

    clearForm(form: FormGroup): void {
        form.reset();
        Object.keys(form.controls).forEach(key => {
            form.controls[key].setErrors(null)
        });
    }


    numberOnly(e) {
        let inp = String.fromCharCode(e.keyCode);
        if (/[0-9]/.test(inp)) {
            return true;
        } else {
            e.preventDefault();
            return false;
        }
    }

    retrieveToken(index) {
        if(this.authentications[index].grantType == 'authorization_code') {
            this.openAuthWindow();
        } else {
            this.getToken(index);
        }
    }

    openAuthWindow() {
        const clientId = this.createProjectForm.get('clientId').value;
        const redirect_uri = this.createProjectForm.get('redirectUrls').value;
        const scope = "launch user/*.read";
        const uri = this.createProjectForm.get('authorizationEndpoint').value;
        let redirectUri: string = encodeURI(uri);
        redirectUri = redirectUri + `?client_id=${clientId}&response_type=code&scope=${scope}&redirect_uri=${redirect_uri}`;
        this.createOauthWindow(redirectUri);
    }

    createOauthWindow(url: string) {
        const name = 'Authorization', width = 500, height = 600, left = 50, top = 100;
        if (url == null) {
            return null;
        }
        this.windowHandle = window.open(url, name, 'popup');
        this.tokenRetrievalStatus = null;
        this.oAuthToken = null;
        this.cd.detectChanges();
        this.codeCheck();
    }

    codeCheck() {
        this.loopCount++;
        setTimeout(() => {
            if (window.localStorage.getItem('code')) {
                this.authorizationCode = window.localStorage.getItem('code');
                window.localStorage.removeItem('code');
                this.windowHandle.close();
                this.getToken();
                this.loopCount = 0;
            } else if (this.loopCount < 11) {
                this.codeCheck();
            } else {
                this.tokenRetrievalStatus = "Request Timed Out.";
                this.windowHandle.close();
                this.loopCount = 0;
                this.cd.detectChanges();
            }
        }, 3000);
    }

    receiveMessage(evt: Event) {
        this.authorizationCode = this.extractCode('code', this.windowHandle.location.href)
        this.windowHandle.removeAllListeners();
        this.windowHandle.close();
        this.getToken();
    }

    extractCode(field: any, url: string): string {
        const windowLocationUrl = url;
        const reg = new RegExp('[?&]' + field + '=([^&#]*)', 'i');
        const string = reg.exec(windowLocationUrl);
        return string ? string[1] : null;
    }

    getToken(index="") {
        this._dashboardCommonService.fetchToken(this.buildAuthorizationModel(index)).subscribe((res: any) => {
        this.authentications[index].tokenRetrievalStatus = "Retrieved " + (new Date().toLocaleTimeString());
        this.authentications[index].oAuthToken = res;
        this.cd.detectChanges();
        });
    }

    buildAuthorizationModel(prefix = "") {
        let authModel: AuthModel = {
            name: this.createProjectForm.get(this.formatCamelCase(prefix, 'authName')).value,
            authenticationType: this.createProjectForm.get(this.formatCamelCase(prefix, 'authType')).value,
            grantType: this.createProjectForm.get(this.formatCamelCase(prefix,'grantType')).value,
            authorizationEndpoint: this.createProjectForm.get(this.formatCamelCase(prefix,'authorizationEndpoint')).value,
            tokenEndpoint: this.createProjectForm.get(this.formatCamelCase(prefix,'tokenEndpoint')).value,
            clientId: this.createProjectForm.get(this.formatCamelCase(prefix,'clientId')).value,
            clientSecret: this.createProjectForm.get(this.formatCamelCase(prefix,'clientSecret')).value,
            clientCode: this.authorizationCode,
            clientScope: "launch user/*.read",
            redirectUrls: this.createProjectForm.get(this.formatCamelCase(prefix,'redirectUrls')).value
        }
        //TODO: Fill this in later - maybe?  Do we really care about the Auth Code flow in export?
        if(prefix == "FHIRImport") {
            authModel.clientCode = null;
        }
        return authModel;
    }

    formatCamelCase(prefix, input) {
        if (prefix == "") {
            return input;
        }
        input = input[0].toUpperCase() + input.slice(1);
        return prefix + input;
    }

    //Matches on forbidden input and has the message as a value to be used for informing the user.
    forbiddenInputValidator(inputRe: RegExp, message): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const forbidden = inputRe.test(control.value);
            return forbidden ? {forbiddenInput: {value: control.value, message: message}}: null;
        }
    }

    //Validates that the query field has the input from the returnValue field in the form of <<token>>
    queryContainsReturnValidator(index) : ValidatorFn {
        let thisQueryIndex = index;
        return (control: AbstractControl): ValidationErrors | null => {
            const contains = this.requests[thisQueryIndex].returnValue == null ||
                this.requests[thisQueryIndex].query == null ||
                this.requests[thisQueryIndex].query.includes(`<<${this.requests[thisQueryIndex].returnValue}>>`);
            return contains ? null : {doesNotContain: {value: this.requests[thisQueryIndex].query,
                message: `Your query must contain your return value (<<${this.requests[thisQueryIndex].returnValue}>>).` }}
        }

    }

    //Checks angular validators for Connection and Query
    isRequestInvalid() {
        for(let i = 1; i < this.requests.length+1; i++) {
            this.createProjectForm.get('query'+i).updateValueAndValidity();
            if(this.checkForError('required', i)) {
                this.dataRetrievalStatus = "All fields are required.";
                this.cd.detectChanges();
                return true;
            } else if(this.checkForError('forbiddenInput', i)) {
                let thisError = (this.createProjectForm.get('connection'+i).errors?.['forbiddenInput']) ?
                    this.createProjectForm.get('connection'+i).errors :
                    (this.createProjectForm.get('query'+i).errors?.['forbiddenInput']) ?
                        this.createProjectForm.get('query'+i).errors : this.createProjectForm.get('returnValue'+i).errors;
                console.log(thisError);
                this.dataRetrievalStatus = thisError['forbiddenInput'].message;
                this.cd.detectChanges();
                return true;
            } else if((i > 1) && this.createProjectForm.get('query'+i).errors?.['doesNotContain']) {
                this.dataRetrievalStatus = this.createProjectForm.get('query'+i).errors['doesNotContain'].message;
                this.cd.detectChanges();
                return true;
            }
        }
        return false;
    }

    //Checks for an errorKey in a query, connection, and returnValue field, returning true if one was found.
    checkForError(errorKey, index) {
        return this.createProjectForm.get('query'+index).errors?.[errorKey] ||
            this.createProjectForm.get('connection'+index).errors?.[errorKey] ||
            ((index > 1) ? this.createProjectForm.get('returnValue'+index).errors?.[errorKey] : false);
    }

    getAPIResult() {
        if(this.isRequestInvalid()) { return; } //Quit out if invalid
        this.jsonData = null;
        this.dataRetrievalStatus = null;
        this.cd.detectChanges();
        console.log(this.buildPostBody());
        this._dashboardCommonService.getAPIResults(this.buildPostBody()).subscribe((res: any) => {
            console.log(res);
            this.dataRetrievalStatus = `${res.statusCode} ${StatusCodes[res.statusCode]}, Retrieved ${new Date().toLocaleTimeString()}`
            try {
                this.jsonData = JSON.parse(res.data);
                this.getPathFromJson(this.jsonData);
                if(res.errMessage) {
                    this.dataRetrievalStatus += `; WARNING: ${res.errMessage}`;
                }
            } catch (e) {
                this.dataRetrievalStatus += `; WARNING: Could not parse JSON response.`;
                this.jsonData = res.data;
            }
            this.cd.detectChanges();
        }, error => {
            console.log("Caught: ", error);
            this.dataRetrievalStatus = `${error.status} ${StatusCodes[error.status]}, Retrieved ${new Date().toLocaleTimeString()} <br/>`
            + `${error.message}`
            this.cd.detectChanges();
        });
    }

    /**
     * Adds the next query to this.requests or removes all after this one.
     * I know there's a lot of confusing use of 'index', so I've added some (probably redundant) variables to help
     * clarify what's actually being passed in.
     * @param event The switch event, which holds the value of the choice ('full' or 'nested')
     * @param index The index of the triggering request (0 for the first one, 1 for the second, etc.)
     */
    changeNestedQueryType(event, index, query=null) {
        let nextIndex = index+1;
        let nextOrder = index+2;
        if(event.value == 'full') {
            this.removeValidators(nextOrder);
            this.requests.length = nextIndex;
        } else if(event.value == 'nested') {
            //this.requests[index].type = "nested"; Unnecessary due to Angular binding
            this.addQuery(query, index);
        }
        console.log(this.createProjectForm.controls);
        console.log(this.requests);
        this.cd.detectChanges();
    }

    /**
     * Adds the passed in query to this.requests (or creates one, if none provided)
     * I know there's a lot of confusing use of 'index', so I've added some (probably redundant) variables to help
     * clarify what's actually being passed in.
     * @param query The query to add (or null)
     * @param index the index of the last element in this.requests, before the push (the previous query)
     * @private
     */
    private addQuery(query, index) {
        let myIndex = index;
        let nextIndex = index+1;
        let nextOrder = index+2;
        this.addValidators(nextOrder);
        if (query == null) {
            let nextRequest: RequestModel = {
                order: nextOrder,
                returnValue: null,
                connection: this.requests[myIndex].connection,
                query: null,
                type: "full",
                authentication: this.requests[myIndex].authentication
            };
            this.requests.push(nextRequest);
        } else {
            this.requests.push(query);
        }
        this.createProjectForm.get("FHIRQueryAuthentication" + nextOrder)
            .setValue(this.requests[nextIndex].authentication && this.requests[nextIndex].authentication.name,
                {
                    onlySelf: true,
                    emitEvent: false,
                    emitModelToViewChange: false,
                });
        this.activateQueryValidator(nextIndex);
        /**
         * This one is added after because it breaks if there isn't a query in this.requests for this index already.
         * So why aren't the others added after?
         *  -> Because if they are, angular will break, expecting at least one control to be there, which addValidators
         *     adds.
         */

    }

    addAuth(auth, index) {
        this.createAuthorizationFormControls(index);
        console.log("Form controls created for index: " + index);
        if(auth == null) {
            let nextAuth: AuthModel = {
                authenticationType: "NoAuth",
                authorizationEndpoint: null,
                clientCode: null,
                clientId: null,
                clientScope: null,
                clientSecret: null,
                grantType: null,
                name: null,
                redirectUrls: null,
                tokenEndpoint: null
            }
            this.authentications.push(nextAuth);
        } else {
            this.authentications.push(auth);
        }
        //Done?
    }

    removeAuth(index) {
        this.removeAuthorizationFormControls(index);
        this.authentications.splice(index, 1);
    }

    //Takes in the order and adds controls & validators to the appropriate field controls (returnValue, connection, query)
    addValidators(order) {
        this.createProjectForm.addControl('returnValue' + order, new FormControl('',[Validators.required,
            this.forbiddenInputValidator(/^[^.]*$/, "Return Value must contain at least one period (.)"),
            this.forbiddenInputValidator(/^Bundle.*/, "Return Value should not start with Bundle.  Start with the resource (i.e. Patient.id)")
        ]));
        this.createProjectForm.addControl('connection' + order, new FormControl('', [Validators.required,
            this.forbiddenInputValidator(/.*(?<!\/)$/, "Connection must end with a slash (/)")]));
        this.createProjectForm.addControl('query' + order, new FormControl('', [Validators.required,
            this.forbiddenInputValidator(/^\/.*/, "Query must not start with a slash (/)")]));
        this.createProjectForm.addControl('FHIRQueryAuthentication' + order, new FormControl(''));
    }

    //Takes in the index and adds an additional query validator.  Seperate because this one should be one *after*
    //The query is added to this.requests - the others should be done before.
    activateQueryValidator(index) {
        this.createProjectForm.get('query'+(index+1)).addValidators(this.queryContainsReturnValidator(index));
        this.createProjectForm.get('query'+(index+1)).updateValueAndValidity();
    }

    //Removes all validators of given order and above (by requests length)
    removeValidators(order) {
        for(let i = order;i < this.requests.length+1;i++) {
            this.createProjectForm.removeControl("returnValue"+i);
            this.createProjectForm.removeControl("connection"+i);
            this.createProjectForm.removeControl("query"+i);
            this.createProjectForm.removeControl("FHIRQueryAuthentication"+i);
        }
    }

    //Standard trackBy pattern to sort by order number (called by ngFor).
    //This is probably unnecessary.
    trackByRequestOrder(index, request) {
        return request ? request.order : undefined;
    }

    //Builds the post body from this.requests to send to the backend.
    buildPostBody() {
        return JSON.parse(JSON.stringify( { 'queries' : this.requests }, null, 4));
        /**
         * We add the 'queries' JSON member instead of just passing up the array because the backend expects a QueriesDTO,
         * which is a Java object with a 'queries' member.  In the future, this might also include authentication and output.
         */
    }


    //We don't want our first query to trigger the repeating display element, so we should filter it out of the ngFor.
    filterOutFirstEntry(requests: RequestModel[]): RequestModel[] {
        return requests.filter(r => r.order > 1);
    }

    getTestQueryResult() {
        let url = this.createProjectForm.get('uri').value;
        let port = this.createProjectForm.get('port').value;
        let basicQuery = this.createProjectForm.get('basicQuery').value;
        let body = url + ':' + port + '/' + basicQuery
        this._dashboardCommonService.getTestQueryResult(body).subscribe((res: any) => {
            this.basicQueryjsonData = JSON.parse(res.data);
            let data = [this.basicQueryjsonData]
            data.forEach(element => {
                element.entry.forEach(data => {
                    this.patientIds.push(data.resource.id)
                });
            });
            this.cd.detectChanges();
        });
    }

    basicQueryTypeQueryResult() {
        let url = this.createProjectForm.get('uri').value;
        let port = this.createProjectForm.get('port').value;
        let basicQuery = this.createProjectForm.get('basicQuery').value;
        let basicQueryType = this.createProjectForm.get('basicQueryTypeQuery').value;
        let body = url + ':' + port + '/' + basicQuery + '?' + basicQueryType
        this._dashboardCommonService.getTestQueryResult(body).subscribe((res: any) => {
            this.basicQueryTypeJsonData = JSON.parse(res.data);
            this.basicQueryTypeStringyfyData = res.data;
            this.checkBsicResponseBtn();
            this.cd.detectChanges();
        });
    }

    basicSendPatientData() {
        this._dashboardCommonService.basicSendPatientData(this.basicQueryTypeStringyfyData,this.createProjectForm.value.basicResponseUri, this.createProjectForm.value.basicResponsePort ).subscribe((res: any) => {
            this.baiscPatientData = JSON.parse(res.data);
            this.snackBar.open('Response submited to client server', '', {
                ...this.snackBarConf,
                duration: 3000,
                panelClass: ['green-color-snackbar'],
            }).afterDismissed().subscribe(() => {

            });
            this.cd.detectChanges();
        });
    }

    checkBsicResponseBtn() {
        if(this.createProjectForm.value.basicResponseUri && this.createProjectForm.value.basicResponsePort && this.basicQueryTypeJsonData) {
            this.basicSubmitResponsebtn = false;
        } else {
            this.basicSubmitResponsebtn = true;
        }
    }

    downloadJsonFile() {
        const blob = new Blob([JSON.stringify(this.jsonData, undefined, 4)], {type: 'text/csv'});
        fileSaver.saveAs(blob, `${this.createProjectForm.value.exportJson}.txt`);
    }

    _handleReaderLoaded(readerEvt) {
        var binaryString = readerEvt.target.result;
        return this.csvKeyBase64textString = btoa(binaryString);
    }

    getPathFromJson(body) {
        this._dashboardCommonService.getPathFromJson(body).subscribe(
            (res) => {
                if(res) {
                    this.basicColumnsList = res;
                    this.checkColumnBlock = true;
                    this.checkFhirPathBlock = false;
                } else {
                    this.checkColumnBlock = false;
                    this.checkFhirPathBlock = true;
                }
                this.cd.detectChanges();
            },
            (error) => {
                this.snackBar.open(error.error.message, '', {
                    ...this.snackBarConf,
                    duration: 5000,
                    panelClass: ['red-color-snackbar'],
                }).afterDismissed().subscribe(() => {

                });
            }
        );
    }
}
