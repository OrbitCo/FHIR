import { ChangeDetectionStrategy, Component, Inject, OnInit, ViewEncapsulation } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { DashboardCommonService } from '../../services/dashboard-common.service';

interface DialogData {
  partnerData: any[];
  id: any[];
  delete: string;
}

@Component({
  selector: 'app-delete-confirmation-dailog',
  templateUrl: './delete-confirmation-dailog.component.html',
  styleUrls: ['./delete-confirmation-dailog.component.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeleteConfirmationDailogComponent implements OnInit {

  private snackBarConf: MatSnackBarConfig = {
    duration: 0,
    horizontalPosition: 'center',
    verticalPosition: 'top',
  };
  projectData: any;

  constructor(
    private _dashboardCommonService: DashboardCommonService,
    public dialogRef: MatDialogRef<DeleteConfirmationDailogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private snackBar: MatSnackBar,
  ) { }

  ngOnInit(): void {
    this.projectData = this.data.partnerData;
  }

  deletePartnerOrProject() {
    if (this.data.delete == 'deletePartner') {
      this._dashboardCommonService.deleteSinglePartner(this.projectData.id)
        .subscribe(() => {
          this.successMessage();
        },
          (response) => {
            this.failureMessage();
          });
    } else {
      this._dashboardCommonService.deleteSingleProject(this.projectData.id)
        .subscribe(() => {
          this.successMessage();
        },
          (response) => {
            this.failureMessage();
          });
    }
  }

  successMessage() {
    this.onClose({});
    this.snackBar.open('Deleted Successfully!', '', {
      ...this.snackBarConf,
      duration: 3000,
      panelClass: ['green-color-snackbar'],
    }).afterDismissed().subscribe(() => {
      
    });
  }

  onClose(value = null): void {
    this.dialogRef.close(value);
  }

  failureMessage() {
    this.dialogRef.close();
    this.snackBar.open('Delete Failure!', '', {
      ...this.snackBarConf,
      duration: 3000,
      panelClass: ['red-color-snackbar'],
    });
  }

  close(): void {
    this.dialogRef.close();
  }

}
