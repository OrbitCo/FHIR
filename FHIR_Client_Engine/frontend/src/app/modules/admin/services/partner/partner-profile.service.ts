import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PartnerProfileService {
  newlyAddedPartnerData: any;
  constructor() { }

  setPartner(data) {
    this.newlyAddedPartnerData = data;
  }

  removePartner() {
    this.newlyAddedPartnerData = "";
  }

}
