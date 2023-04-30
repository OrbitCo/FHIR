import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { PartnerNameModel } from '../../models/partner.model';
import { DashboardCommonService } from '../../services/dashboard-common.service';
import { PartnerProfileService } from '../../services/partner/partner-profile.service';
import { ProjectService } from '../../services/project/project.service';
import { CreateProfileComponent } from '../create-profile/create-profile.component';
import { DeleteConfirmationDailogComponent } from '../delete-confirmation-dailog/delete-confirmation-dailog.component';

@Component({
  selector: 'app-view-profiles',
  templateUrl: './view-profiles.component.html',
  styleUrls: ['./view-profiles.component.scss']
})
export class ViewProfilesComponent implements OnInit {

  products: any;
  allProfiles: any;
  partnerNames;
  newlyAddedPartnerId: any;
  partnerData: PartnerNameModel;

  unfilteredDataToSearch;
  filteredDataToSearch: any[] = [];

  viewProfileForm = new FormGroup(
    {
      partnerName: new FormControl(''),
    })

  constructor(
    public _dashboardCommonService: DashboardCommonService,
    public _partnerProfileService: PartnerProfileService,
    public _projectService: ProjectService,
    private dialog: MatDialog,
    public router: Router
  ) { }

  ngOnInit(): void {
    // Get the data
    this.partnerData = this._partnerProfileService.newlyAddedPartnerData;
    this._partnerProfileService.removePartner();
    this.getPartnerNames();
    this.partnerData?.id ? this.getPartnerDetails(this.partnerData?.id) : this.getPartnerDetails('');
  }

  lookup(e) {
    this.filteredDataToSearch = this.unfilteredDataToSearch
      .filter(
       // i => (i.name).toString().toLowerCase().indexOf(e) > -1 ||  (i.name).toString().toUpperCase().indexOf(e) > -1
        i => (i.name
          .toString()
          .toLowerCase()
          .indexOf(e.toString().toLowerCase()) !== -1)
      )
      .map(w => {
        return {
          text: w.name,
          value: w.id
        };
      });
  }

  clean(t) {
    t.value = '';
    this.lookup(t.value);
  }

  getPartnerNames() {
    // Get the data
    this._dashboardCommonService.getAllPartnerNamesData().subscribe((res: PartnerNameModel) => {
      this.partnerNames = res;
      this.unfilteredDataToSearch = res;
      if (this.partnerData) {
        this.viewProfileForm.patchValue({
          partnerName: this.partnerData.id,
        });
      }
      this.filteredDataToSearch = this.unfilteredDataToSearch.map(w => {
        return {
          text: w.name,
          value: w.id
        };
      });
    });
  }

  editPartnerProfile(data) {
    this.openDialog({ partnerData: data, edit: true });
  }

  openDialog(data) {
    const dialogRef = this.dialog.open(CreateProfileComponent, { position: { top: '10px' }, data, disableClose: true });

    dialogRef.afterClosed().subscribe(result => {
      this.getPartnerNames();
      this.getPartnerDetails(this.viewProfileForm.get('partnerName').value);
    });
  }

  editPartnerProfileProject(projectData) {
    this._dashboardCommonService.setPartnerProject(projectData);
    this.router.navigateByUrl('dashboards/create-project');
  }

  partnerDeleteDialog(data) {
    this.openDeleteDialog({ partnerData: data, delete: 'deletePartner' });
  }

  projectDeleteDialog(data) {
    this.openDeleteDialog({ partnerData: data, delete: 'deleteProject' });
  }

  openDeleteDialog(data) {
    const dialogRef = this.dialog.open(DeleteConfirmationDailogComponent, { position: { top: '10px' }, data, disableClose: true });

    dialogRef.afterClosed().subscribe(result => {
      this.getPartnerNames();
      this.getPartnerDetails(this.viewProfileForm.get('partnerName').value);
      console.log('The dialog was closed');
    });
  }

  createPartner() {
    this.router.navigateByUrl('dashboards/create-profile');
  }

  getPartnerDetails(partner) {
    let partnerId = partner?.value ? partner?.value : partner
    this._dashboardCommonService.getPartnerProfileData(partnerId).subscribe(response => {
      this.allProfiles = response;
    });
  }

  createProject(profile) {
    this.router.navigateByUrl('dashboards/create-project');
    this._projectService.setSelectedProject(profile.id);
  }

  goToProjectView(p) {
    this.router.navigate(['dashboards/project-details'], { state: { example: p } });

  }

}
