import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { DashboardCommonService } from '../../services/dashboard-common.service';
import { PartnerProfileService } from '../../services/partner/partner-profile.service';
import { DeleteConfirmationDailogComponent } from '../delete-confirmation-dailog/delete-confirmation-dailog.component';

@Component({
  selector: 'app-view-projects',
  templateUrl: './view-projects.component.html',
  styleUrls: ['./view-projects.component.scss']
})
export class ViewProjectsComponent implements OnInit, AfterViewInit {
  @ViewChild('allProjectsTable', { read: MatSort }) allProjectsTableMatSort: MatSort;
  labels: { [key: string]: string };
  allProjectsDataSource: MatTableDataSource<any> = new MatTableDataSource();
  allProjectsTableColumns: string[] = ['partnerName', 'projectName', 'status', 'frequency', 'buttons']
  data: any;
  allProjects: any;

  constructor(
    public _dashboardCommonService: DashboardCommonService,
    private _partnerProfileService: PartnerProfileService,
    public router: Router,
    private dialog: MatDialog,
  ) {
    this.updateLanguage();
  }

  ngOnInit(): void {
    // Get Data
    this.getAllProjectsList();
  }

  ngAfterViewInit(): void {
    // Make the data source sortable
    this.allProjectsDataSource.sort = this.allProjectsTableMatSort;
  }

  getAllProjectsList() {
    this._dashboardCommonService.getAllProjectsData().subscribe(response => {
      this.allProjects = response;
      // Store the table data
      this.allProjectsDataSource.data = this.allProjects;

    });
  }

  editProject(projectData) {
    this._dashboardCommonService.setPartnerProject(projectData);
    this.router.navigateByUrl('dashboards/create-project');
  }

  projectDeleteDialog(projectData) {
    this.openDeleteDialog({ partnerData: projectData, delete: 'deleteProject' });

  }

  openDeleteDialog(data) {
    const dialogRef = this.dialog.open(DeleteConfirmationDailogComponent, { position: { top: '10px' }, data, disableClose: true });

    dialogRef.afterClosed().subscribe(result => {
      this.getAllProjectsList();
      console.log('The delete project was closed');
    });

  }

  updateLanguage() {
    this.labels = {
      partnerName: 'Partner',
      projectName: 'Project',
      projectStatus: 'Status',
      projectFrequency: 'Frequency',
      actions: 'Actions',
    };
  }

  goToProfileView(p) {
    let response = {
      id: p.partnerId,
      name:p.partnerName
    }
    this._partnerProfileService.setPartner(response);
    this.router.navigate(['dashboards/view-profiles']);

  }

  goToProjectView(p) {
    this.router.navigate(['dashboards/project-details'], { state: { example: p } });

  }

  /**
     * Track by function for ngFor loops
     *
     * @param index
     * @param item
     */
  trackByFn(index: number, item: any): any {
    return item.id || index;
  }

}
