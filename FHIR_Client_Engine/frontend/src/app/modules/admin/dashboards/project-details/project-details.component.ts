import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { DashboardCommonService } from '../../services/dashboard-common.service';
import { ProjectService } from '../../services/project/project.service';

@Component({
  selector: 'app-project-details',
  templateUrl: './project-details.component.html',
  styleUrls: ['./project-details.component.scss']
})
export class ProjectDetailsComponent implements OnInit {
  selectedProject: any;
  projectData: any;

  constructor(
    public _dashboardCommonService: DashboardCommonService,
    public _projectService: ProjectService,
    public router: Router
  ) {
    this.selectedProject = this.router?.getCurrentNavigation()?.extras?.state?.example;
  }

  ngOnInit(): void {

    this.getProjectData();
  }

  getProjectData() {
    this._projectService.getProjectData(this.selectedProject?.id).subscribe(response => {
      this.projectData = response;
    });
  }

  editProject(projectData) {
    this._dashboardCommonService.setPartnerProject(projectData);
    this.router.navigateByUrl('dashboards/create-project');
  }

}
