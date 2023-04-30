import {RequestModel} from "./request.model";

export interface FHIRExportModel {
    request: RequestModel;
    type: string; // "BATCH" or "TRNSC"
    body: string; //Bundle to POST (typically a search result)
}
