import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { PartnerNameModel } from '../../models/partner.model';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  selectedParterForProject: any;

  constructor(private _httpClient: HttpClient) { }

  setSelectedProject(data) {
    this.selectedParterForProject = data;
  }

  removeSelectedProject() {
    this.selectedParterForProject = '';
  }

  getProjectData(projectId) {
    return this._httpClient.get(environment.apiUrl + '/api/project/get-project/' + projectId);
  }

}
