export interface AuthModel {
    name: string;
    authenticationType: string;
    grantType: string;
    authorizationEndpoint: string;
    tokenEndpoint: string;
    clientId: string;
    clientSecret: string;
    clientCode: string;
    clientScope: string;
    redirectUrls: string; //Comma-delimited
}
