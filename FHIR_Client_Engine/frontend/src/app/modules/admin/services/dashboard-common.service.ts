import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DashboardCommonService {
  singleProjectData: any;
  constructor(private _httpClient: HttpClient) { }

  savePartnerProfile(formData): Observable<any> {
    const url = environment.apiUrl + '/api/partner/create-partner';
    return this._httpClient.post(url, formData);
  }

  updatePartner(formData, id): Observable<any> {
    const url = environment.apiUrl + '/api/partner/update-partner/' + id;
    return this._httpClient.put(url, formData);
  }

  createProject(formData): Observable<any> {
    const url = environment.apiUrl + '/api/project/create-project';
    return this._httpClient.post(url, formData);
  }

  updateProject(formData, id): Observable<any> {
      console.log(formData);
    const url = environment.apiUrl + '/api/project/update-project/' + id;
    return this._httpClient.put(url, formData);
  }

  getAllPartnerNamesData() {
    return this._httpClient.get(environment.apiUrl + '/api/partner/get-partner-names')
  }

  getAllPartnerProfileData() {
    return this._httpClient.get(environment.apiUrl + '/api/partner/get-all-partner')
  }

  getPartnerProfileData(partnerId) {
    let url = partnerId ? '/api/partner/get-all-partner?partnerId=' +partnerId : '/api/partner/get-all-partner?partnerId='
    return this._httpClient.get(environment.apiUrl + url);
  }

  getAllProjectsData() {
    return this._httpClient.get(environment.apiUrl + '/api/project/get-all-project')
  }

  setPartnerProject(data) {
    this.singleProjectData = data;
  }

  removePartenerProject() {
    this.singleProjectData = "";
  }

  deleteSinglePartner(id){
    return this._httpClient.delete(environment.apiUrl + '/api/partner/delete-partner/' +id);
  }

  deleteSingleProject(id){
    return this._httpClient.delete(environment.apiUrl + '/api/project/delete-project/' +id);
  }

  fetchToken(formData): Observable<any> {
    const url = environment.apiUrl + '/api/auth/get-token';
    return this._httpClient.post(url, formData);
  }

  getAPIResults(searchJSON:JSON) {
    //return this._httpClient.get(environment.apiUrl + `/api/fhir/get-authorized-api-result?uri=${searchString}`)
      return this._httpClient.post(environment.apiUrl + '/api/fhir/get-authorized-api-result', searchJSON);
  }

  sendDataForFHIRExport(data): Observable<any> {
      console.log(data);
    const url = environment.apiUrl + '/api/project/import-data-to-client';
    return this._httpClient.post(url, data);
  }

  //TODO: Refactor
  downloadCSVFile(data): Observable<any> {
    let url = environment.apiUrl + '/api/project/download-csv';
    return this._httpClient.post(url, data, {responseType: 'blob'});
  }

  getTestQueryResult(searchString) {
    return this._httpClient.get(environment.apiUrl + `/api/fhir/get-basic-auth-api-result?uri=${searchString}`)
  }

  basicSendPatientData(data,basicResponseUri,basicResponsePort): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    const url = environment.apiUrl + `/api/project/send-patient-data-to-client-basic-auth?uri=${basicResponseUri}:${basicResponsePort}`;
    return this._httpClient.post(url, data, {headers});
  }

  UploadCSV(body): Observable<any> {
    const url = environment.apiUrl + `/api/project/upload-csv-to-sftp`;
    return this._httpClient.post(url, body);
  }

  sendCSVToEmail(body): Observable<any> {
    const url = environment.apiUrl + `/api/project/send-csv-to-email`;
    return this._httpClient.post(url, body);
  }

  getPathFromJson(body) {
    const url = environment.apiUrl + `/api/project/get-fhir-paths-from-json`;
    return this._httpClient.post(url, body);
  }

}
