export interface PartnerNameModel {
    id: number;
    name: string;
}

export interface PartnerDetailsModel {
    id: number;
    description?: string;
    email: string;
    mobileNumber: string;
    partnerName: string;
    primaryContactName?: string;
    projects?: ProjectDetailsModel;
}

export interface ProjectDetailsModel {
    id: number;
    authentication?: string;
    authorizationEndpoint?: string;
    clientId: string;
    clientSecret?: string;
    connection?: string;
    contactNumber?: string;
    description?: string;
    email: string;
    grantType?: string;
    isDeleted?: boolean;
    isPartnerDeleted?: boolean;
    partnerId: number;
    partnerName: string;
    port?: number;
    projectName: string;
    query?: string;
    redirectUrls?: string;
    status?: string;
    tokenEndpoint?: string;
    uri?: string;
}