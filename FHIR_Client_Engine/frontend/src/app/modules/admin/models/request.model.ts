import {AuthModel} from "./auth.model";

export interface RequestModel {
    order: number;
    returnValue: string; //It's worth noting that this returnValue applies to the PREVIOUS query in the sequence
    connection: string;
    query: string;
    type: string;
    authentication: AuthModel; //Null if we expect to be authenticated already
}
