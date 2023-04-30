import { ChangeDetectionStrategy, Component, Inject, OnInit, ViewEncapsulation } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { partnerData } from 'app/modules/models/common.model';
import { DashboardCommonService } from '../../services/dashboard-common.service';
import { Router } from '@angular/router';
import { PartnerNameModel } from '../../models/partner.model';
import { PartnerProfileService } from '../../services/partner/partner-profile.service';
import config from 'app/modules/constants/common-constants';

interface DialogData {
    partnerData: partnerData;
    edit: boolean;
}
//@UntilDestroy()
@Component({
    selector: 'create-profile',
    templateUrl: './create-profile.component.html',
    styleUrls: ['./create-profile.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CreateProfileComponent implements OnInit {
    private snackBarConf: MatSnackBarConfig = {
        duration: 0,
        horizontalPosition: 'center',
        verticalPosition: 'top',
    };

    partnerProfileForm = new FormGroup(
        {
            partnerName: new FormControl('', [Validators.required, Validators.pattern(config.NAME_PATTERN)]),
            primaryContactName: new FormControl('', [Validators.required, Validators.pattern(config.NAME_PATTERN)]),
            email: new FormControl('', [Validators.required, Validators.email, Validators.pattern(config.EMAIL_PATTERN)]),
            mobileNumber: new FormControl('', Validators.required),
            description: new FormControl('', [Validators.maxLength(250)]),
        })

    constructor(
        private _dashboardCommonService: DashboardCommonService,
        private _partnerProfileService: PartnerProfileService,
        private snackBar: MatSnackBar,
        public dialogRef: MatDialogRef<CreateProfileComponent>,
        @Inject(MAT_DIALOG_DATA) public data: DialogData,
        private router: Router,
    ) {
    }


    ngOnInit(): void {

        if (this.data.edit) {
            this.partnerProfileForm.patchValue({
                partnerName: this.data.partnerData.partnerName,
                primaryContactName: this.data.partnerData.primaryContactName,
                email: this.data.partnerData.email,
                mobileNumber: this.data.partnerData.mobileNumber,
                description: this.data.partnerData.description,
            });
        }
    }

    submitData() {
        if (this.partnerProfileForm.valid) {
            if (this.data.edit) {
                this._dashboardCommonService.updatePartner(this.partnerProfileForm.value, this.data.partnerData.id).subscribe(
                    () => {
                        this.snackBar.open('Profile Updated Successfully!', '', {
                            ...this.snackBarConf,
                            duration: 3000,
                            panelClass: ['green-color-snackbar'],
                        }).afterDismissed().subscribe(() => {
                            this.onClose({});
                        });
                        this.dialogRef.close();
                    },
                    (response) => {
                        this.snackBar.open('Profile Updating Failure!', '', {
                            ...this.snackBarConf,
                            duration: 3000,
                            panelClass: ['red-color-snackbar'],
                        });
                    }
                );
            } else {
                this._dashboardCommonService.savePartnerProfile(this.partnerProfileForm.value).subscribe(
                    (response: PartnerNameModel) => {
                        this.snackBar.open('Profile Created Successfully!', '', {
                            ...this.snackBarConf,
                            duration: 3000,
                            panelClass: ['green-color-snackbar'],
                        });
                        this.clearForm(this.partnerProfileForm);
                        if(response){
                            this._partnerProfileService.setPartner(response);
                            this.router.navigateByUrl('dashboards/view-profiles');
                        }
                    },
                    (response) => {
                        this.snackBar.open('Profile Creating Failure!', '', {
                            ...this.snackBarConf,
                            duration: 3000,
                            panelClass: ['red-color-snackbar'],
                        });
                    }
                );

            }
        }
    }

    onClose(value = null): void {
        this.dialogRef.close(value);
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

    close(): void {
        this.dialogRef.close();
    }

}
